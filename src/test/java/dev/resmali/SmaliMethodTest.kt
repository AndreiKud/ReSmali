/*
 * Copyright 2014, Google Inc.
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

import com.android.tools.smali.dexlib2.Opcode
import com.intellij.psi.PsiTypes
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import dev.resmali.psi.impl.SmaliFile
import dev.resmali.psi.impl.SmaliInstruction
import dev.resmali.psi.impl.SmaliMethod
import dev.resmali.psi.impl.SmaliMethodParameter
import org.junit.Assert

class SmaliMethodTest : LightJavaCodeInsightFixtureTestCase() {
    fun testMethodRegisters() {
        val text = ".class public Lmy/pkg/blah; .super Ljava/lang/Object;\n.me<ref>thod blah()V\n    .registers 123\n    return-void\n.end method"

        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            text.replace("<ref>", ""),
        ) as SmaliFile

        val leafElement = checkNotNull(file.findElementAt(text.indexOf("<ref>")))
        val methodElement = checkNotNull(leafElement.parent as? SmaliMethod)

        Assert.assertEquals(123, methodElement.registerCount.toLong())
        Assert.assertEquals(1, methodElement.parameterRegisterCount.toLong())
    }

    fun testMethodRegisters2() {
        val text = ".class public Lmy/pkg/blah; .super Ljava/lang/Object;\n.me<ref>thod blah(IJLjava/lang/String;)V\n    .locals 123\n    return-void\n.end method"

        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            text.replace("<ref>", ""),
        ) as SmaliFile

        val leafElement = checkNotNull(file.findElementAt(text.indexOf("<ref>")))
        val methodElement = checkNotNull(leafElement.parent as? SmaliMethod)

        Assert.assertEquals(128, methodElement.registerCount.toLong())
        Assert.assertEquals(5, methodElement.parameterRegisterCount.toLong())
    }

    fun testStaticRegisterCount() {
        val text = ".class public Lmy/pkg/blah; .super Ljava/lang/Object;\n.method static blah(IJLjava/lang/String;)V\n    .locals 123\n    return-void\n.end method"

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        val smaliMethod = smaliClass.methods[0]

        Assert.assertEquals(127, smaliMethod.registerCount.toLong())
        Assert.assertEquals(4, smaliMethod.parameterRegisterCount.toLong())

        Assert.assertEquals(0, smaliMethod.parameterList.parameters[0].parameterRegisterNumber.toLong())
        Assert.assertEquals(123, smaliMethod.parameterList.parameters[0].registerNumber.toLong())
    }

    fun testMethodParams() {
        myFixture.addFileToProject(
            "my/TestAnnotation.smali",
            """
                .class public interface abstract annotation Lmy/TestAnnotation;
                .super Ljava/lang/Object;
                .implements Ljava/lang/annotation/Annotation;

                .method public abstract testBooleanValue()Z
                .end method

                .method public abstract testStringArrayValue()[Ljava/lang/String;
                .end method

                .method public abstract testStringValue()Ljava/lang/String;
                .end method
            """.trimIndent(),
        )

        val text = """
            .class public Lmy/pkg/blah; .super Ljava/lang/Object;
            .method blah(IJLjava/lang/String;)V
                .locals 123
                .param p1, "anInt"
                .param p2
                    .annotation runtime Lmy/TestAnnotation;
                        testStringValue = "myValue"
                    .end annotation
                .end param
                return-void
            .end method
        """.trimIndent()

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        val smaliMethod = smaliClass.methods[0]

        val paramList = smaliMethod.parameterList
        val parameters: Array<SmaliMethodParameter> = paramList.parameters
        Assert.assertEquals(3, parameters.size.toLong())

        Assert.assertEquals("int", parameters[0].type.canonicalText)
        Assert.assertEquals("\"anInt\"", parameters[0].name)
        Assert.assertEquals(1, parameters[0].registerCount.toLong())
        Assert.assertEquals(124, parameters[0].registerNumber.toLong())
        Assert.assertEquals(1, parameters[0].parameterRegisterNumber.toLong())
        Assert.assertEquals(0, parameters[0].annotations.size.toLong())

        Assert.assertEquals("long", parameters[1].type.canonicalText)
        Assert.assertEquals("", parameters[1].name)
        Assert.assertEquals(2, parameters[1].registerCount.toLong())
        Assert.assertEquals(125, parameters[1].registerNumber.toLong())
        Assert.assertEquals(2, parameters[1].parameterRegisterNumber.toLong())
        Assert.assertEquals(1, parameters[1].annotations.size.toLong())
        Assert.assertEquals("my.TestAnnotation", parameters[1].annotations[0].qualifiedName)

        Assert.assertEquals("java.lang.String", parameters[2].type.canonicalText)
        Assert.assertEquals("", parameters[2].name)
        Assert.assertEquals(1, parameters[2].registerCount.toLong())
        Assert.assertEquals(127, parameters[2].registerNumber.toLong())
        Assert.assertEquals(4, parameters[2].parameterRegisterNumber.toLong())
        Assert.assertEquals(0, parameters[2].annotations.size.toLong())
    }

    fun testVarArgsMethod() {
        val text = """
            .class public Lmy/pkg/blah; .super Ljava/lang/Object;
            .method varargs static blah(IJ[Ljava/lang/String;)V
                .locals 123
                return-void
            .end method
            .method varargs static blah2(IJLjava/lang/String;)V
                .locals 123
                return-void
            .end method
        """.trimIndent()

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        var smaliMethod = smaliClass.methods[0]

        Assert.assertTrue(smaliMethod.isVarArgs)
        Assert.assertFalse(smaliMethod.parameterList.parameters[0].isVarArgs)
        Assert.assertFalse(smaliMethod.parameterList.parameters[1].isVarArgs)
        Assert.assertTrue(smaliMethod.parameterList.parameters[2].isVarArgs)

        smaliMethod = smaliClass.methods[1]
        Assert.assertTrue(smaliMethod.isVarArgs)
        Assert.assertFalse(smaliMethod.parameterList.parameters[0].isVarArgs)
        Assert.assertFalse(smaliMethod.parameterList.parameters[1].isVarArgs)
        Assert.assertFalse(smaliMethod.parameterList.parameters[2].isVarArgs)
    }

    fun testGetInstructions() {
        val text: String = instructionsTestClass

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        val smaliMethod = smaliClass.methods[0]

        val instructions: MutableList<SmaliInstruction> = smaliMethod.instructions
        Assert.assertEquals(14, instructions.size.toLong())
    }

    private fun checkSourcePosition(smaliMethod: SmaliMethod, codeOffset: Int, opcode: Opcode?) {
        val sourcePosition = checkNotNull(smaliMethod.getSourcePositionForCodeOffset(codeOffset))

        val instruction = sourcePosition.elementAt as? SmaliInstruction
        Assert.assertEquals(opcode, checkNotNull(instruction).opcode)
        Assert.assertEquals(codeOffset.toLong(), instruction.offset.toLong())
    }

    fun testGetSourcePositionForCodeOffset() {
        val text: String = instructionsTestClass

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        val smaliMethod = smaliClass.methods[0]

        checkSourcePosition(smaliMethod, 0, Opcode.CONST_4)
        checkSourcePosition(smaliMethod, 2, Opcode.IF_NEZ)
        checkSourcePosition(smaliMethod, 6, Opcode.MOVE)
        checkSourcePosition(smaliMethod, 8, Opcode.RETURN)
        checkSourcePosition(smaliMethod, 10, Opcode.IF_NE)
        checkSourcePosition(smaliMethod, 14, Opcode.SGET_OBJECT)
        checkSourcePosition(smaliMethod, 18, Opcode.CONST_4)
        checkSourcePosition(smaliMethod, 20, Opcode.INVOKE_VIRTUAL)
        checkSourcePosition(smaliMethod, 26, Opcode.MOVE_RESULT)
        checkSourcePosition(smaliMethod, 28, Opcode.GOTO)
        checkSourcePosition(smaliMethod, 30, Opcode.SGET_OBJECT)
        checkSourcePosition(smaliMethod, 34, Opcode.INVOKE_VIRTUAL)
        checkSourcePosition(smaliMethod, 40, Opcode.MOVE_RESULT)
        checkSourcePosition(smaliMethod, 42, Opcode.GOTO)
    }

    fun testThrowsList() {
        val text: String = instructionsTestClass

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        val smaliMethod = smaliClass.methods[0]

        val throwsList = checkNotNull(smaliMethod.throwsList)
        Assert.assertEquals(0, throwsList.referencedTypes.size.toLong())
        Assert.assertEquals(0, throwsList.referenceElements.size.toLong())
    }

    fun testPrimitiveReturnType() {
        val text = ".class public Lmy/pkg/blah; .super Ljava/lang/Object;\n.method blah()I\n    .registers 123\n    return-void\n.end method"

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        val smaliMethod = smaliClass.methods[0]

        Assert.assertNotNull(smaliMethod.returnType)
        Assert.assertTrue(checkNotNull(smaliMethod.returnType).isConvertibleFrom(PsiTypes.intType()))
        Assert.assertTrue(checkNotNull(smaliMethod.returnType).isAssignableFrom(PsiTypes.intType()))
    }

    companion object {
        private val instructionsTestClass = """
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
    }
}
