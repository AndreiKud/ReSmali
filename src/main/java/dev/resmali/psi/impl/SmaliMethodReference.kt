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

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiType
import com.intellij.psi.impl.light.LightMethodBuilder
import com.intellij.util.ArrayUtil
import com.intellij.util.IncorrectOperationException
import dev.resmali.SmaliLanguage
import dev.resmali.psi.SmaliElementTypes
import javax.annotation.Nonnull

class SmaliMethodReference : SmaliCompositeElement(SmaliElementTypes.METHOD_REFERENCE), PsiReference {
    override fun getName(): String? {
        return memberName?.text
    }

    override fun getReference(): PsiReference {
        return this
    }

    override fun getElement(): PsiElement {
        return this
    }

    override fun getRangeInElement(): TextRange {
        return TextRange(0, textLength)
    }

    val containingClass: PsiClass?
        get() = containingType?.resolve()

    val containingType: SmaliClassTypeElement?
        get() = findChildByClass(SmaliClassTypeElement::class.java)

    private val memberName: SmaliMemberName?
        get() = findChildByClass(SmaliMemberName::class.java)

    @get:Nonnull
    val parameterTypes: MutableList<PsiType>
        get() {
            val paramList = findChildByClass(
                SmaliMethodReferenceParamList::class.java,
            ) ?: return mutableListOf()
            return paramList.parameterTypes.mapTo(ArrayList()) { it.type }
        }

    val returnType: SmaliTypeElement?
        get() {
            return findChildrenByClass(SmaliTypeElement::class.java).getOrNull(1)
        }

    override fun resolve(): PsiElement? {
        val containingClass = containingClass ?: return null
        val memberName = memberName ?: return null

        val pattern = LightMethodBuilder(manager, SmaliLanguage.INSTANCE, memberName.text)

        for (type in this.parameterTypes) {
            pattern.addParameter("", type)
        }

        val returnTypeElement = returnType ?: return null

        pattern.setMethodReturnType(returnTypeElement.type)

        // TODO: what about static constructor?
        pattern.isConstructor = memberName.text == "<init>"

        return containingClass.findMethodBySignature(pattern, true)
    }

    override fun getCanonicalText(): String {
        return text
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return resolve() === element
    }

    override fun getVariants(): Array<Any> {
        return ArrayUtil.EMPTY_OBJECT_ARRAY
    }

    override fun isSoft(): Boolean {
        return false
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        val memberName = memberName ?: throw IncorrectOperationException()
        memberName.setName(newElementName)
        return this
    }

    @Throws(IncorrectOperationException::class)
    override fun bindToElement(element: PsiElement): PsiElement? {
        // TODO: implement this
        throw IncorrectOperationException()
    }
}
