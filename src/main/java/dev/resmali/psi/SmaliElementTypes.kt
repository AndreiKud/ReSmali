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

import dev.resmali.psi.impl.SmaliAnnotationElement
import dev.resmali.psi.impl.SmaliAnnotationElementName
import dev.resmali.psi.impl.SmaliAnnotationParameterList
import dev.resmali.psi.impl.SmaliArrayDataElement
import dev.resmali.psi.impl.SmaliArrayTypeElement
import dev.resmali.psi.impl.SmaliCatchAllStatement
import dev.resmali.psi.impl.SmaliCatchStatement
import dev.resmali.psi.impl.SmaliClassTypeElement
import dev.resmali.psi.impl.SmaliEndLocalDebugStatement
import dev.resmali.psi.impl.SmaliEpilogueDebugStatement
import dev.resmali.psi.impl.SmaliFieldInitializer
import dev.resmali.psi.impl.SmaliFieldReference
import dev.resmali.psi.impl.SmaliImplementsStatement
import dev.resmali.psi.impl.SmaliInstruction
import dev.resmali.psi.impl.SmaliLabel
import dev.resmali.psi.impl.SmaliLabelReference
import dev.resmali.psi.impl.SmaliLineDebugStatement
import dev.resmali.psi.impl.SmaliLiteral
import dev.resmali.psi.impl.SmaliLocalDebugStatement
import dev.resmali.psi.impl.SmaliLocalName
import dev.resmali.psi.impl.SmaliMemberName
import dev.resmali.psi.impl.SmaliMethodReference
import dev.resmali.psi.impl.SmaliMethodReferenceParamList
import dev.resmali.psi.impl.SmaliPackedSwitchElement
import dev.resmali.psi.impl.SmaliParameterStatement
import dev.resmali.psi.impl.SmaliPrimitiveTypeElement
import dev.resmali.psi.impl.SmaliPrologueDebugStatement
import dev.resmali.psi.impl.SmaliRegisterReference
import dev.resmali.psi.impl.SmaliRegistersStatement
import dev.resmali.psi.impl.SmaliRestartLocalDebugStatement
import dev.resmali.psi.impl.SmaliSourceDebugStatement
import dev.resmali.psi.impl.SmaliSourceStatement
import dev.resmali.psi.impl.SmaliSparseSwitchElement
import dev.resmali.psi.impl.SmaliSuperStatement
import dev.resmali.psi.impl.SmaliVoidTypeElement

interface SmaliElementTypes {
    companion object {
        @JvmField
        val LITERAL: SmaliCompositeElementType = SmaliCompositeElementType("LITERAL") { SmaliLiteral() }

        @JvmField
        val SUPER_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("SUPER_STATEMENT") { SmaliSuperStatement() }

        @JvmField
        val IMPLEMENTS_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("IMPLEMENTS_STATEMENT") { SmaliImplementsStatement() }

        @JvmField
        val SOURCE_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("SOURCE_STATEMENT") { SmaliSourceStatement() }

        @JvmField
        val REGISTERS_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("REGISTERS_STATEMENT") { SmaliRegistersStatement() }

        @JvmField
        val REGISTER_REFERENCE: SmaliCompositeElementType = SmaliCompositeElementType("REGISTER_REFERENCE") { SmaliRegisterReference() }

        @JvmField
        val MEMBER_NAME: SmaliCompositeElementType = SmaliCompositeElementType("MEMBER_NAME") { SmaliMemberName() }

        @JvmField
        val LOCAL_NAME: SmaliCompositeElementType = SmaliCompositeElementType("LOCAL_NAME") { SmaliLocalName() }

        @JvmField
        val PARAMETER_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("PARAMETER_STATEMENT") { SmaliParameterStatement() }

        @JvmField
        val FIELD_INITIALIZER: SmaliCompositeElementType = SmaliCompositeElementType("FIELD_INITIALIZER") { SmaliFieldInitializer() }

        @JvmField
        val INSTRUCTION: SmaliCompositeElementType = SmaliCompositeElementType("INSTRUCTION") { SmaliInstruction() }

        @JvmField
        val ANNOTATION_PARAMETER_LIST: SmaliCompositeElementType = SmaliCompositeElementType("ANNOTATION_PARAMETER_LIST") { SmaliAnnotationParameterList() }

        @JvmField
        val ANNOTATION_ELEMENT: SmaliCompositeElementType = SmaliCompositeElementType("ANNOTATION_ELEMENT") { SmaliAnnotationElement() }

        @JvmField
        val ANNOTATION_ELEMENT_NAME: SmaliCompositeElementType = SmaliCompositeElementType("ANNOTATION_ELEMENT_NAME") { SmaliAnnotationElementName() }

        @JvmField
        val FIELD_REFERENCE: SmaliCompositeElementType = SmaliCompositeElementType("FIELD_REFERENCE") { SmaliFieldReference() }

        @JvmField
        val METHOD_REFERENCE: SmaliCompositeElementType = SmaliCompositeElementType("METHOD_REFERENCE") { SmaliMethodReference() }

        @JvmField
        val METHOD_REFERENCE_PARAM_LIST: SmaliCompositeElementType = SmaliCompositeElementType("METHOD_REFERENCE_PARAM_LIST") { SmaliMethodReferenceParamList() }

        @JvmField
        val LABEL: SmaliCompositeElementType = SmaliCompositeElementType("LABEL") { SmaliLabel() }

        @JvmField
        val LABEL_REFERENCE: SmaliCompositeElementType = SmaliCompositeElementType("LABEL_REFERENCE") { SmaliLabelReference() }

        @JvmField
        val LINE_DEBUG_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("LINE_DEBUG_STATEMENT") { SmaliLineDebugStatement() }

        @JvmField
        val LOCAL_DEBUG_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("LOCAL_DEBUG_STATEMENT") { SmaliLocalDebugStatement() }

        @JvmField
        val END_LOCAL_DEBUG_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("END_LOCAL_DEBUG_STATEMENT") { SmaliEndLocalDebugStatement() }

        @JvmField
        val RESTART_LOCAL_DEBUG_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("RESTART_LOCAL_DEBUG_STATEMENT") { SmaliRestartLocalDebugStatement() }

        @JvmField
        val PROLOGUE_DEBUG_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("PROLOGUE_DEBUG_STATEMENT") { SmaliPrologueDebugStatement() }

        @JvmField
        val EPILOGUE_DEBUG_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("EPILOGUE_DEBUG_STATEMENT") { SmaliEpilogueDebugStatement() }

        @JvmField
        val SOURCE_DEBUG_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("SOURCE_DEBUG_STATEMENT") { SmaliSourceDebugStatement() }

        @JvmField
        val PRIMITIVE_TYPE: SmaliCompositeElementType = SmaliCompositeElementType("PRIMITIVE_TYPE") { SmaliPrimitiveTypeElement() }

        @JvmField
        val CLASS_TYPE: SmaliCompositeElementType = SmaliCompositeElementType("CLASS_TYPE") { SmaliClassTypeElement() }

        @JvmField
        val ARRAY_TYPE: SmaliCompositeElementType = SmaliCompositeElementType("ARRAY_TYPE") { SmaliArrayTypeElement() }

        @JvmField
        val VOID_TYPE: SmaliCompositeElementType = SmaliCompositeElementType("VOID_TYPE") { SmaliVoidTypeElement() }

        @JvmField
        val CATCH_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("CATCH_STATEMENT") { SmaliCatchStatement() }

        @JvmField
        val CATCH_ALL_STATEMENT: SmaliCompositeElementType = SmaliCompositeElementType("CATCH_ALL_STATEMENT") { SmaliCatchAllStatement() }

        @JvmField
        val PACKED_SWITCH_ELEMENT: SmaliCompositeElementType = SmaliCompositeElementType("PACKED_SWITCH_ELEMENT") { SmaliPackedSwitchElement() }

        @JvmField
        val SPARSE_SWITCH_ELEMENT: SmaliCompositeElementType = SmaliCompositeElementType("SPARSE_SWITCH_ELEMENT") { SmaliSparseSwitchElement() }

        @JvmField
        val ARRAY_DATA_ELEMENT: SmaliCompositeElementType = SmaliCompositeElementType("ARRAY_DATA_ELEMENT") { SmaliArrayDataElement() }
    }
}
