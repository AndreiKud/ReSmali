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

import com.android.tools.smali.dexlib2.Opcode
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReference
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProvider
import dev.resmali.SmaliTokens
import dev.resmali.psi.impl.SmaliAnnotation
import dev.resmali.psi.impl.SmaliCatchStatement
import dev.resmali.psi.impl.SmaliClassStatement
import dev.resmali.psi.impl.SmaliField
import dev.resmali.psi.impl.SmaliFieldReference
import dev.resmali.psi.impl.SmaliImplementsStatement
import dev.resmali.psi.impl.SmaliInstruction
import dev.resmali.psi.impl.SmaliLiteral
import dev.resmali.psi.impl.SmaliLocalDebugStatement
import dev.resmali.psi.impl.SmaliMethodParamList
import dev.resmali.psi.impl.SmaliMethodPrototype
import dev.resmali.psi.impl.SmaliMethodReference
import dev.resmali.psi.impl.SmaliMethodReferenceParamList
import dev.resmali.psi.impl.SmaliSuperStatement
import java.util.EnumSet

internal class SmaliUsageTypeProvider : UsageTypeProvider {
    override fun getUsageType(element: PsiElement): UsageType {
        val referenced = (element as? PsiReference)?.resolve()
        return when (referenced) {
            is PsiClass -> findClassUsageType(element)
            is PsiField -> findFieldUsageType(element)
            is PsiMethod -> findMethodUsageType(element)
            else -> UsageType.UNCLASSIFIED
        }
    }

    private val newArrayInstructions: Set<Opcode> = EnumSet.of(
        Opcode.FILLED_NEW_ARRAY, Opcode.NEW_ARRAY,
        Opcode.FILLED_NEW_ARRAY_RANGE,
    )

    private val fieldReadInstructions: Set<Opcode> = EnumSet.of(
        Opcode.IGET, Opcode.IGET_BOOLEAN, Opcode.IGET_BYTE,
        Opcode.IGET_CHAR, Opcode.IGET_OBJECT, Opcode.IGET_OBJECT_VOLATILE, Opcode.IGET_SHORT, Opcode.IGET_VOLATILE,
        Opcode.IGET_WIDE, Opcode.IGET_WIDE_VOLATILE, Opcode.SGET, Opcode.SGET_BOOLEAN, Opcode.SGET_BYTE,
        Opcode.SGET_CHAR, Opcode.SGET_OBJECT, Opcode.SGET_OBJECT_VOLATILE, Opcode.SGET_SHORT, Opcode.SGET_VOLATILE,
        Opcode.SGET_WIDE, Opcode.SGET_WIDE_VOLATILE,
    )

    private val fieldWriteInstructions: Set<Opcode> = EnumSet.of(
        Opcode.IPUT, Opcode.IPUT_BOOLEAN, Opcode.IPUT_BYTE,
        Opcode.IPUT_CHAR, Opcode.IPUT_OBJECT, Opcode.IPUT_OBJECT_VOLATILE, Opcode.IPUT_SHORT, Opcode.IPUT_VOLATILE,
        Opcode.IPUT_WIDE, Opcode.IPUT_WIDE_VOLATILE, Opcode.SPUT, Opcode.SPUT_BOOLEAN, Opcode.SPUT_BYTE,
        Opcode.SPUT_CHAR, Opcode.SPUT_OBJECT, Opcode.SPUT_OBJECT_VOLATILE, Opcode.SPUT_SHORT, Opcode.SPUT_VOLATILE,
        Opcode.SPUT_WIDE, Opcode.SPUT_WIDE_VOLATILE,
    )

    private fun findClassUsageType(element: PsiElement): UsageType {
        val originalElement = element
        var element: PsiElement? = element

        while (element != null) {
            if (element is SmaliFieldReference) {
                var prev = originalElement.prevSibling
                while (prev != null) {
                    // if the element is to the right of a colon, then it is the field type, otherwise it is
                    // the declaring class
                    if (prev.node.elementType === SmaliTokens.COLON) {
                        return Types.FIELD_TYPE_REFERENCE
                    }
                    prev = prev.prevSibling
                }
                return Types.FIELD_DECLARING_TYPE_REFERENCE
            } else if (element is SmaliMethodReferenceParamList) {
                return Types.METHOD_PARAM_REFERENCE
            } else if (element is SmaliMethodReference) {
                var prev = originalElement.prevSibling
                while (prev != null) {
                    val elementType = prev.node.elementType
                    // if the element is to the right of a close paren, then it is the return type,
                    // otherwise it is the declaring class. Any parameter type will be taken care of by the previous
                    // "if" for SmaliMethodReferenceParamList
                    if (elementType === SmaliTokens.CLOSE_PAREN) {
                        return Types.METHOD_RETURN_TYPE_REFERENCE
                    }
                    prev = prev.prevSibling
                }
                return Types.METHOD_DECLARING_TYPE_REFERENCE
            } else if (element is SmaliInstruction) {
                val opcode = element.opcode
                if (opcode == Opcode.INSTANCE_OF) {
                    return UsageType.CLASS_INSTANCE_OF
                } else if (opcode == Opcode.CHECK_CAST) {
                    return UsageType.CLASS_CAST_TO
                } else if (newArrayInstructions.contains(opcode)) {
                    return UsageType.CLASS_NEW_ARRAY
                } else if (opcode == Opcode.NEW_INSTANCE) {
                    return UsageType.CLASS_NEW_OPERATOR
                } else if (opcode == Opcode.CONST_CLASS) {
                    return UsageType.CLASS_CLASS_OBJECT_ACCESS
                } else if (opcode == Opcode.THROW_VERIFICATION_ERROR) {
                    return Types.VERIFICATION_ERROR
                }
            } else if (element is SmaliSuperStatement || element is SmaliImplementsStatement) {
                return UsageType.CLASS_EXTENDS_IMPLEMENTS_LIST
            } else if (element is SmaliClassStatement) {
                return Types.CLASS_DECLARATION
            } else if (element is SmaliMethodParamList) {
                return UsageType.CLASS_METHOD_PARAMETER_DECLARATION
            } else if (element is SmaliMethodPrototype) {
                return UsageType.CLASS_METHOD_RETURN_TYPE
            } else if (element is SmaliField) {
                return UsageType.CLASS_FIELD_DECLARATION
            } else if (element is SmaliCatchStatement) {
                return UsageType.CLASS_CATCH_CLAUSE_PARAMETER_DECLARATION
            } else if (element is SmaliLocalDebugStatement) {
                return UsageType.CLASS_LOCAL_VAR_DECLARATION
            } else if (element is SmaliAnnotation) {
                return UsageType.ANNOTATION
            } else if (element is SmaliLiteral) {
                return Types.LITERAL
            }
            element = element.parent
        }
        return UsageType.UNCLASSIFIED
    }

    private fun findFieldUsageType(element: PsiElement): UsageType {
        var element: PsiElement? = element

        while (element != null) {
            element = element.parent

            if (element is SmaliInstruction) {
                val opcode = element.opcode
                if (fieldReadInstructions.contains(opcode)) {
                    return UsageType.READ
                } else if (fieldWriteInstructions.contains(opcode)) {
                    return UsageType.WRITE
                } else if (opcode == Opcode.THROW_VERIFICATION_ERROR) {
                    return Types.VERIFICATION_ERROR
                }
            } else if (element is SmaliLiteral) {
                return Types.LITERAL
            }
        }
        return UsageType.UNCLASSIFIED
    }

    private fun findMethodUsageType(element: PsiElement): UsageType {
        var element: PsiElement? = element

        while (element != null) {
            element = element.parent

            if (element is SmaliInstruction) {
                val opcode = element.opcode
                if (opcode == Opcode.THROW_VERIFICATION_ERROR) {
                    return Types.VERIFICATION_ERROR
                }
            } else if (element is SmaliLiteral) {
                return Types.LITERAL
            }
        }
        return UsageType.UNCLASSIFIED
    }

    object Types {
        val CLASS_DECLARATION: UsageType = UsageType { "Class declaration" }
        val VERIFICATION_ERROR: UsageType = UsageType { "Usage in verification error" }
        val FIELD_TYPE_REFERENCE: UsageType = UsageType { "Usage as field type in a field reference" }
        val FIELD_DECLARING_TYPE_REFERENCE: UsageType = UsageType { "Usage as a declaring type in a field reference" }
        val METHOD_RETURN_TYPE_REFERENCE: UsageType = UsageType { "Usage as return type in a method reference" }
        val METHOD_PARAM_REFERENCE: UsageType = UsageType { "Usage as parameter in a method reference" }
        val METHOD_DECLARING_TYPE_REFERENCE: UsageType = UsageType { "Usage as a declaring type in a method reference" }
        val LITERAL: UsageType = UsageType { "Usage as a literal" }
    }
}
