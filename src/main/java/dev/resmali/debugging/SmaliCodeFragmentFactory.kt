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
package dev.resmali.debugging

import com.intellij.debugger.engine.evaluation.DefaultCodeFragmentFactory
import com.intellij.debugger.engine.evaluation.EvaluateException
import com.intellij.debugger.engine.evaluation.TextWithImports
import com.intellij.debugger.engine.evaluation.expression.EvaluatorBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.JavaCodeFragment
import com.intellij.psi.PsiElement
import dev.resmali.SmaliFileType
import dev.resmali.SmaliLanguage
import dev.resmali.debugging.utils.RegistersContext

internal class SmaliCodeFragmentFactory : DefaultCodeFragmentFactory() {
    override fun createPsiCodeFragmentImpl(
        item: TextWithImports,
        context: PsiElement?,
        project: Project,
    ): JavaCodeFragment? {
        return ApplicationManager.getApplication().runReadAction<JavaCodeFragment?> {
            val wrapped = RegistersContext.wrap(project, context)
            val fragment = super.createPsiCodeFragmentImpl(item, wrapped, project)
            val lazyValues = wrapped?.getUserData(RegistersContext.SMALI_LAZY_VALUES_KEY)
            if (lazyValues != null) {
                checkNotNull(fragment)
                fragment.putUserData(RegistersContext.SMALI_LAZY_VALUES_KEY, lazyValues)
            }
            fragment
        }
    }

    override fun isContextAccepted(contextElement: PsiElement?): Boolean {
        if (contextElement == null) {
            return false
        }
        return contextElement.language === SmaliLanguage.INSTANCE
    }

    override fun getFileType(): LanguageFileType {
        return SmaliFileType
    }

    override fun getEvaluatorBuilder(): EvaluatorBuilder {
        val builder = super.evaluatorBuilder
        return EvaluatorBuilder { codeFragment, position ->
            val evaluator = ApplicationManager.getApplication().runReadAction(
                Computable {
                    try {
                        return@Computable builder.build(codeFragment, position)
                    } catch (e: EvaluateException) {
                        throw RuntimeException(e)
                    }
                },
            )
            SmaliExpressionEvaluator(codeFragment, evaluator)
        }
    }
}
