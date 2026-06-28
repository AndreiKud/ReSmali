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
import com.android.tools.smali.dexlib2.HiddenApiRestriction
import com.android.tools.smali.dexlib2.base.reference.BaseFieldReference
import com.android.tools.smali.dexlib2.iface.Annotation
import com.android.tools.smali.dexlib2.iface.Field
import com.android.tools.smali.dexlib2.iface.value.EncodedValue
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiModifierList
import dev.resmali.psi.impl.SmaliField
import dev.resmali.util.NameUtils
import javax.annotation.Nonnull

internal fun PsiModifierList.commonDexAccessFlags(): Int {
    var flags = when {
        hasModifierProperty(PsiModifier.PUBLIC) -> AccessFlags.PUBLIC.value
        hasModifierProperty(PsiModifier.PROTECTED) -> AccessFlags.PROTECTED.value
        hasModifierProperty(PsiModifier.PRIVATE) -> AccessFlags.PRIVATE.value
        else -> 0
    }
    if (hasModifierProperty(PsiModifier.STATIC)) {
        flags = flags or AccessFlags.STATIC.value
    }
    if (hasModifierProperty(PsiModifier.FINAL)) {
        flags = flags or AccessFlags.FINAL.value
    }
    return flags
}

class SmalideaField(private val psiField: PsiField) : BaseFieldReference(), Field {
    override fun getAccessFlags(): Int {
        if (psiField is SmaliField) {
            return psiField.modifierList.accessFlags
        }
        val modifierList = psiField.modifierList ?: return 0
        var flags = modifierList.commonDexAccessFlags()

        if (modifierList.hasModifierProperty(PsiModifier.VOLATILE)) {
            flags = flags or AccessFlags.VOLATILE.value
        }

        // TODO: how do we tell if it's an enum?
        return flags
    }

    @Nonnull
    override fun getDefiningClass(): String {
        val containingClass = checkNotNull(psiField.containingClass) { "A field must have a containing class" }
        return NameUtils.javaToSmaliType(containingClass)
    }

    @Nonnull
    override fun getName(): String {
        return psiField.nameIdentifier.text
    }

    @Nonnull
    override fun getType(): String {
        return NameUtils.javaToSmaliType(psiField.type)
    }

    override fun getInitialValue(): EncodedValue? {
        // TODO: implement this. Not needed for method analysis
        return null
    }

    @Nonnull
    override fun getAnnotations(): MutableSet<out Annotation> = mutableSetOf()

    @Nonnull
    override fun getHiddenApiRestrictions(): MutableSet<HiddenApiRestriction> {
        return mutableSetOf()
    }
}
