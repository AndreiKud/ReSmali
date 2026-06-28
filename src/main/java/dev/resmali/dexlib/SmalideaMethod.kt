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
package dev.resmali.dexlib

import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.HiddenApiRestriction
import com.android.tools.smali.dexlib2.base.reference.BaseMethodReference
import com.android.tools.smali.dexlib2.iface.Annotation
import com.android.tools.smali.dexlib2.iface.ExceptionHandler
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.MethodImplementation
import com.android.tools.smali.dexlib2.iface.MethodParameter
import com.android.tools.smali.dexlib2.iface.TryBlock
import com.android.tools.smali.dexlib2.iface.debug.DebugItem
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import dev.resmali.dexlib.instruction.SmalideaInstruction
import dev.resmali.psi.impl.SmaliMethod
import dev.resmali.util.NameUtils
import javax.annotation.Nonnull

class SmalideaMethod(private val psiMethod: PsiMethod) : BaseMethodReference(), Method {
    @Nonnull
    override fun getDefiningClass(): String {
        return NameUtils.javaToSmaliType(checkNotNull(psiMethod.containingClass))
    }

    @Nonnull
    override fun getParameters(): MutableList<out MethodParameter> {
        return psiMethod.parameterList.parameters.map(::SmalideaMethodParameter).toMutableList()
    }

    override fun getAccessFlags(): Int {
        if (psiMethod is SmaliMethod) {
            return psiMethod.modifierList.accessFlags
        }

        val modifierList = psiMethod.modifierList
        var flags = modifierList.commonDexAccessFlags()
        val isNative = modifierList.hasModifierProperty(PsiModifier.NATIVE)
        if (isNative) {
            flags = flags or AccessFlags.NATIVE.value
        }

        if (modifierList.hasModifierProperty(PsiModifier.SYNCHRONIZED)) {
            flags = flags or if (isNative) {
                AccessFlags.SYNCHRONIZED.value
            } else {
                AccessFlags.DECLARED_SYNCHRONIZED.value
            }
        }

        if (psiMethod.isVarArgs) {
            flags = flags or AccessFlags.VARARGS.value
        }

        if (modifierList.hasModifierProperty(PsiModifier.ABSTRACT)) {
            flags = flags or AccessFlags.ABSTRACT.value
        }

        if (modifierList.hasModifierProperty(PsiModifier.STRICTFP)) {
            flags = flags or AccessFlags.STRICTFP.value
        }

        if (psiMethod.isConstructor) {
            flags = flags or AccessFlags.CONSTRUCTOR.value
        }
        return flags
    }

    @Nonnull
    override fun getAnnotations(): MutableSet<out Annotation> = mutableSetOf()

    @Nonnull
    override fun getHiddenApiRestrictions(): MutableSet<HiddenApiRestriction> {
        return mutableSetOf()
    }

    override fun getImplementation(): MethodImplementation? {
        if (psiMethod is SmaliMethod) {
            val smaliMethod = this.psiMethod

            val instructions = smaliMethod.instructions
            if (instructions.isEmpty()) {
                return null
            }

            // TODO: cache this?
            return object : MethodImplementation {
                override fun getRegisterCount(): Int {
                    return smaliMethod.registerCount
                }

                @Nonnull
                override fun getInstructions(): Iterable<Instruction> {
                    return smaliMethod.instructions.map(SmalideaInstruction::of)
                }

                @Nonnull
                override fun getTryBlocks(): MutableList<out TryBlock<out ExceptionHandler>> {
                    return smaliMethod.catchStatements.map(::SmalideaTryBlock).toMutableList()
                }

                @Nonnull
                override fun getDebugItems(): Iterable<DebugItem> {
                    // TODO: implement this
                    return emptyList()
                }
            }
        }
        return null
    }

    @Nonnull
    override fun getName(): String {
        return psiMethod.name
    }

    @Nonnull
    override fun getParameterTypes(): MutableList<out CharSequence> {
        return psiMethod.parameterList.parameters.map { it.text }.toMutableList()
    }

    @Nonnull
    override fun getReturnType(): String {
        return checkNotNull(psiMethod.returnTypeElement).text
    }
}
