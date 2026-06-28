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

class ParserTest : LightCodeInsightParsingTestCase("", "smalidea", SmaliLanguage.INSTANCE) {
    override fun getTestDataPath(): String {
        return "testData"
    }

    @Throws(Exception::class)
    fun testEmpty() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testFieldAnnotations() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidAnnotation() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidClassDirective() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidClassDirective2() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidClassDirective3() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidEnumLiteral() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidField() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidField2() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidField3() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidField4() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidInstruction() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidLocal() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testParamListInvalidParameter() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testSuperClassInvalidSyntax() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testSuperClassInvalidSyntax2() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidMethodReference() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidParameter() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidMethod() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidMethod2() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidMethod3() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testInvalidMethod4() {
        doTest(true)
    }

    @Throws(Exception::class)
    fun testMissingDotDot() {
        doTest(true)
    }
}
