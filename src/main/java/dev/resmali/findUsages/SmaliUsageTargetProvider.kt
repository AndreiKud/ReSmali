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
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.usages.UsageTarget
import com.intellij.usages.UsageTargetProvider
import dev.resmali.SmaliTokens
import dev.resmali.psi.impl.SmaliMemberName

/**
 * A usage target provider for smali member names consisting of primitive types.
 * 
 * For member names like IIII, the default logic to find the usage target doesn't work, due to the member
 * name being split up into multiple leaf tokens.
 */
internal class SmaliUsageTargetProvider : UsageTargetProvider, DumbAware {
    override fun getTargets(editor: Editor, file: PsiFile): Array<UsageTarget>? {
        val element = file.findElementAt(
            TargetElementUtilBase.adjustOffset(file, editor.document, editor.caretModel.offset),
        ) ?: return null
        return getTargets(element)
    }

    override fun getTargets(element: PsiElement): Array<UsageTarget>? {
        val node = element.node ?: return null

        if (node.elementType === SmaliTokens.PARAM_LIST_OR_ID_PRIMITIVE_TYPE) {
            val parent = element.parent
            if (parent is SmaliMemberName) {
                return arrayOf(PsiElement2UsageTargetAdapter(parent.parent, false))
            }
        }
        return null
    }
}
