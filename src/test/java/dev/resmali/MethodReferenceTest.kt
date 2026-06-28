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

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.psi.PsiMethod
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.JavaResolveTestCase
import dev.resmali.psi.impl.SmaliMethodReference
import org.junit.Assert

class MethodReferenceTest : JavaResolveTestCase() {
    /**
     * Test a reference to a java method from a smali class
     */
    @Throws(Exception::class)
    fun testJavaReferenceFromSmali() {
        val text = """
            .class public Lmy/pkg/blah; .super Ljava/lang/Object;
            .method public blah()V
                .locals 1
                invoke-static {}, Ljava/lang/System;->nano<ref>Time()J
                return-void
            .end method
        """.trimIndent()

        val methodReference = configureByFileText(text, "blah.smali") as SmaliMethodReference
        Assert.assertEquals("nanoTime", methodReference.name)

        val resolvedMethod = checkNotNull(methodReference.resolve() as? PsiMethod)
        Assert.assertEquals("nanoTime", resolvedMethod.name)
        Assert.assertNotNull(resolvedMethod.containingClass)
        Assert.assertEquals("java.lang.System", checkNotNull(resolvedMethod.containingClass).qualifiedName)
        Assert.assertEquals(0, resolvedMethod.parameterList.parametersCount.toLong())
        Assert.assertNotNull(resolvedMethod.returnType)
        Assert.assertEquals("long", checkNotNull(resolvedMethod.returnType).canonicalText)
    }

    /**
     * Test a reference to a smali method from a smali class
     */
    @Throws(Exception::class)
    fun testSmaliReferenceFromSmali() {
        createFile(
            "blarg.smali",
            ".class public Lblarg; .super Ljava/lang/Object;.method public static blort(ILjava/lang/String;)V\n    .locals 0\n    return-void\n.end method\n",
        )

        val text = """
            .class public Lmy/pkg/blah; .super Ljava/lang/Object;
            .method public blah2()V
                .locals 0
                invoke-static {}, Lblarg;->bl<ref>ort(ILjava/lang/String;)V
                return-void
            .end method
        """.trimIndent()

        val methodReference = configureByFileText(text, "blah.smali") as SmaliMethodReference
        Assert.assertEquals("blort", methodReference.name)

        val resolvedMethod = checkNotNull(methodReference.resolve() as? PsiMethod)
        Assert.assertEquals("blort", resolvedMethod.name)
        Assert.assertNotNull(resolvedMethod.containingClass)
        Assert.assertEquals("blarg", checkNotNull(resolvedMethod.containingClass).qualifiedName)
        Assert.assertEquals(2, resolvedMethod.parameterList.parametersCount.toLong())
        Assert.assertEquals("int", resolvedMethod.parameterList.parameters[0].type.canonicalText)
        Assert.assertEquals(
            "java.lang.String",
            resolvedMethod.parameterList.parameters[1].type.canonicalText,
        )
        Assert.assertNotNull(resolvedMethod.returnType)
        Assert.assertEquals("void", checkNotNull(resolvedMethod.returnType).canonicalText)
    }

    /**
     * Test a reference to a smali method from a java class
     */
    @Throws(Exception::class)
    fun testSmaliReferenceFromJava() {
        createFile(
            "blarg.smali",
            ".class public Lblarg; .super Ljava/lang/Object;.method public static blort(ILjava/lang/String;)V\n    .locals 0\n    return-void\n.end method\n",
        )

        val text = "public class blah { public static void something() {blarg.bl<ref>ort(10, \"bob\");}}"

        val methodReference = checkNotNull(configureByFileText(text, "blah.java"))

        val resolvedMethod = checkNotNull(methodReference.resolve() as? PsiMethod)
        Assert.assertEquals("blort", resolvedMethod.name)
        Assert.assertNotNull(resolvedMethod.containingClass)
        Assert.assertEquals("blarg", checkNotNull(resolvedMethod.containingClass).qualifiedName)
        Assert.assertEquals(2, resolvedMethod.parameterList.parametersCount.toLong())
        Assert.assertEquals("int", resolvedMethod.parameterList.parameters[0].type.canonicalText)
        Assert.assertEquals(
            "java.lang.String",
            resolvedMethod.parameterList.parameters[1].type.canonicalText,
        )
        Assert.assertNotNull(resolvedMethod.returnType)
        Assert.assertEquals("void", checkNotNull(resolvedMethod.returnType).canonicalText)
    }

    override fun getTestProjectJdk(): Sdk {
        return IdeaTestUtil.getMockJdk21()
    }
}
