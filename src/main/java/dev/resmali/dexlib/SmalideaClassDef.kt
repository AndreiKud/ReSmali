/*
 * Copyright 2016, Google Inc.
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
import com.android.tools.smali.dexlib2.base.reference.BaseTypeReference
import com.android.tools.smali.dexlib2.iface.Annotation
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Field
import com.android.tools.smali.dexlib2.iface.Method
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier
import dev.resmali.util.NameUtils
import javax.annotation.Nonnull

class SmalideaClassDef(private val psiClass: PsiClass) : BaseTypeReference(), ClassDef {
    override fun getAccessFlags(): Int {
        val modifierList = psiClass.modifierList ?: return 0
        var flags = 0

        if (modifierList.hasModifierProperty(PsiModifier.PUBLIC)) {
            flags = flags or AccessFlags.PUBLIC.value
        }

        if (modifierList.hasModifierProperty(PsiModifier.FINAL)) {
            flags = flags or AccessFlags.FINAL.value
        }

        if (modifierList.hasModifierProperty(PsiModifier.ABSTRACT)) {
            flags = flags or AccessFlags.ABSTRACT.value
        }

        if (modifierList.hasModifierProperty(PsiModifier.PROTECTED)) {
            flags = flags or AccessFlags.PROTECTED.value
        }

        if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
            flags = flags or AccessFlags.PRIVATE.value
        }

        if (psiClass.isInterface) {
            flags = flags or AccessFlags.INTERFACE.value
        }

        if (psiClass.isEnum) {
            flags = flags or AccessFlags.ENUM.value
        }

        if (psiClass.isAnnotationType) {
            flags = flags or AccessFlags.ANNOTATION.value
        }

        return flags
    }

    @Nonnull
    override fun getType(): String {
        return NameUtils.javaToSmaliType(psiClass)
    }

    override fun getSuperclass(): String? {
        return psiClass.superClass?.let(NameUtils::javaToSmaliType)
    }

    @Nonnull
    override fun getInterfaces(): MutableList<String> {
        return psiClass.interfaces.map(NameUtils::javaToSmaliType).toMutableList()
    }

    override fun getSourceFile(): String? {
        return null
    }

    @Nonnull
    override fun getAnnotations(): MutableSet<out Annotation> = mutableSetOf()

    @Nonnull
    override fun getStaticFields(): Iterable<Field> {
        return psiClass.fields.filter { it.modifierList?.hasModifierProperty(PsiModifier.STATIC) == true }.map(::SmalideaField)
    }

    @Nonnull
    override fun getInstanceFields(): Iterable<Field> {
        return psiClass.fields.filter { it.modifierList?.hasModifierProperty(PsiModifier.STATIC) != true }.map(::SmalideaField)
    }

    @Nonnull
    override fun getFields(): Iterable<Field> = staticFields + instanceFields

    @Nonnull
    override fun getDirectMethods(): Iterable<Method> {
        return (psiClass.constructors.asList() + psiClass.methods).filter { method ->
            val modifiers = method.modifierList
            modifiers.hasModifierProperty(PsiModifier.STATIC) || modifiers.hasModifierProperty(PsiModifier.PRIVATE) || modifiers.hasModifierProperty(
                "constructor",
            )
        }.map(::SmalideaMethod)
    }

    @Nonnull
    override fun getVirtualMethods(): Iterable<Method> {
        return psiClass.methods.filter { method ->
            val modifiers = method.modifierList
            !modifiers.hasModifierProperty(PsiModifier.STATIC) && !modifiers.hasModifierProperty(PsiModifier.PRIVATE) && !modifiers.hasModifierProperty(
                "constructor",
            )
        }.map(::SmalideaMethod)
    }

    @Nonnull
    override fun getMethods(): Iterable<Method> = directMethods + virtualMethods
}
