/*
 * Copyright 2016, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dev.resmali

import com.intellij.codeInsight.JavaCodeInsightTestCase
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.debugger.NoDataException
import com.intellij.debugger.engine.evaluation.CodeFragmentKind
import com.intellij.debugger.engine.evaluation.TextWithImportsImpl
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl
import com.intellij.testFramework.IdeaTestUtil
import dev.resmali.debugging.SmaliCodeFragmentFactory
import dev.resmali.psi.impl.SmaliFile
import org.junit.Assert

class SmaliCodeFragmentFactoryTest : JavaCodeInsightTestCase() {
    @Throws(NoDataException::class)
    fun testCompletion() {
        val smaliFile = configureByText(SmaliFileType, completionTestClass) as SmaliFile
        val smaliClass = checkNotNull(smaliFile.psiClass)

        var context: PsiElement = smaliClass.methods[0].instructions[0]
        assertCompletionContains("v", context, arrayOf("v2", "v3"), arrayOf("v0", "v1", "p0", "p1"))
        assertCompletionContains("p", context, arrayOf("p0", "p1"), arrayOf("v0", "v1", "v2", "v3"))

        context = smaliClass.methods[0].instructions[2]
        assertCompletionContains("v", context, arrayOf("v1", "v2", "v3"), arrayOf("v0", "p0", "p1"))
        assertCompletionContains("p", context, arrayOf("p0", "p1"), arrayOf("v0", "v1", "v2", "v3"))

        context = smaliClass.methods[0].instructions[6]
        assertCompletionContains("v", context, arrayOf("v0", "v1", "v2", "v3"), arrayOf("p0", "p1"))
        assertCompletionContains("p", context, arrayOf("p0", "p1"), emptyArray())
    }

    @Throws(NoDataException::class)
    fun testRegisterType() {
        val smaliFile = configureByText(
            SmaliFileType,
            registerTypeTestText.replace("<ref>", ""),
        ) as SmaliFile

        val refOffset = registerTypeTestText.indexOf("<ref>")

        val context = checkNotNull(smaliFile.findElementAt(refOffset)).parent
        assertVariableType(context, "v1", "java.util.Random")
        assertVariableType(context, "v0", "java.io.Serializable")
    }

    fun testUnknownClass() {
        val modifiedText = registerTypeTestText.replace("Random", "Rnd")
        val smaliFile = configureByText(
            SmaliFileType,
            modifiedText.replace("<ref>", ""),
        ) as SmaliFile

        val refOffset = modifiedText.indexOf("<ref>")

        val context = checkNotNull(smaliFile.findElementAt(refOffset)).parent
        assertVariableType(context, "v1", "java.lang.Object")
        assertVariableType(context, "v0", "java.lang.Object")
    }

    private fun assertCompletionContains(
        completionText: String, context: PsiElement?, expectedItems: Array<String>,
        disallowedItems: Array<String>,
    ) {
        val codeFragmentFactory = SmaliCodeFragmentFactory()
        val fragment = checkNotNull(
            codeFragmentFactory.createPsiCodeFragment(
                TextWithImportsImpl(CodeFragmentKind.EXPRESSION, completionText),
                context, project,
            ),
        )

        val editor = createEditor(fragment.virtualFile)
        editor.caretModel.moveToOffset(completionText.length)

        CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(project, editor)
        val elements = checkNotNull(LookupManager.getInstance(project).activeLookup).items

        val expectedSet = expectedItems.toMutableSet()
        val disallowedSet = disallowedItems.toSet()

        for (element in elements) {
            expectedSet.remove(element.toString())
            Assert.assertFalse(disallowedSet.contains(element.toString()))
        }

        Assert.assertTrue(expectedSet.isEmpty())
    }

    private fun assertVariableType(context: PsiElement?, variableName: String, expectedType: String?) {
        val codeFragmentFactory = SmaliCodeFragmentFactory()
        val fragment = checkNotNull(
            codeFragmentFactory.createPsiCodeFragment(
                TextWithImportsImpl(CodeFragmentKind.EXPRESSION, variableName),
                context, project,
            ),
        )

        val editor = createEditor(fragment.virtualFile)
        editor.caretModel.moveToOffset(1)

        val reference = checkNotNull(fragment.findElementAt(0)).parent as PsiReferenceExpressionImpl
        Assert.assertEquals(expectedType, checkNotNull(reference.type).canonicalText)
    }

    override fun createEditor(file: VirtualFile): Editor {
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val editor: Editor = checkNotNull(
            FileEditorManager.getInstance(project).openTextEditor(
                com.intellij.openapi.fileEditor.OpenFileDescriptor(project, file, 0), false,
            ),
        )
        DaemonCodeAnalyzer.getInstance(project).restart("Editor opened for test")

        (editor as EditorImpl).setCaretActive()
        return editor
    }

    override fun getTestProjectJdk(): Sdk {
        return IdeaTestUtil.getMockJdk21()
    }

    companion object {
        private val completionTestClass = """
            .class public Lmy/pkg/blah; .super Ljava/lang/Object;
            .method public getRandomParentType(I)I
                .registers 4
                .param p1, "edge"    # I

                .prologue
                const/4 v1, 0x2

                .line 179
                if-nez p1, :cond_5

                move v0, v1

                .line 185
                :goto_4
                return v0

                .line 182
                :cond_5
                if-ne p1, v1, :cond_f

                .line 183
                sget-object v0, Lorg/jf/Penroser/PenroserApp;->random:Ljava/util/Random;

                const/4 v1, 0x3

                invoke-virtual {v0, v1}, Ljava/util/Random;->nextInt(I)I

                move-result v0

                goto :goto_4

                .line 185
                :cond_f
                sget-object v0, Lorg/jf/Penroser/PenroserApp;->random:Ljava/util/Random;

                invoke-virtual {v0, v1}, Ljava/util/Random;->nextInt(I)I

                move-result v0

                goto :goto_4
            .end method
        """.trimIndent()

        private val registerTypeTestText = """
            .class public LRegisterTypeTest;
            .super Ljava/lang/Object;

            # virtual methods
            .method public blah()V
                .registers 6

                .prologue
                const/16 v3, 0xa

                .line 7
                new-instance v0, Ljava/util/Random;

                invoke-direct {v0}, Ljava/util/Random;-><init>()V

                .line 9
                invoke-virtual {v0, v3}, Ljava/util/Random;->nextInt(I)I

                move-result v1

                const/4 v2, 0x5

                if-le v1, v2, :cond_26

                .line 10
                new-instance v1, Ljava/security/SecureRandom;

                invoke-direct {v1}, Ljava/security/SecureRandom;-><init>()V

                .line 14
                :goto_13
                sget-o<ref>bject v2, Ljava/lang/System;->out:Ljava/io/PrintStream;

                invoke-virtual {v1, v3}, Ljava/util/Random;->nextInt(I)I

                move-result v1

                invoke-virtual {v2, v1}, Ljava/io/PrintStream;->println(I)V

                .line 15
                sget-object v1, Ljava/lang/System;->out:Ljava/io/PrintStream;

                invoke-virtual {v0}, Ljava/lang/Object;->toString()Ljava/lang/String;

                move-result-object v0

                invoke-virtual {v1, v0}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

                .line 16
                return-void

                .line 12
                :cond_26
                invoke-virtual {p0}, LRegisterTypeTest;->getSerializable()Ljava/io/Serializable;

                move-result-object v1

                move-object v4, v1

                move-object v1, v0

                move-object v0, v4

                goto :goto_13
            .end method

            .method public getSerializable()Ljava/io/Serializable;
                .registers 2

                .prologue
                .line 19
                new-instance v0, Ljava/util/Random;

                invoke-direct {v0}, Ljava/util/Random;-><init>()V

                return-object v0
            .end method
        """.trimIndent() + "\n"
    }
}
