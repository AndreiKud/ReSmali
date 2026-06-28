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

import com.intellij.psi.PsiAnnotationOwner
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import dev.resmali.psi.impl.SmaliClass
import dev.resmali.psi.impl.SmaliFile
import dev.resmali.psi.impl.SmaliLiteral
import org.junit.Assert

private val testAnnotationSource = """
    .class public interface abstract annotation Lmy/TestAnnotation;
    .super Ljava/lang/Object;
    .implements Ljava/lang/annotation/Annotation;

    .method public abstract testBooleanValue()Z
    .end method

    .method public abstract testStringArrayValue()[Ljava/lang/String;
    .end method

    .method public abstract testStringValue()Ljava/lang/String;
    .end method
""".trimIndent()

private val testAnnotation2Source = """
    .class public interface abstract annotation Lmy/TestAnnotation2;
    .super Ljava/lang/Object;
    .implements Ljava/lang/annotation/Annotation;
""".trimIndent()

class SmaliAnnotationTest : LightJavaCodeInsightFixtureTestCase() {
    // TODO: test default values
    fun testClassAnnotation() {
        myFixture.addFileToProject(
            "my/TestAnnotation.smali",
            testAnnotationSource,
        )

        myFixture.addFileToProject(
            "my/TestAnnotation2.smali",
            testAnnotation2Source,
        )

        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            """
                .class public Lmy/pkg/blah; .super Ljava/lang/Object;

                .annotation runtime Lmy/TestAnnotation;
                    testBooleanValue = true
                    testStringValue = "blah"
                    testStringArrayValue = {
                        "blah1",
                        "blah2"
                    }
                .end annotation

                .annotation runtime Lmy/TestAnnotation2;
                .end annotation
            """.trimIndent(),
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("my.pkg.blah", smaliClass.qualifiedName)

        doTest(smaliClass)
    }

    fun testFieldAnnotation() {
        myFixture.addFileToProject(
            "my/TestAnnotation.smali",
            testAnnotationSource,
        )

        myFixture.addFileToProject(
            "my/TestAnnotation2.smali",
            testAnnotation2Source,
        )

        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            """
                .class public Lmy/pkg/blah; .super Ljava/lang/Object;

                .field public myField:I
                    .annotation runtime Lmy/TestAnnotation;
                        testBooleanValue = true
                        testStringValue = "blah"
                        testStringArrayValue = {
                            "blah1",
                            "blah2"
                        }
                    .end annotation
                    .annotation runtime Lmy/TestAnnotation2;
                    .end annotation
                .end field
            """.trimIndent(),
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("my.pkg.blah", smaliClass.qualifiedName)

        val field = smaliClass.findFieldByName("myField", false)
        doTest(checkNotNull(field as? PsiAnnotationOwner))
    }

    fun testMethodAnnotation() {
        myFixture.addFileToProject(
            "my/TestAnnotation.smali",
            testAnnotationSource,
        )

        myFixture.addFileToProject(
            "my/TestAnnotation2.smali",
            testAnnotation2Source,
        )

        val file = myFixture.addFileToProject(
            "my/pkg/blah.smali",
            """
                .class public Lmy/pkg/blah; .super Ljava/lang/Object;

                .method public myMethod()V
                    .annotation runtime Lmy/TestAnnotation;
                        testBooleanValue = true
                        testStringValue = "blah"
                        testStringArrayValue = {
                            "blah1",
                            "blah2"
                        }
                    .end annotation
                    .annotation runtime Lmy/TestAnnotation2;
                    .end annotation
                .end method
            """.trimIndent(),
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        Assert.assertEquals("my.pkg.blah", smaliClass.qualifiedName)

        val method = smaliClass.methods[0]
        doTest(method)
    }

    fun doTest(annotationOwner: PsiAnnotationOwner) {
        Assert.assertEquals(2, annotationOwner.annotations.size.toLong())
        Assert.assertTrue(annotationOwner.hasAnnotations())
        Assert.assertTrue(annotationOwner.hasAnnotation("my.TestAnnotation"))
        Assert.assertFalse(annotationOwner.hasAnnotation("my.MissingAnnotation"))

        Assert.assertEquals("my.TestAnnotation", annotationOwner.annotations[0].qualifiedName)
        var annotationNameRef = annotationOwner.annotations[0].nameReferenceElement
        Assert.assertNotNull(annotationNameRef)
        var smaliAnnotationClass = checkNotNull(annotationNameRef).resolve() as? SmaliClass
        Assert.assertNotNull(smaliAnnotationClass)
        Assert.assertEquals("my.TestAnnotation", checkNotNull(smaliAnnotationClass).qualifiedName)

        Assert.assertEquals("my.TestAnnotation2", annotationOwner.annotations[1].qualifiedName)
        annotationNameRef = annotationOwner.annotations[1].nameReferenceElement
        Assert.assertNotNull(annotationNameRef)
        smaliAnnotationClass = checkNotNull(annotationNameRef).resolve() as? SmaliClass
        Assert.assertNotNull(smaliAnnotationClass)
        Assert.assertEquals("my.TestAnnotation2", checkNotNull(smaliAnnotationClass).qualifiedName)

        var smaliAnnotation = annotationOwner.findAnnotation("my.TestAnnotation")
        Assert.assertNotNull(smaliAnnotation)
        Assert.assertEquals("my.TestAnnotation", checkNotNull(smaliAnnotation).qualifiedName)
        var owner = smaliAnnotation.owner
        Assert.assertNotNull(owner)
        Assert.assertSame(annotationOwner, owner)
        annotationNameRef = smaliAnnotation.nameReferenceElement
        Assert.assertNotNull(annotationNameRef)
        smaliAnnotationClass = checkNotNull(annotationNameRef).resolve() as? SmaliClass
        Assert.assertNotNull(smaliAnnotationClass)
        Assert.assertEquals("my.TestAnnotation", checkNotNull(smaliAnnotationClass).qualifiedName)

        var parameterList = smaliAnnotation.parameterList
        Assert.assertNotNull(parameterList)
        Assert.assertEquals(3, parameterList.attributes.size.toLong())
        Assert.assertEquals("testBooleanValue", parameterList.attributes[0].name)
        var value = parameterList.attributes[0].value
        Assert.assertNotNull(value)
        // TODO: test the values rather than the text
        Assert.assertEquals("true", checkNotNull(value).text)
        Assert.assertEquals("testStringValue", parameterList.attributes[1].name)
        value = parameterList.attributes[1].value
        Assert.assertNotNull(value)
        Assert.assertEquals("\"blah\"", checkNotNull(value).text)
        Assert.assertEquals("testStringArrayValue", parameterList.attributes[2].name)
        value = parameterList.attributes[2].value
        Assert.assertNotNull(value)

        // TODO: test the individual values, once the array literal stuff is implemented
        value = smaliAnnotation.findAttributeValue("testBooleanValue")
        Assert.assertNotNull(value)
        Assert.assertEquals("true", checkNotNull(value).text)

        value = smaliAnnotation.findAttributeValue("testStringValue")
        Assert.assertNotNull(value)
        Assert.assertEquals("\"blah\"", checkNotNull(value).text)

        value = smaliAnnotation.findAttributeValue("testStringArrayValue")
        Assert.assertNotNull(value)

        // TODO: test findAttributeValue vs findDeclaredAttributeValue for default values
        smaliAnnotation = annotationOwner.findAnnotation("my.TestAnnotation2")
        Assert.assertNotNull(smaliAnnotation)
        Assert.assertEquals("my.TestAnnotation2", checkNotNull(smaliAnnotation).qualifiedName)
        owner = smaliAnnotation.owner
        Assert.assertNotNull(owner)
        Assert.assertSame(annotationOwner, owner)
        annotationNameRef = smaliAnnotation.nameReferenceElement
        Assert.assertNotNull(annotationNameRef)
        smaliAnnotationClass = checkNotNull(annotationNameRef).resolve() as? SmaliClass
        Assert.assertNotNull(smaliAnnotationClass)
        Assert.assertEquals("my.TestAnnotation2", checkNotNull(smaliAnnotationClass).qualifiedName)

        parameterList = smaliAnnotation.parameterList
        Assert.assertNotNull(parameterList)
        Assert.assertEquals(0, parameterList.attributes.size.toLong())
    }

    fun testDefaultValue() {
        val file = myFixture.addFileToProject(
            "AnnotationWithDefaultValue.smali",
            """
                .class public abstract interface annotation LAnnotationWithValues;
                .super Ljava/lang/Object;
                .implements Ljava/lang/annotation/Annotation;

                .method public abstract intValue()I
                .end method

                .annotation system Ldalvik/annotation/AnnotationDefault;
                    value = .subannotation LAnnotationWithValues;
                                intValue = 4
                            .end subannotation
                .end annotation

            """.trimIndent() + "\n\n",
        ) as SmaliFile

        val smaliClass = checkNotNull(file.psiClass)
        val method = smaliClass.methods[0]
        Assert.assertEquals("intValue", method.name)

        val defaultValue = method.defaultValue
        Assert.assertTrue(defaultValue is SmaliLiteral)
        Assert.assertEquals(4, (defaultValue as SmaliLiteral).integralValue)
    }
}
