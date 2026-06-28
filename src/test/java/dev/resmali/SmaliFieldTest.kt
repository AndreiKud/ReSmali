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
import com.intellij.psi.PsiField
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.PsiTypeElement
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import dev.resmali.psi.impl.SmaliField
import dev.resmali.psi.impl.SmaliFile
import org.junit.Assert

class SmaliFieldTest : LightJavaCodeInsightFixtureTestCase() {
    fun testBasicField() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public Lmy/pkg/blah; .super Ljava/lang/Object;\n.field public myField:I",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("my.pkg.blah", smaliClass.qualifiedName)

        val fields: Array<SmaliField> = smaliClass.fields
        Assert.assertEquals(1, fields.size.toLong())
        Assert.assertEquals("myField", fields[0].name)
        Assert.assertTrue(fields[0].type is PsiPrimitiveType)
        Assert.assertEquals("int", fields[0].type.canonicalText)
        val typeElement: PsiTypeElement? = fields[0].typeElement
        Assert.assertNotNull("I", typeElement)
        Assert.assertEquals("I", checkNotNull(typeElement).text)

        val modifierList = checkNotNull(fields[0].modifierList)
        Assert.assertEquals(AccessFlags.PUBLIC.value.toLong(), modifierList.accessFlags.toLong())
        Assert.assertTrue(modifierList.hasExplicitModifier("public"))
        Assert.assertTrue(modifierList.hasModifierProperty("public"))
        Assert.assertTrue(fields[0].hasModifierProperty("public"))

        val psifields: Array<PsiField> = smaliClass.allFields
        Assert.assertEquals(1, psifields.size.toLong())
        Assert.assertEquals("myField", psifields[0].name)

        var field = smaliClass.findFieldByName("myField", true)
        Assert.assertNotNull(field)
        Assert.assertEquals("myField", checkNotNull(field).name)

        field = smaliClass.findFieldByName("nonExistantField", true)
        Assert.assertNull(field)
        field = smaliClass.findFieldByName("nonExistantField", false)
        Assert.assertNull(field)
    }

    fun testSmaliSuperField() {
        myFixture.addFileToProject(
            "my/pkg/base.smali",
            ".class public Lmy/pkg/base; .super Ljava/lang/Object;\n.field public baseField:I",
        )

        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public Lmy/pkg/blah; .super Lmy/pkg/base;\n.field public myField:I",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("my.pkg.blah", smaliClass.qualifiedName)

        var fields: Array<out PsiField> = smaliClass.fields
        Assert.assertEquals(1, fields.size.toLong())
        Assert.assertEquals("myField", fields[0].name)

        fields = smaliClass.allFields
        Assert.assertEquals(2, fields.size.toLong())

        Assert.assertTrue(fields[0].name == "myField" || fields[1].name == "myField")
        Assert.assertTrue(fields[0].name == "baseField" || fields[1].name == "baseField")

        var field = smaliClass.findFieldByName("myField", true)
        Assert.assertNotNull(field)
        Assert.assertEquals("myField", checkNotNull(field).name)

        field = smaliClass.findFieldByName("myField", false)
        Assert.assertNotNull(field)
        Assert.assertEquals("myField", checkNotNull(field).name)

        field = smaliClass.findFieldByName("baseField", false)
        Assert.assertNull(field)

        field = smaliClass.findFieldByName("baseField", true)
        Assert.assertNotNull(field)
        Assert.assertEquals("baseField", checkNotNull(field).name)

        field = smaliClass.findFieldByName("nonExistantField", true)
        Assert.assertNull(field)
        field = smaliClass.findFieldByName("nonExistantField", false)
        Assert.assertNull(field)
    }

    fun testJavaSuperField() {
        myFixture.addFileToProject(
            "my/pkg/base.java",
            "package my.pkg; public class base { public int baseField; }",
        )

        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public Lmy/pkg/blah; .super Lmy/pkg/base;\n.field public myField:I",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("my.pkg.blah", smaliClass.qualifiedName)

        var fields: Array<out PsiField> = smaliClass.fields
        Assert.assertEquals(1, fields.size.toLong())
        Assert.assertEquals("myField", fields[0].name)

        fields = smaliClass.allFields
        Assert.assertEquals(2, fields.size.toLong())

        Assert.assertTrue(fields[0].name == "myField" || fields[1].name == "myField")
        Assert.assertTrue(fields[0].name == "baseField" || fields[1].name == "baseField")

        var field = smaliClass.findFieldByName("myField", true)
        Assert.assertNotNull(field)
        Assert.assertEquals("myField", checkNotNull(field).name)

        field = smaliClass.findFieldByName("myField", false)
        Assert.assertNotNull(field)
        Assert.assertEquals("myField", checkNotNull(field).name)

        field = smaliClass.findFieldByName("baseField", false)
        Assert.assertNull(field)

        field = smaliClass.findFieldByName("baseField", true)
        Assert.assertNotNull(field)
        Assert.assertEquals("baseField", checkNotNull(field).name)

        field = smaliClass.findFieldByName("nonExistantField", true)
        Assert.assertNull(field)
        field = smaliClass.findFieldByName("nonExistantField", false)
        Assert.assertNull(field)
    }

    fun testMultipleField() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            """
                .class public Lmy/pkg/blah; .super Ljava/lang/Object;
                .field public myField:I
                .field public myField2:Ljava/lang/String;
                .field public myField3:[Ljava/lang/String;
                .field public myField4:[[[Ljava/lang/String;
            """.trimIndent() + "\n",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("my.pkg.blah", smaliClass.qualifiedName)

        val fields: Array<SmaliField> = smaliClass.fields
        Assert.assertEquals(4, fields.size.toLong())
        Assert.assertEquals("myField", fields[0].name)
        Assert.assertEquals("myField2", fields[1].name)
        Assert.assertEquals("myField3", fields[2].name)
        Assert.assertEquals("myField4", fields[3].name)
        Assert.assertEquals("int", fields[0].type.canonicalText)
        Assert.assertEquals("java.lang.String", fields[1].type.canonicalText)
        Assert.assertEquals("java.lang.String[]", fields[2].type.canonicalText)
        Assert.assertEquals("java.lang.String[][][]", fields[3].type.canonicalText)

        var field = smaliClass.findFieldByName("myField", true)
        Assert.assertNotNull(field)
        Assert.assertEquals("myField", checkNotNull(field).name)

        field = smaliClass.findFieldByName("myField2", true)
        Assert.assertNotNull(field)
        Assert.assertEquals("myField2", checkNotNull(field).name)

        field = smaliClass.findFieldByName("myField3", true)
        Assert.assertNotNull(field)
        Assert.assertEquals("myField3", checkNotNull(field).name)

        field = smaliClass.findFieldByName("myField4", true)
        Assert.assertNotNull(field)
        Assert.assertEquals("myField4", checkNotNull(field).name)

        field = smaliClass.findFieldByName("nonExistantField", true)
        Assert.assertNull(field)
        field = smaliClass.findFieldByName("nonExistantField", false)
        Assert.assertNull(field)
    }

    // TODO: identical to testBasicField (testFieldAnnotation as an example)
    fun testFieldAnnotations() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public Lmy/pkg/blah; .super Ljava/lang/Object;\n.field public myField:I",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("my.pkg.blah", smaliClass.qualifiedName)

        val fields: Array<SmaliField> = smaliClass.fields
        Assert.assertEquals(1, fields.size.toLong())
        Assert.assertEquals("myField", fields[0].name)
        Assert.assertTrue(fields[0].type is PsiPrimitiveType)
        Assert.assertEquals("int", fields[0].type.canonicalText)
        val typeElement: PsiTypeElement? = fields[0].typeElement
        Assert.assertNotNull("I", typeElement)
        Assert.assertEquals("I", checkNotNull(typeElement).text)

        val modifierList = checkNotNull(fields[0].modifierList)
        Assert.assertEquals(AccessFlags.PUBLIC.value.toLong(), modifierList.accessFlags.toLong())
        Assert.assertTrue(modifierList.hasExplicitModifier("public"))
        Assert.assertTrue(modifierList.hasModifierProperty("public"))
        Assert.assertTrue(fields[0].hasModifierProperty("public"))

        val psifields: Array<PsiField> = smaliClass.allFields
        Assert.assertEquals(1, psifields.size.toLong())
        Assert.assertEquals("myField", psifields[0].name)

        var field = smaliClass.findFieldByName("myField", true)
        Assert.assertNotNull(field)
        Assert.assertEquals("myField", checkNotNull(field).name)

        field = smaliClass.findFieldByName("nonExistantField", true)
        Assert.assertNull(field)
        field = smaliClass.findFieldByName("nonExistantField", false)
        Assert.assertNull(field)
    }
}
