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

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import dev.resmali.psi.impl.SmaliFile
import org.junit.Assert

class SmaliImplementsExtendsTest : LightJavaCodeInsightFixtureTestCase() {
    fun testNormalClass() {
        myFixture.addFileToProject(
            "my/pkg/base.smali",
            ".class public Lmy/pkg/base; .super Ljava/lang/Object;",
        )
        myFixture.addFileToProject(
            "my/pkg/iface.smali",
            ".class public Lmy/pkg/iface; .super Ljava/lang/Object;",
        )
        myFixture.addFileToProject(
            "my/pkg/iface2.smali",
            ".class public Lmy/pkg/iface2; .super Ljava/lang/Object;",
        )

        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public Lmy/pkg/blah; .implements Lmy/pkg/iface; .super Lmy/pkg/base; .implements Lmy/pkg/iface2;",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        val extendsList = smaliClass.extendsList
        Assert.assertEquals(1, extendsList.referencedTypes.size.toLong())
        Assert.assertEquals("my.pkg.base", extendsList.referencedTypes[0].canonicalText)
        Assert.assertEquals(1, extendsList.referenceNames.size.toLong())
        Assert.assertEquals("my.pkg.base", extendsList.referenceNames[0])
        Assert.assertEquals(1, smaliClass.extendsListTypes.size.toLong())
        Assert.assertEquals("my.pkg.base", smaliClass.extendsListTypes[0].canonicalText)

        val resolvedSuper = checkNotNull(extendsList.referencedTypes[0].resolve())
        Assert.assertEquals("my.pkg.base", resolvedSuper.qualifiedName)

        val implementsList = smaliClass.implementsList
        Assert.assertEquals(2, implementsList.referencedTypes.size.toLong())
        Assert.assertEquals("my.pkg.iface", implementsList.referencedTypes[0].canonicalText)
        Assert.assertEquals("my.pkg.iface2", implementsList.referencedTypes[1].canonicalText)
        Assert.assertEquals(2, implementsList.referenceNames.size.toLong())
        Assert.assertEquals("my.pkg.iface", implementsList.referenceNames[0])
        Assert.assertEquals("my.pkg.iface2", implementsList.referenceNames[1])
        Assert.assertEquals(2, smaliClass.implementsListTypes.size.toLong())
        Assert.assertEquals("my.pkg.iface", smaliClass.implementsListTypes[0].canonicalText)
        Assert.assertEquals("my.pkg.iface2", smaliClass.implementsListTypes[1].canonicalText)

        var resolvedInterface = implementsList.referencedTypes[0].resolve()
        Assert.assertNotNull(resolvedInterface)
        Assert.assertEquals("my.pkg.iface", checkNotNull(resolvedInterface).qualifiedName)

        resolvedInterface = implementsList.referencedTypes[1].resolve()
        Assert.assertNotNull(resolvedInterface)
        Assert.assertEquals("my.pkg.iface2", checkNotNull(resolvedInterface).qualifiedName)
    }

    fun testInterface() {
        myFixture.addFileToProject(
            "my/pkg/iface.smali",
            ".class public Lmy/pkg/iface; .super Ljava/lang/Object;",
        )
        myFixture.addFileToProject(
            "my/pkg/iface2.smali",
            ".class public Lmy/pkg/iface2; .super Ljava/lang/Object;",
        )

        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            ".class public interface Lmy/pkg/blah; .implements Lmy/pkg/iface; .super Ljava/lang/Object; .implements Lmy/pkg/iface2;",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        val extendsList = smaliClass.extendsList

        Assert.assertEquals(2, extendsList.referencedTypes.size.toLong())
        Assert.assertEquals("my.pkg.iface", extendsList.referencedTypes[0].canonicalText)
        Assert.assertEquals("my.pkg.iface2", extendsList.referencedTypes[1].canonicalText)
        Assert.assertEquals(2, extendsList.referenceNames.size.toLong())
        Assert.assertEquals("my.pkg.iface", extendsList.referenceNames[0])
        Assert.assertEquals("my.pkg.iface2", extendsList.referenceNames[1])
        Assert.assertEquals(2, smaliClass.extendsListTypes.size.toLong())
        Assert.assertEquals("my.pkg.iface", smaliClass.extendsListTypes[0].canonicalText)
        Assert.assertEquals("my.pkg.iface2", smaliClass.extendsListTypes[1].canonicalText)

        var resolvedInterface = extendsList.referencedTypes[0].resolve()
        Assert.assertNotNull(resolvedInterface)
        Assert.assertEquals("my.pkg.iface", checkNotNull(resolvedInterface).qualifiedName)

        resolvedInterface = extendsList.referencedTypes[1].resolve()
        Assert.assertNotNull(resolvedInterface)
        Assert.assertEquals("my.pkg.iface2", checkNotNull(resolvedInterface).qualifiedName)

        val implementsList = smaliClass.implementsList
        Assert.assertEquals(0, implementsList.referencedTypes.size.toLong())
        Assert.assertEquals(0, implementsList.referenceNames.size.toLong())
        Assert.assertEquals(0, smaliClass.implementsListTypes.size.toLong())
    }
}
