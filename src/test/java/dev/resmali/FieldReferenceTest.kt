/*
 * Copyright 2015, Google Inc.
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

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.psi.PsiField
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.JavaResolveTestCase
import dev.resmali.psi.impl.SmaliFieldReference
import org.junit.Assert

class FieldReferenceTest : JavaResolveTestCase() {
    /**
     * Test a reference to a java field from a smali class
     */
    @Throws(Exception::class)
    fun testJavaReferenceFromSmali() {
        val text = """
            .class public Lmy/pkg/blah; .super Ljava/lang/Object;
            .method public blah()V
                .locals 1
                sget-object v0, Ljava/lang/System;->o<ref>ut:Ljava/io/PrintStream;
                return-void
            .end method
        """.trimIndent()

        val fieldReference = checkNotNull(configureByFileText(text, "blah.smali") as? SmaliFieldReference)
        Assert.assertEquals("out", fieldReference.name)
        Assert.assertEquals("java.io.PrintStream", fieldReference.fieldType.type.canonicalText)

        val resolvedField = checkNotNull(fieldReference.resolve())
        Assert.assertEquals("out", resolvedField.name)
        Assert.assertNotNull(resolvedField.containingClass)
        Assert.assertEquals("java.lang.System", checkNotNull(resolvedField.containingClass).qualifiedName)
        Assert.assertEquals("java.io.PrintStream", resolvedField.type.canonicalText)
    }

    /**
     * Test a reference to a smali field from a smali class
     */
    @Throws(Exception::class)
    fun testSmaliReferenceFromSmali() {
        createFile(
            "blarg.smali",
            ".class public Lblarg; .super Ljava/lang/Object;\n.field public static blort:I",
        )

        val text = ".class public Lmy/pkg/blah; .super Ljava/lang/Object;\n.method public blah()V\n    .locals 1\n    sget v0, Lblarg;->bl<ref>ort:I\n    return-void\n.end method"

        val fieldReference = checkNotNull(configureByFileText(text, "blah.smali") as? SmaliFieldReference)
        Assert.assertEquals("blort", fieldReference.name)
        Assert.assertEquals("int", fieldReference.fieldType.type.canonicalText)

        val resolvedField = checkNotNull(fieldReference.resolve())
        Assert.assertEquals("blort", resolvedField.name)
        Assert.assertNotNull(resolvedField.containingClass)
        Assert.assertEquals("blarg", checkNotNull(resolvedField.containingClass).qualifiedName)
        Assert.assertEquals("int", resolvedField.type.canonicalText)
    }

    /**
     * Test a reference to a smali field from a java class
     */
    @Throws(Exception::class)
    fun testSmaliReferenceFromJava() {
        createFile(
            "blarg.smali",
            ".class public Lblarg; .super Ljava/lang/Object;.field public static blort:I",
        )

        val text = "public class blah { public static void something() {blarg.bl<ref>ort = 10;}}"

        val fieldReference = checkNotNull(configureByFileText(text, "blah.java"))

        val resolvedField = checkNotNull(fieldReference.resolve() as? PsiField)
        Assert.assertEquals("blort", resolvedField.name)
        Assert.assertNotNull(resolvedField.containingClass)
        Assert.assertEquals("blarg", checkNotNull(resolvedField.containingClass).qualifiedName)
        Assert.assertEquals("int", resolvedField.type.canonicalText)
    }

    override fun getTestProjectJdk(): Sdk {
        return IdeaTestUtil.getMockJdk21()
    }
}
