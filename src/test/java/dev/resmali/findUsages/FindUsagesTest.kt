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

import com.intellij.codeInsight.TargetElementUtilBase
import com.intellij.find.FindManager
import com.intellij.find.impl.FindManagerImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.JavaPsiTestCase
import com.intellij.usageView.UsageInfo
import com.intellij.usages.PsiElementUsageTarget
import com.intellij.usages.UsageTargetUtil
import com.intellij.util.CommonProcessors
import org.junit.Assert
import java.util.regex.Pattern

abstract class FindUsagesTest : JavaPsiTestCase() {
    private inner class TestFile(val fileName: String, val fileText: String) {
        val psiFile: PsiFile = createFile(fileName, text)

        val text: String
            get() = fileText.replace(REF_TAG, "").replace(USAGE_TAG, "")

        val refIndex: Int
            get() = fileText.replace(USAGE_TAG, "").indexOf(REF_TAG)

        val usageIndices: List<Int>
            get() {
                val matcher = Pattern.compile(USAGE_TAG).matcher(fileText.replace(REF_TAG, ""))
                val matches = mutableListOf<Int>()

                var adjustment = 0
                while (matcher.find()) {
                    matches.add(matcher.start() - adjustment)
                    adjustment += USAGE_TAG.length
                }
                return matches
            }
    }

    private lateinit var testFiles: MutableList<TestFile>

    @Throws(Exception::class)
    public override fun setUp() {
        testFiles = mutableListOf()
        super.setUp()
    }

    @Throws(Exception::class)
    protected fun addFile(fileName: String, fileText: String) {
        testFiles += TestFile(fileName, fileText)
    }

    protected fun doTest() {
        val referenceFile = testFiles.first { it.refIndex != -1 }
        val refIndex = referenceFile.refIndex
        val element = checkNotNull(referenceFile.psiFile.findElementAt(refIndex))
        val targetElement = UsageTargetUtil.findUsageTargets(element).filterIsInstance<PsiElementUsageTarget>().firstOrNull()?.element
            ?: referenceFile.psiFile.findReferenceAt(refIndex)?.resolve() ?: TargetElementUtilBase.getNamedElement(element, 0)

        val usages = findUsages(checkNotNull(targetElement))
        for (testFile in testFiles) {
            assertUsages(testFile, usages)
        }
    }

    private fun assertUsages(testFile: TestFile, usages: MutableCollection<UsageInfo>) {
        val fileUsages = usages.filterTo(mutableListOf()) { it.file?.name == testFile.fileName }

        for (usageIndex in testFile.usageIndices) {
            val matchingUsage = fileUsages.indexOfFirst { usage ->
                val usageElement = checkNotNull(usage.element)
                usageIndex in usageElement.textRange.startOffset until usageElement.textRange.endOffset
            }
            Assert.assertTrue(matchingUsage >= 0)
            fileUsages.removeAt(matchingUsage)
        }
        Assert.assertEquals(0, fileUsages.size.toLong())
    }

    private fun findUsages(element: PsiElement): MutableCollection<UsageInfo> {
        val findUsagesManager = (FindManager.getInstance(project) as FindManagerImpl).findUsagesManager

        val findUsagesHandler = checkNotNull(findUsagesManager.getFindUsagesHandler(element, false))
        val options = findUsagesHandler.findUsagesOptions
        val processor = CommonProcessors.CollectProcessor<UsageInfo>()

        for (primaryElement in findUsagesHandler.primaryElements) {
            findUsagesHandler.processElementUsages(primaryElement, processor, options)
        }

        for (secondaryElement in findUsagesHandler.secondaryElements) {
            findUsagesHandler.processElementUsages(secondaryElement, processor, options)
        }

        return processor.results
    }

    companion object {
        const val USAGE_TAG: String = "<usage>"
        const val REF_TAG: String = "<ref>"
    }
}
