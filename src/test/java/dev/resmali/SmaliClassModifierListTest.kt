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

import com.android.tools.smali.dexlib2.AccessFlags
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiModifierListOwner
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import dev.resmali.psi.impl.SmaliAnnotation
import dev.resmali.psi.impl.SmaliFile
import org.junit.Assert

class SmaliClassModifierListTest : LightJavaCodeInsightFixtureTestCase() {
    fun testAllClassAccessFlags() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public final interface abstract synthetic enum annotation Lmy/pkg/blah; .super Ljava/lang/Object;",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        val modifierList = checkNotNull(smaliClass.modifierList)

        Assert.assertEquals(
            (AccessFlags.PUBLIC.value or AccessFlags.FINAL.value or AccessFlags.INTERFACE.value or AccessFlags.ABSTRACT.value or AccessFlags.SYNTHETIC.value or AccessFlags.ENUM.value or AccessFlags.ANNOTATION.value).toLong(),
            modifierList.accessFlags.toLong(),
        )

        Assert.assertTrue(modifierList.hasModifierProperty("public"))
        Assert.assertTrue(modifierList.hasModifierProperty("final"))
        Assert.assertTrue(modifierList.hasModifierProperty("interface"))
        Assert.assertTrue(modifierList.hasModifierProperty("abstract"))
        Assert.assertTrue(modifierList.hasModifierProperty("synthetic"))
        Assert.assertTrue(modifierList.hasModifierProperty("enum"))
        Assert.assertTrue(modifierList.hasModifierProperty("annotation"))

        Assert.assertTrue(modifierList.hasExplicitModifier("public"))
        Assert.assertTrue(modifierList.hasExplicitModifier("final"))
        Assert.assertTrue(modifierList.hasExplicitModifier("interface"))
        Assert.assertTrue(modifierList.hasExplicitModifier("abstract"))
        Assert.assertTrue(modifierList.hasExplicitModifier("synthetic"))
        Assert.assertTrue(modifierList.hasExplicitModifier("enum"))
        Assert.assertTrue(modifierList.hasExplicitModifier("annotation"))
    }

    fun testNoClassAccessFlags() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class Lmy/pkg/blah; .super Ljava/lang/Object;",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        val modifierList = checkNotNull(smaliClass.modifierList)

        Assert.assertEquals(0, modifierList.accessFlags.toLong())

        Assert.assertFalse(modifierList.hasModifierProperty("public"))
        Assert.assertFalse(modifierList.hasModifierProperty("final"))
        Assert.assertFalse(modifierList.hasModifierProperty("interface"))
        Assert.assertFalse(modifierList.hasModifierProperty("abstract"))
        Assert.assertFalse(modifierList.hasModifierProperty("synthetic"))
        Assert.assertFalse(modifierList.hasModifierProperty("enum"))
        Assert.assertFalse(modifierList.hasModifierProperty("annotation"))

        Assert.assertFalse(modifierList.hasExplicitModifier("public"))
        Assert.assertFalse(modifierList.hasExplicitModifier("final"))
        Assert.assertFalse(modifierList.hasExplicitModifier("interface"))
        Assert.assertFalse(modifierList.hasExplicitModifier("abstract"))
        Assert.assertFalse(modifierList.hasExplicitModifier("synthetic"))
        Assert.assertFalse(modifierList.hasExplicitModifier("enum"))
        Assert.assertFalse(modifierList.hasExplicitModifier("annotation"))
    }

    fun testAddClassAccessFlag() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public Lmy/pkg/blah;\n.super Ljava/lang/Object;",
        ) as SmaliFile
        myFixture.configureFromExistingVirtualFile(file.virtualFile)

        ApplicationManager.getApplication().runWriteAction {
            checkNotNull(checkNotNull(file.psiClass).modifierList).setModifierProperty("final", true)
        }

        myFixture.checkResult(
            ".class public final Lmy/pkg/blah;\n.super Ljava/lang/Object;",
        )
    }

    fun testRemoveClassAccessFlag() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public final Lmy/pkg/blah;\n.super Ljava/lang/Object;",
        ) as SmaliFile
        myFixture.configureFromExistingVirtualFile(file.virtualFile)

        ApplicationManager.getApplication().runWriteAction {
            checkNotNull(checkNotNull(file.psiClass).modifierList).setModifierProperty("final", false)
        }

        myFixture.checkResult(
            ".class public Lmy/pkg/blah;\n.super Ljava/lang/Object;",
        )
    }

    fun testBasicAnnotation() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public final Lmy/pkg/blah;\n.super Ljava/lang/Object;\n.annotation Lmy/pkg/anno; .end annotation",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        val modifierList = checkNotNull(smaliClass.modifierList)

        val annotations: Array<SmaliAnnotation> = modifierList.annotations
        Assert.assertEquals(1, annotations.size.toLong())

        Assert.assertEquals("my.pkg.anno", annotations[0].qualifiedName)

        val applicableAnnotations: Array<SmaliAnnotation> = modifierList.applicableAnnotations
        Assert.assertEquals(1, applicableAnnotations.size.toLong())
        Assert.assertSame(annotations[0], applicableAnnotations[0])
    }

    fun testNoAnnotation() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public final Lmy/pkg/blah;\n.super Ljava/lang/Object;",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        val modifierList = checkNotNull(smaliClass.modifierList)

        // Ensures that the parent of the modifier list is a PsiModifierListOwner
        // e.g. for code like JavaSuppressionUtil.getInspectionIdsSuppressedInAnnotation,
        // which assumes the parent is a PsiModifierListOwner
        Assert.assertTrue(modifierList.parent is PsiModifierListOwner)

        Assert.assertEquals(0, modifierList.annotations.size.toLong())
        Assert.assertEquals(0, modifierList.applicableAnnotations.size.toLong())
    }

    fun testFindAnnotation() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            """
                .class public final Lmy/pkg/blah;
                .annotation Lanno; .end annotation
                .super Ljava/lang/Object;
                .annotation Lmy/pkg/anno; .end annotation
                .annotation Lmy/pkg/anno2; .end annotation
                .annotation Lmy/pkg/anno3; .end annotation
            """.trimIndent() + "\n",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        val modifierList = checkNotNull(smaliClass.modifierList)

        val smaliAnnotation = checkNotNull(modifierList.findAnnotation("my.pkg.anno2"))
        Assert.assertEquals("my.pkg.anno2", smaliAnnotation.qualifiedName)
    }

    // TODO: test modifierList.addAnnotation once implemented
}
