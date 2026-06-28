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
package dev.resmali.psi

import dev.resmali.psi.stub.element.SmaliAnnotationElementType
import dev.resmali.psi.stub.element.SmaliClassElementType
import dev.resmali.psi.stub.element.SmaliClassStatementElementType
import dev.resmali.psi.stub.element.SmaliExtendsListElementType
import dev.resmali.psi.stub.element.SmaliFieldElementType
import dev.resmali.psi.stub.element.SmaliFileElementType
import dev.resmali.psi.stub.element.SmaliImplementsListElementType
import dev.resmali.psi.stub.element.SmaliMethodElementType
import dev.resmali.psi.stub.element.SmaliMethodParamListElementType
import dev.resmali.psi.stub.element.SmaliMethodParameterElementType
import dev.resmali.psi.stub.element.SmaliMethodPrototypeElementType
import dev.resmali.psi.stub.element.SmaliModifierListElementType
import dev.resmali.psi.stub.element.SmaliThrowsListElementType

interface SmaliStubElementTypes {
    companion object {
        @JvmField
        val FILE: SmaliFileElementType = SmaliFileElementType.INSTANCE

        @JvmField
        val CLASS: SmaliClassElementType = SmaliClassElementType.INSTANCE

        @JvmField
        val FIELD: SmaliFieldElementType = SmaliFieldElementType.INSTANCE

        @JvmField
        val METHOD: SmaliMethodElementType = SmaliMethodElementType.INSTANCE

        @JvmField
        val CLASS_STATEMENT: SmaliClassStatementElementType = SmaliClassStatementElementType.INSTANCE

        @JvmField
        val METHOD_PROTOTYPE: SmaliMethodPrototypeElementType = SmaliMethodPrototypeElementType.INSTANCE

        @JvmField
        val METHOD_PARAM_LIST: SmaliMethodParamListElementType = SmaliMethodParamListElementType.INSTANCE

        @JvmField
        val METHOD_PARAMETER: SmaliMethodParameterElementType = SmaliMethodParameterElementType.INSTANCE

        @JvmField
        val ANNOTATION: SmaliAnnotationElementType = SmaliAnnotationElementType.INSTANCE

        @JvmField
        val MODIFIER_LIST: SmaliModifierListElementType = SmaliModifierListElementType.INSTANCE

        @JvmField
        val EXTENDS_LIST: SmaliExtendsListElementType = SmaliExtendsListElementType.INSTANCE

        @JvmField
        val IMPLEMENTS_LIST: SmaliImplementsListElementType = SmaliImplementsListElementType.INSTANCE

        @JvmField
        val THROWS_LIST: SmaliThrowsListElementType = SmaliThrowsListElementType.INSTANCE
    }
}
