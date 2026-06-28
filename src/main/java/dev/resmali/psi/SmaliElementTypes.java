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

package dev.resmali.psi;

import dev.resmali.psi.impl.*;

public interface SmaliElementTypes {
    SmaliCompositeElementType LITERAL = new SmaliCompositeElementType("LITERAL", SmaliLiteral::new);
    SmaliCompositeElementType SUPER_STATEMENT = new SmaliCompositeElementType("SUPER_STATEMENT", SmaliSuperStatement::new);
    SmaliCompositeElementType IMPLEMENTS_STATEMENT = new SmaliCompositeElementType("IMPLEMENTS_STATEMENT", SmaliImplementsStatement::new);
    SmaliCompositeElementType SOURCE_STATEMENT = new SmaliCompositeElementType("SOURCE_STATEMENT", SmaliSourceStatement::new);
    SmaliCompositeElementType REGISTERS_STATEMENT = new SmaliCompositeElementType("REGISTERS_STATEMENT", SmaliRegistersStatement::new);
    SmaliCompositeElementType REGISTER_REFERENCE = new SmaliCompositeElementType("REGISTER_REFERENCE", SmaliRegisterReference::new);
    SmaliCompositeElementType MEMBER_NAME = new SmaliCompositeElementType("MEMBER_NAME", SmaliMemberName::new);
    SmaliCompositeElementType LOCAL_NAME = new SmaliCompositeElementType("LOCAL_NAME", SmaliLocalName::new);
    SmaliCompositeElementType PARAMETER_STATEMENT = new SmaliCompositeElementType("PARAMETER_STATEMENT", SmaliParameterStatement::new);
    SmaliCompositeElementType FIELD_INITIALIZER = new SmaliCompositeElementType("FIELD_INITIALIZER", SmaliFieldInitializer::new);
    SmaliCompositeElementType INSTRUCTION = new SmaliCompositeElementType("INSTRUCTION", SmaliInstruction::new);
    SmaliCompositeElementType ANNOTATION_PARAMETER_LIST = new SmaliCompositeElementType("ANNOTATION_PARAMETER_LIST", SmaliAnnotationParameterList::new);
    SmaliCompositeElementType ANNOTATION_ELEMENT = new SmaliCompositeElementType("ANNOTATION_ELEMENT", SmaliAnnotationElement::new);
    SmaliCompositeElementType ANNOTATION_ELEMENT_NAME = new SmaliCompositeElementType("ANNOTATION_ELEMENT_NAME", SmaliAnnotationElementName::new);
    SmaliCompositeElementType FIELD_REFERENCE = new SmaliCompositeElementType("FIELD_REFERENCE", SmaliFieldReference::new);
    SmaliCompositeElementType METHOD_REFERENCE = new SmaliCompositeElementType("METHOD_REFERENCE", SmaliMethodReference::new);
    SmaliCompositeElementType METHOD_REFERENCE_PARAM_LIST = new SmaliCompositeElementType("METHOD_REFERENCE_PARAM_LIST", SmaliMethodReferenceParamList::new);
    SmaliCompositeElementType LABEL = new SmaliCompositeElementType("LABEL", SmaliLabel::new);
    SmaliCompositeElementType LABEL_REFERENCE = new SmaliCompositeElementType("LABEL_REFERENCE", SmaliLabelReference::new);
    SmaliCompositeElementType LINE_DEBUG_STATEMENT = new SmaliCompositeElementType("LINE_DEBUG_STATEMENT", SmaliLineDebugStatement::new);
    SmaliCompositeElementType LOCAL_DEBUG_STATEMENT = new SmaliCompositeElementType("LOCAL_DEBUG_STATEMENT", SmaliLocalDebugStatement::new);
    SmaliCompositeElementType END_LOCAL_DEBUG_STATEMENT = new SmaliCompositeElementType("END_LOCAL_DEBUG_STATEMENT", SmaliEndLocalDebugStatement::new);
    SmaliCompositeElementType RESTART_LOCAL_DEBUG_STATEMENT = new SmaliCompositeElementType("RESTART_LOCAL_DEBUG_STATEMENT", SmaliRestartLocalDebugStatement::new);
    SmaliCompositeElementType PROLOGUE_DEBUG_STATEMENT = new SmaliCompositeElementType("PROLOGUE_DEBUG_STATEMENT", SmaliPrologueDebugStatement::new);
    SmaliCompositeElementType EPILOGUE_DEBUG_STATEMENT = new SmaliCompositeElementType("EPILOGUE_DEBUG_STATEMENT", SmaliEpilogueDebugStatement::new);
    SmaliCompositeElementType SOURCE_DEBUG_STATEMENT = new SmaliCompositeElementType("SOURCE_DEBUG_STATEMENT", SmaliSourceDebugStatement::new);
    SmaliCompositeElementType PRIMITIVE_TYPE = new SmaliCompositeElementType("PRIMITIVE_TYPE", SmaliPrimitiveTypeElement::new);
    SmaliCompositeElementType CLASS_TYPE = new SmaliCompositeElementType("CLASS_TYPE", SmaliClassTypeElement::new);
    SmaliCompositeElementType ARRAY_TYPE = new SmaliCompositeElementType("ARRAY_TYPE", SmaliArrayTypeElement::new);
    SmaliCompositeElementType VOID_TYPE = new SmaliCompositeElementType("VOID_TYPE", SmaliVoidTypeElement::new);
    SmaliCompositeElementType CATCH_STATEMENT = new SmaliCompositeElementType("CATCH_STATEMENT", SmaliCatchStatement::new);
    SmaliCompositeElementType CATCH_ALL_STATEMENT = new SmaliCompositeElementType("CATCH_ALL_STATEMENT", SmaliCatchAllStatement::new);
    SmaliCompositeElementType PACKED_SWITCH_ELEMENT = new SmaliCompositeElementType("PACKED_SWITCH_ELEMENT", SmaliPackedSwitchElement::new);
    SmaliCompositeElementType SPARSE_SWITCH_ELEMENT = new SmaliCompositeElementType("SPARSE_SWITCH_ELEMENT", SmaliSparseSwitchElement::new);
    SmaliCompositeElementType ARRAY_DATA_ELEMENT = new SmaliCompositeElementType("ARRAY_DATA_ELEMENT", SmaliArrayDataElement::new);
}
