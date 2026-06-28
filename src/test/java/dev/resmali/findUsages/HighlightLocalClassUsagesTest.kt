/*
 * Copyright 2016, Google Inc.
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
package dev.resmali.findUsages

import com.intellij.codeInsight.TargetElementUtilBase
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.testFramework.JavaPsiTestCase
import org.junit.Assert

class HighlightLocalClassUsagesTest : JavaPsiTestCase() {
    @Throws(Exception::class)
    fun testHighlightLocalClassUsage() {
        val fileText = """
            .class public Lbl<ref>arg; .super Ljava/lang/Object;
            .method public doSomething()V
              .registers 1
              new-instance v0, Lbl<ref>arg;
              invoke-direct {v0}, Lblah;-><init>()V
              return-void
            .end method
        """.trimIndent()

        val file = createFile("blarg.smali", fileText.replace("<ref>", ""))
        val refIndex = fileText.indexOf("<ref>")
        val reference = file.findReferenceAt(refIndex)
        val target = if (reference != null) {
            reference.resolve()
        } else {
            TargetElementUtilBase.getNamedElement(file.findElementAt(refIndex), 0)
        }

        val scope = LocalSearchScope(file)
        val refs = ReferencesSearch.search(checkNotNull(target), scope).findAll().sortedBy { it.element.textOffset }
        Assert.assertEquals(2, refs.size.toLong())

        Assert.assertEquals(checkNotNull(file.findElementAt(refIndex)).textOffset, refs[0].element.textOffset)
        Assert.assertEquals(
            checkNotNull(file.findElementAt(fileText.replaceFirst("<ref>".toRegex(), "").indexOf("<ref>"))).textOffset,
            refs[1].element.textOffset,
        )
    }
}
