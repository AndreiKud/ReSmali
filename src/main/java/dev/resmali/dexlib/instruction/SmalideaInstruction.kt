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
package dev.resmali.dexlib.instruction

import com.android.tools.smali.dexlib2.Format
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.ReferenceType
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.reference.Reference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableFieldReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableTypeReference
import dev.resmali.psi.impl.SmaliInstruction
import dev.resmali.psi.impl.SmaliLabelReference
import dev.resmali.util.NameUtils
import dev.resmali.util.StringUtils
import javax.annotation.Nonnull

internal fun switchTargetOffset(labelReference: SmaliLabelReference?, baseOffset: Int): Int {
    val label = labelReference?.resolve() ?: return 0
    return (label.offset - baseOffset) / 2
}

abstract class SmalideaInstruction protected constructor(
    @field:Nonnull @param:Nonnull protected val psiInstruction: SmaliInstruction,
) : Instruction {
    @Nonnull
    override fun getOpcode(): Opcode {
        return psiInstruction.opcode
    }

    override fun getCodeUnits(): Int {
        return opcode.format.size / 2
    }

    protected val codeOffsetValue: Int
        get() {
            val labelReference = psiInstruction.target ?: return -1
            val label = labelReference.resolve() ?: return -1
            return (label.offset - psiInstruction.offset) / 2
        }

    protected val registerCountValue: Int
        get() = psiInstruction.registerCount

    protected val registerAValue: Int
        get() = psiInstruction.getRegister(0)

    protected val registerBValue: Int
        get() = psiInstruction.getRegister(1)

    protected val registerCValue: Int
        get() = psiInstruction.getRegister(2)

    protected val narrowLiteralValue: Int
        get() {
            val literal = psiInstruction.literal ?: return 0
            return literal.integralValue.toInt()
        }

    protected val wideLiteralValue: Long
        get() {
            val literal = psiInstruction.literal ?: return 0
            return literal.integralValue
        }

    @get:Nonnull
    protected val referenceValue: Reference
        get() {
            when (referenceTypeValue) {
                ReferenceType.STRING -> return ImmutableStringReference(
                    StringUtils.parseQuotedString(
                        checkNotNull(psiInstruction.literal).text,
                    ),
                )
                ReferenceType.TYPE -> {
                    val typeReference = checkNotNull(psiInstruction.typeReference)
                    return ImmutableTypeReference(typeReference.text)
                }
                ReferenceType.METHOD -> {
                    val methodReference = checkNotNull(psiInstruction.methodReference)
                    val containingClass = checkNotNull(methodReference.containingType).text
                    val paramTypes = methodReference.parameterTypes.map(NameUtils::javaToSmaliType)

                    return ImmutableMethodReference(
                        containingClass,
                        checkNotNull(methodReference.name),
                        paramTypes,
                        checkNotNull(methodReference.returnType).text,
                    )
                }
                ReferenceType.FIELD -> {
                    val fieldReference = checkNotNull(psiInstruction.fieldReference)
                    val containingClass = checkNotNull(fieldReference.containingType).text
                    return ImmutableFieldReference(
                        containingClass,
                        checkNotNull(fieldReference.name),
                        checkNotNull(fieldReference.fieldType).text,
                    )
                }
            }
            error("Unsupported reference type: $referenceTypeValue")
        }

    protected val referenceTypeValue: Int
        get() = psiInstruction.opcode.referenceType

    companion object {
        @Nonnull
        fun of(instruction: SmaliInstruction): SmalideaInstruction {
            return when (instruction.opcode.format) {
                Format.Format10t -> SmalideaInstruction10t(instruction)
                Format.Format10x -> SmalideaInstruction10x(instruction)
                Format.Format11n -> SmalideaInstruction11n(instruction)
                Format.Format11x -> SmalideaInstruction11x(instruction)
                Format.Format12x -> SmalideaInstruction12x(instruction)
                Format.Format20t -> SmalideaInstruction20t(instruction)
                Format.Format21c -> SmalideaInstruction21c(instruction)
                Format.Format21ih -> SmalideaInstruction21ih(instruction)
                Format.Format21lh -> SmalideaInstruction21lh(instruction)
                Format.Format21s -> SmalideaInstruction21s(instruction)
                Format.Format21t -> SmalideaInstruction21t(instruction)
                Format.Format22b -> SmalideaInstruction22b(instruction)
                Format.Format22c -> SmalideaInstruction22c(instruction)
                Format.Format22s -> SmalideaInstruction22s(instruction)
                Format.Format22t -> SmalideaInstruction22t(instruction)
                Format.Format22x -> SmalideaInstruction22x(instruction)
                Format.Format23x -> SmalideaInstruction23x(instruction)
                Format.Format30t -> SmalideaInstruction30t(instruction)
                Format.Format31c -> SmalideaInstruction31c(instruction)
                Format.Format31i -> SmalideaInstruction31i(instruction)
                Format.Format31t -> SmalideaInstruction31t(instruction)
                Format.Format32x -> SmalideaInstruction32x(instruction)
                Format.Format35c -> SmalideaInstruction35c(instruction)
                Format.Format3rc -> SmalideaInstruction3rc(instruction)
                Format.Format51l -> SmalideaInstruction51l(instruction)
                Format.PackedSwitchPayload -> SmalideaPackedSwitchPayload(instruction)
                Format.SparseSwitchPayload -> SmalideaSparseSwitchPayload(instruction)
                Format.ArrayPayload -> SmalideaArrayPayload(instruction)
                else -> throw RuntimeException("Unexpected instruction type")
            }
        }
    }
}
