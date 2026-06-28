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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.search.LowLevelSearchUtil
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.SingleTargetRequestResultProcessor
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.Processor
import com.intellij.util.text.StringSearcher
import dev.resmali.SmaliFileType
import dev.resmali.util.NameUtils

internal class SmaliClassReferenceSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>() {
    override fun processQuery(
        queryParameters: ReferencesSearch.SearchParameters,
        consumer: Processor<in PsiReference>,
    ) {
        val element = queryParameters.elementToSearch
        if (element !is PsiClass) {
            return
        }
        val project = PsiUtilCore.getProjectInReadAction(element)
        val processor = SingleTargetRequestResultProcessor(element)

        val smaliType = ApplicationManager.getApplication().runReadAction<String?> {
            element.qualifiedName?.let { NameUtils.javaToSmaliType(element) }
        } ?: return

        val stringSearcher = StringSearcher(smaliType, true, true, false, false)

        val querySearchScope = ApplicationManager.getApplication().runReadAction<SearchScope> {
            queryParameters.effectiveSearchScope
        }

        when (querySearchScope) {
            is LocalSearchScope -> for (scopeElement in querySearchScope.scope) {
                ApplicationManager.getApplication().runReadAction {
                    LowLevelSearchUtil.processElementsContainingWordInElement(
                        { psiElement, offsetInElement ->
                            processor.processTextOccurrence(psiElement, offsetInElement, consumer)
                        },
                        scopeElement, stringSearcher, true, EmptyProgressIndicator(),
                    )
                }
            }

            is GlobalSearchScope -> ApplicationManager.getApplication().runReadAction {
                val smaliVirtualFiles = FileTypeIndex.getFiles(SmaliFileType, querySearchScope)
                for (vf in smaliVirtualFiles) {
                    ProgressManager.checkCanceled()
                    val psiFile = PsiManager.getInstance(project).findFile(vf)
                    if (psiFile != null) {
                        LowLevelSearchUtil.processElementsContainingWordInElement(
                            { psiElement: PsiElement, offsetInElement: Int ->
                                processor.processTextOccurrence(
                                    psiElement, offsetInElement, consumer,
                                )
                            },
                            psiFile, stringSearcher, true, EmptyProgressIndicator(),
                        )
                    }
                }
            }

            else -> return
        }
    }
}
