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
import com.intellij.psi.JavaPsiFacade
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import dev.resmali.psi.impl.SmaliFile
import org.junit.Assert

class SmaliClassTest : LightJavaCodeInsightFixtureTestCase() {
    fun testName() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public Lmy/pkg/blah; .super Ljava/lang/Object;",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("my.pkg.blah", smaliClass.qualifiedName)
        Assert.assertEquals("my.pkg", smaliClass.packageName)
        Assert.assertEquals("blah", smaliClass.name)
    }

    fun testEmptyPackageName() {
        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public Lblah; .super Ljava/lang/Object;",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("blah", smaliClass.qualifiedName)
        Assert.assertEquals("", smaliClass.packageName)
    }

    fun testGetSuperclass() {
        myFixture.addFileToProject(
            "base.smali",
            ".class public interface Lbase; .super Ljava/lang/Object;",
        )

        myFixture.addFileToProject(
            "iface.smali",
            ".class public interface Liface; .super Ljava/lang/Object;",
        )

        val file = myFixture.addFileToProject(
            "blah.smali",
            ".class public Lblah; .super Lbase; .implements Liface;",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("blah", smaliClass.qualifiedName)
        val superClass = checkNotNull(smaliClass.superClass)
        Assert.assertEquals("base", superClass.qualifiedName)

        Assert.assertEquals(2, smaliClass.supers.size.toLong())
        Assert.assertEquals("base", smaliClass.supers[0].qualifiedName)
        Assert.assertEquals("iface", smaliClass.supers[1].qualifiedName)

        Assert.assertEquals(2, smaliClass.superTypes.size.toLong())
        Assert.assertEquals("base", smaliClass.superTypes[0].canonicalText)
        Assert.assertEquals("iface", smaliClass.superTypes[1].canonicalText)

        Assert.assertEquals(1, smaliClass.interfaces.size.toLong())
        Assert.assertEquals("iface", smaliClass.interfaces[0].qualifiedName)
    }

    fun testGetSuperclassForInterface() {
        myFixture.addFileToProject(
            "iface.smali",
            ".class public interface Liface; .super Ljava/lang/Object;",
        )

        val file = myFixture.addFileToProject(
            "blah.smali",
            ".class public interface Lblah; .super Ljava/lang/Object; .implements Liface;",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("blah", smaliClass.qualifiedName)
        val superClass = checkNotNull(smaliClass.superClass)
        Assert.assertEquals("java.lang.Object", superClass.qualifiedName)

        Assert.assertEquals(2, smaliClass.supers.size.toLong())
        Assert.assertEquals("java.lang.Object", smaliClass.supers[0].qualifiedName)
        Assert.assertEquals("iface", smaliClass.supers[1].qualifiedName)

        Assert.assertEquals(1, smaliClass.superTypes.size.toLong())
        Assert.assertEquals("iface", smaliClass.superTypes[0].canonicalText)

        Assert.assertEquals(1, smaliClass.interfaces.size.toLong())
        Assert.assertEquals("iface", smaliClass.interfaces[0].qualifiedName)
    }

    fun testIsInheritor() {
        val file = myFixture.addFileToProject(
            "blah.smali",
            ".class public Lblah; .super Ljava/lang/Exception;",
        ) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("blah", smaliClass.qualifiedName)

        val factory = JavaPsiFacade.getInstance(project).elementFactory
        val throwableType = factory.createTypeByFQClassName("java.lang.Throwable", file.resolveScope)
        val throwableClass = checkNotNull(throwableType.resolve())

        val exceptionType = factory.createTypeByFQClassName("java.lang.Exception", file.resolveScope)
        val exceptionClass = checkNotNull(exceptionType.resolve())

        val objectType = factory.createTypeByFQClassName("java.lang.Object", file.resolveScope)
        val objectClass = checkNotNull(objectType.resolve())

        Assert.assertTrue(smaliClass.isInheritor(exceptionClass, true))
        Assert.assertTrue(smaliClass.isInheritor(throwableClass, true))
        Assert.assertTrue(smaliClass.isInheritor(objectClass, true))

        Assert.assertTrue(smaliClass.isInheritorDeep(exceptionClass, null))
        Assert.assertTrue(smaliClass.isInheritorDeep(throwableClass, null))
        Assert.assertTrue(smaliClass.isInheritorDeep(objectClass, null))

        Assert.assertTrue(smaliClass.isInheritor(exceptionClass, false))
        Assert.assertFalse(smaliClass.isInheritor(throwableClass, false))
        Assert.assertFalse(smaliClass.isInheritor(objectClass, false))
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        return object : DefaultLightProjectDescriptor() {
            override fun getSdk(): Sdk {
                return IdeaTestUtil.getMockJdk21()
            }
        }
    }
}
