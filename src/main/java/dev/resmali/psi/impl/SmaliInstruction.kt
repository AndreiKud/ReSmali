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

import com.android.tools.smali.dexlib2.Format
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.Opcodes
import com.android.tools.smali.dexlib2.analysis.AnalyzedInstruction
import com.google.common.base.Preconditions
import dev.resmali.SmaliTokens
import dev.resmali.psi.SmaliElementTypes

class SmaliInstruction : SmaliCompositeElement(SmaliElementTypes.INSTRUCTION) {
    private var cachedOpcode: Opcode? = null

    val opcode: Opcode
        get() {
            cachedOpcode?.let { return it }

            // TODO: keep a project-level Opcodes instance for the selected API level.
            val instructionText = checkNotNull(findChildByType(SmaliTokens.INSTRUCTION_TOKENS)).text
            return (Opcodes.getDefault().getOpcodeByName(instructionText) ?: when (instructionText) {
                ".packed-switch" -> Opcode.PACKED_SWITCH_PAYLOAD
                ".sparse-switch" -> Opcode.SPARSE_SWITCH_PAYLOAD
                ".array-data" -> Opcode.ARRAY_PAYLOAD
                else -> error("Unknown opcode: $instructionText")
            }).also {
                cachedOpcode = it
            }
        }

    var offset: Int = NO_OFFSET
        get() {
            // TODO: don't calculate this recursively. ugh!
            if (field == NO_OFFSET) {
                val previousInstruction = findPrevSiblingByClass(SmaliInstruction::class.java)
                field = if (previousInstruction == null) {
                    0
                } else {
                    previousInstruction.offset + previousInstruction.instructionSize
                }
            }
            return field
        }
        private set

    val parentMethod: SmaliMethod
        get() {
            val smaliMethod = checkNotNull(findAncestorByClass(SmaliMethod::class.java))
            return smaliMethod
        }

    fun getRegister(registerIndex: Int): Int {
        Preconditions.checkArgument(registerIndex >= 0)

        val registers = findChildrenByType(SmaliElementTypes.REGISTER_REFERENCE)
        if (registerIndex >= registers.size) {
            return -1
        }

        val registerReference = registers[registerIndex].psi as SmaliRegisterReference
        return registerReference.registerNumber
    }

    val target: SmaliLabelReference?
        get() = findChildByClass(SmaliLabelReference::class.java)

    val registerCount: Int
        get() = findChildrenByType(SmaliElementTypes.REGISTER_REFERENCE).size

    val literal: SmaliLiteral?
        get() = findChildByClass(SmaliLiteral::class.java)

    val typeReference: SmaliTypeElement?
        get() = findChildByClass(SmaliTypeElement::class.java)

    val fieldReference: SmaliFieldReference?
        get() = findChildByClass(SmaliFieldReference::class.java)

    val methodReference: SmaliMethodReference?
        get() = findChildByClass(SmaliMethodReference::class.java)

    val packedSwitchStartKey: SmaliLiteral?
        get() = findChildByClass(SmaliLiteral::class.java)

    val packedSwitchElements: List<SmaliPackedSwitchElement>
        get() = findChildrenByClass(SmaliPackedSwitchElement::class.java).asList()

    val sparseSwitchElements: List<SmaliSparseSwitchElement>
        get() = findChildrenByClass(SmaliSparseSwitchElement::class.java).asList()

    val arrayDataWidth: SmaliLiteral?
        get() = findChildByClass(SmaliLiteral::class.java)

    val arrayDataElements: List<SmaliArrayDataElement>
        get() = findChildrenByClass(SmaliArrayDataElement::class.java).asList()

    val instructionSize: Int
        get() {
            val opcode = opcode
            if (!opcode.format.isPayloadFormat) {
                return opcode.format.size
            } else if (opcode.format == Format.ArrayPayload) {
                val elementWidth = checkNotNull(arrayDataWidth).integralValue.toInt()
                val elementCount = arrayDataElements.size

                return 8 + (elementWidth * elementCount + 1)
            } else if (opcode.format == Format.PackedSwitchPayload) {
                return 8 + packedSwitchElements.size * 4
            } else if (opcode.format == Format.SparseSwitchPayload) {
                return 2 + sparseSwitchElements.size * 4
            }
            assert(false)
            throw RuntimeException()
        }

    private var cachedAnalyzedInstruction: AnalyzedInstruction? = null

    private val analyzedInstructionFromMethod: AnalyzedInstruction?
        get() {
            val analyzer = parentMethod.methodAnalyzer ?: return null

            val thisOffset = offset / 2
            var codeOffset = 0

            for (instruction in analyzer.analyzedInstructions) {
                if (codeOffset == thisOffset) {
                    return instruction
                }
                assert(codeOffset < thisOffset)

                codeOffset += instruction.originalInstruction.codeUnits
            }
            assert(false)
            return null
        }

    val analyzedInstruction: AnalyzedInstruction?
        get() {
            if (cachedAnalyzedInstruction == null) {
                cachedAnalyzedInstruction = analyzedInstructionFromMethod
            }
            return cachedAnalyzedInstruction
        }

    override fun clearCaches() {
        super.clearCaches()
        cachedAnalyzedInstruction = null
    }

    companion object {
        private const val NO_OFFSET = -1
    }
}
