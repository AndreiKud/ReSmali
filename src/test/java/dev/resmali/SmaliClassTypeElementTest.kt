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
import dev.resmali.psi.impl.SmaliClass
import dev.resmali.psi.impl.SmaliClassTypeElement
import dev.resmali.psi.impl.SmaliFile
import org.junit.Assert

class SmaliClassTypeElementTest : LightJavaCodeInsightFixtureTestCase() {
    fun testGetType() {
        myFixture.addFileToProject(
            "my/blarg.smali",
            ".class public Lmy/blarg; .super Ljava/lang/Object;",
        )

        val text = ".class public Lmy/pkg/blah; .super Lmy/bl<ref>arg;"

        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            text.replace("<ref>", ""),
        ) as SmaliFile

        val typeElement = checkNotNull(file.findReferenceAt(text.indexOf("<ref>")) as? SmaliClassTypeElement)
        val type = typeElement.type

        Assert.assertEquals("blarg", typeElement.name)
        Assert.assertEquals("my.blarg", typeElement.canonicalText)
        Assert.assertEquals("blarg", type.className)
        Assert.assertEquals("my.blarg", type.canonicalText)

        var resolvedClass = typeElement.resolve() as? SmaliClass
        Assert.assertNotNull(resolvedClass)
        Assert.assertEquals("my.blarg", checkNotNull(resolvedClass).qualifiedName)

        resolvedClass = type.resolve() as? SmaliClass
        Assert.assertNotNull(resolvedClass)
        Assert.assertEquals("my.blarg", checkNotNull(resolvedClass).qualifiedName)
    }

    fun testSimpleInnerClass() {
        myFixture.addFileToProject(
            "Outer.java",
            "public class Outer {   public static class Inner {   }}",
        )

        val text = $$".class public Lsmali; .super LOuter$In<ref>ner;"

        val file = myFixture.addFileToProject("smali.smali", text.replace("<ref>", "")) as SmaliFile

        val typeElement = checkNotNull(file.findReferenceAt(text.indexOf("<ref>")) as? SmaliClassTypeElement)
        val type = typeElement.type

        Assert.assertEquals("Outer.Inner", typeElement.qualifiedName)
        Assert.assertEquals("Outer.Inner", type.canonicalText)
    }

    fun testInnerClassWithPackage() {
        myFixture.addFileToProject(
            "my/Outer.java",
            "package my;public class Outer {   public static class Inner {   }}",
        )

        val text = $$".class public Lsmali; .super Lmy/Outer$In<ref>ner;"

        val file = myFixture.addFileToProject("smali.smali", text.replace("<ref>", "")) as SmaliFile

        val typeElement = checkNotNull(file.findReferenceAt(text.indexOf("<ref>")) as? SmaliClassTypeElement)
        val type = typeElement.type

        Assert.assertEquals("my.Outer.Inner", typeElement.qualifiedName)
        Assert.assertEquals("my.Outer.Inner", type.canonicalText)
    }

    fun testComplexInnerClass() {
        myFixture.addFileToProject(
            $$"my/Outer$blah.java",
            $$"package my;public class Outer$blah {   public static class Inner {   }   public static class Inner$blah {   }}",
        )

        var text = $$".class public Lsmali; .super Lmy/Outer$blah$In<ref>ner$blah;"

        var file = myFixture.addFileToProject("smali.smali", text.replace("<ref>", "")) as SmaliFile

        var typeElement = checkNotNull(file.findReferenceAt(text.indexOf("<ref>")) as? SmaliClassTypeElement)
        var type = typeElement.type

        Assert.assertEquals($$"my.Outer$blah.Inner$blah", typeElement.qualifiedName)
        Assert.assertEquals($$"my.Outer$blah.Inner$blah", type.canonicalText)

        text = $$".class public Lsmali2; .super Lmy/Outer$blah$In<ref>ner;"

        file = myFixture.addFileToProject("smali2.smali", text.replace("<ref>", "")) as SmaliFile

        typeElement = checkNotNull(file.findReferenceAt(text.indexOf("<ref>")) as? SmaliClassTypeElement)
        type = typeElement.type

        Assert.assertEquals($$"my.Outer$blah.Inner", typeElement.qualifiedName)
        Assert.assertEquals($$"my.Outer$blah.Inner", type.canonicalText)
    }

    fun testInnerClassTrailingDollar() {
        myFixture.addFileToProject(
            $$"my/Outer$blah.java",
            "package my;public class Outer$ {   public static class Inner$ {   }}",
        )

        val text = $$$".class public Lsmali; .super Lmy/Outer$$In<ref>ner$;"

        val file = myFixture.addFileToProject("smali.smali", text.replace("<ref>", "")) as SmaliFile

        val typeElement = checkNotNull(file.findReferenceAt(text.indexOf("<ref>")) as? SmaliClassTypeElement)
        val type = typeElement.type

        Assert.assertEquals("my.Outer$.Inner$", typeElement.qualifiedName)
        Assert.assertEquals("my.Outer$.Inner$", type.canonicalText)
    }
}
