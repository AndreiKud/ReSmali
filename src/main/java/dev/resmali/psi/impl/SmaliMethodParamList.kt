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
package dev.resmali.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiParameterList
import dev.resmali.psi.SmaliStubElementTypes
import dev.resmali.psi.stub.SmaliMethodParamListStub

class SmaliMethodParamList : SmaliStubBasedPsiElement<SmaliMethodParamListStub>, PsiParameterList {
    constructor(stub: SmaliMethodParamListStub) : super(stub, SmaliStubElementTypes.METHOD_PARAM_LIST)

    constructor(node: ASTNode) : super(node)

    override fun getParameters(): Array<SmaliMethodParameter> {
        return stubOrPsiChildren(
            SmaliStubElementTypes.METHOD_PARAMETER, emptyArray(),
        )
    }

    override fun getParameterIndex(parameter: PsiParameter): Int {
        if (parameter !is SmaliMethodParameter) {
            return -1
        }
        return parameters.indexOf(parameter)
    }

    override fun getParametersCount(): Int {
        return parameters.size
    }

    val parameterRegisterCount: Int
        /**
         * Returns the number of registers needed for the parameters in this parameter list
         * 
         * Note: this does *not* include the implicit "this" parameter, if applicable
         */
        get() {
            var count = 0
            for (param in parameters) {
                count += param.registerCount
            }
            return count
        }
}
