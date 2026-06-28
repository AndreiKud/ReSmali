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

import com.intellij.psi.PsiAnnotationMethod
import com.intellij.testFramework.JavaResolveTestCase
import org.junit.Assert

private val annotationWithValues = """
    .class public abstract interface annotation LAnnotationWithValues;
    .super Ljava/lang/Object;
    .implements Ljava/lang/annotation/Annotation;

    .method public abstract intValue()I
    .end method
""".trimIndent()

class AnnotationElementNameReferenceTest : JavaResolveTestCase() {
    @Throws(Exception::class)
    fun testSmaliReferenceFromSmali() {
        createFile(
            "AnnotationWithValues.smali",
            annotationWithValues,
        )

        val reference = configureByFileText(
            ".class public Lblah;\n.super Ljava/lang/Object;\n.annotation runtime LAnnotationWithValues;\n  int<ref>Value = 123\n.end annotation",
            "blah.smali",
        )

        val resolved = checkNotNull(reference.resolve())
        Assert.assertTrue(resolved is PsiAnnotationMethod)
        Assert.assertEquals("intValue", (resolved as PsiAnnotationMethod).name)
        Assert.assertEquals(
            "AnnotationWithValues",
            checkNotNull(resolved.containingClass).qualifiedName,
        )
    }

    @Throws(Exception::class)
    fun testJavaReferenceFromSmali() {
        createFile(
            "AnnotationWithValues.java",
            "public @interface AnnotationWithValues {\n    int intValue();\n}",
        )

        val reference = configureByFileText(
            ".class public Lblah;\n.super Ljava/lang/Object;\n.annotation runtime LAnnotationWithValues;\n  int<ref>Value = 123\n.end annotation",
            "blah.smali",
        )

        val resolved = checkNotNull(reference.resolve())
        Assert.assertTrue(resolved is PsiAnnotationMethod)
        Assert.assertEquals("intValue", (resolved as PsiAnnotationMethod).name)
        Assert.assertEquals(
            "AnnotationWithValues",
            checkNotNull(resolved.containingClass).qualifiedName,
        )
    }

    @Throws(Exception::class)
    fun testSmaliReferenceFromJava() {
        createFile(
            "AnnotationWithValues.smali",
            annotationWithValues,
        )

        val reference = configureByFileText(
            "@AnnotationWithValues(int<ref>Value=123)\npublic class blah {}",
            "blah.java",
        )

        val resolved = checkNotNull(reference.resolve())
        Assert.assertTrue(resolved is PsiAnnotationMethod)
        Assert.assertEquals("intValue", (resolved as PsiAnnotationMethod).name)
        Assert.assertEquals(
            "AnnotationWithValues",
            checkNotNull(resolved.containingClass).qualifiedName,
        )
    }
}
