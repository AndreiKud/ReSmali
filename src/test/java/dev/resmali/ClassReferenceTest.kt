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
import com.intellij.psi.JavaResolveResult
import com.intellij.psi.PsiClass
import com.intellij.testFramework.DumbModeTestUtils.runInDumbModeSynchronously
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.JavaResolveTestCase
import dev.resmali.psi.impl.SmaliClass
import dev.resmali.psi.impl.SmaliClassTypeElement
import org.junit.Assert

class ClassReferenceTest : JavaResolveTestCase() {
    /**
     * Test a reference to a java class from a smali class
     */
    @Throws(Exception::class)
    fun testJavaReferenceFromSmali() {
        val typeElement = configureByFileText(
            ".class public Lblah; .super L<ref>java/lang/Object;", "blah.smali",
        ) as SmaliClassTypeElement

        Assert.assertNotNull(typeElement)
        Assert.assertEquals("Object", typeElement.name)

        val psiClass = checkNotNull(typeElement.resolve())
        Assert.assertEquals("java.lang.Object", psiClass.qualifiedName)

        val resolveResult = typeElement.advancedResolve(false)
        Assert.assertNotNull(resolveResult.element)
        Assert.assertEquals("java.lang.Object", (resolveResult.element as PsiClass).qualifiedName)

        val resolveResults: Array<JavaResolveResult> = typeElement.multiResolve(false)
        Assert.assertEquals(1, resolveResults.size.toLong())
        Assert.assertNotNull(resolveResults[0].element)
        Assert.assertEquals("java.lang.Object", (resolveResults[0].element as PsiClass).qualifiedName)
    }

    /**
     * Test a reference to a java class from a smali class, while in dumb mode
     */
    @Throws(Exception::class)
    fun testJavaReferenceFromSmaliInDumbMode() {
        val typeElement = configureByFileText(
            ".class public Lblah; .super L<ref>java/lang/Object;", "blah.smali",
        ) as SmaliClassTypeElement

        Assert.assertNotNull(typeElement)
        Assert.assertEquals("Object", typeElement.name)

        runInDumbModeSynchronously(project) {
            val psiClass = typeElement.resolve()
            Assert.assertNull(psiClass)
        }
    }

    /**
     * Test a reference to a smali class from a smali class
     */
    @Throws(Exception::class)
    fun testSmaliReferenceFromSmali() {
        createFile("blarg.smali", ".class public Lblarg; .super Ljava/lang/Object;")

        val typeElement = configureByFileText(
            ".class public Lblah; .super L<ref>blarg;", "blah.smali",
        ) as SmaliClassTypeElement

        Assert.assertEquals("blarg", typeElement.name)

        val smaliClass = checkNotNull(typeElement.resolve() as? SmaliClass)
        Assert.assertEquals("blarg", smaliClass.qualifiedName)

        val resolveResult = typeElement.advancedResolve(false)
        Assert.assertNotNull(resolveResult.element)
        Assert.assertEquals("blarg", (resolveResult.element as PsiClass).qualifiedName)

        val resolveResults: Array<JavaResolveResult> = typeElement.multiResolve(false)
        Assert.assertEquals(1, resolveResults.size.toLong())
        Assert.assertNotNull(resolveResults[0].element)
        Assert.assertEquals("blarg", (resolveResults[0].element as PsiClass).qualifiedName)
    }

    /**
     * Test a reference to a smali class from a java class
     */
    @Throws(Exception::class)
    fun testSmaliReferenceFromJava() {
        createFile("blarg.smali", ".class public Lblarg; .super Ljava/lang/Object;")

        val reference = configureByFileText(
            "public class blah extends bla<ref>rg { }", "blah.java",
        )

        val smaliClass = checkNotNull(reference.resolve() as? SmaliClass)
        Assert.assertEquals("blarg", smaliClass.qualifiedName)
    }

    override fun getTestProjectJdk(): Sdk {
        return IdeaTestUtil.getMockJdk21()
    }
}
