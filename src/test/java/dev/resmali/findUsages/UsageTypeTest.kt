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
package dev.resmali.findUsages

import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.testFramework.JavaPsiTestCase
import com.intellij.usages.impl.rules.UsageType
import org.junit.Assert
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class UsageTypeTest(private val usageTypeProviderFactory: UsageTypeProviderFactory) : JavaPsiTestCase() {
    @Throws(Exception::class)
    protected fun doTest(fileName: String, text: String, vararg expectedUsageTypes: Any) {
        Assert.assertTrue(expectedUsageTypes.size % 2 == 0)

        val expectedUsageTypesMap = mutableMapOf<Int, UsageType>()
        var i = 0
        while (i < expectedUsageTypes.size) {
            expectedUsageTypesMap[expectedUsageTypes[i] as Int] = expectedUsageTypes[i + 1] as UsageType
            i += 2
        }

        val psiFile = createFile(fileName, REF_PATTERN.matcher(text).replaceAll(""))
        val refIndexMap = getRefIndexes(text)

        val usageTypeProvider = usageTypeProviderFactory.create()
        for ((refId, index) in refIndexMap) {

            var reference = psiFile.firstChild.findReferenceAt(index)
            Assert.assertNotNull(reference)
            if (reference is PsiMultiReference) {
                // If there are multiple reference parents, the default seems to be the last one,
                // i.e. the highest parent. We actually want the lowest one here.
                reference = reference.references[0]
            }

            val usageType = checkNotNull(usageTypeProvider.getUsageType(checkNotNull(reference).element))
            Assert.assertSame(expectedUsageTypesMap[refId], usageType)
            expectedUsageTypesMap.remove(refId)
        }
        Assert.assertTrue(expectedUsageTypesMap.isEmpty())
    }

    private fun getRefIndexes(text: String): MutableMap<Int, Int> {
        val m: Matcher = REF_PATTERN.matcher(text)
        var correction = 0
        val refIndexes = mutableMapOf<Int, Int>()
        while (m.find()) {
            val refId = m.group("id").toInt()
            refIndexes[refId] = m.start() - correction
            correction += m.end() - m.start()
        }
        return refIndexes
    }

    companion object {
        // e.g. <ref:1>, <ref:1234>, etc.
        private val REF_PATTERN: Pattern = Pattern.compile("<ref:(?<id>[0-9]+)>")
    }
}
