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
import com.intellij.psi.JavaResolveResult
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceParameterList
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.util.IncorrectOperationException
import dev.resmali.psi.SmaliElementTypes
import dev.resmali.psi.leaf.SmaliClassDescriptor
import dev.resmali.util.NameUtils

class SmaliClassTypeElement : SmaliTypeElement(SmaliElementTypes.CLASS_TYPE), PsiJavaCodeReferenceElement {
    private val classType by lazy(LazyThreadSafetyMode.NONE) { SmaliClassType(this) }

    override fun getType(): SmaliClassType {
        return classType
    }

    override fun getName(): String? {
        return NameUtils.shortNameFromQualifiedName(canonicalText)
    }

    override fun getInnermostComponentReferenceElement(): SmaliClassTypeElement {
        return this
    }

    override fun getElement(): PsiElement {
        return this
    }

    override fun getReference(): PsiReference {
        return this
    }

    override fun getRangeInElement(): TextRange {
        return TextRange(0, textLength)
    }

    override fun resolve(): PsiClass? {
        return NameUtils.resolveSmaliType(this, text)
    }

    override fun getCanonicalText(): String {
        return qualifiedName
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        val descriptor = referenceNameElement ?: throw IncorrectOperationException()

        val newDescriptor = SmaliClassDescriptor(NameUtils.javaToSmaliType(newElementName))
        CodeEditUtil.setNodeGenerated(newDescriptor, true)

        this.replaceChild(descriptor, newDescriptor)
        return this
    }

    @Throws(IncorrectOperationException::class)
    override fun bindToElement(element: PsiElement): PsiElement {
        if (element is PsiClass) {
            val qualifiedName = element.qualifiedName ?: throw IncorrectOperationException("Cannot bind to an anonymous class")
            handleElementRename(qualifiedName)
            return this
        }
        throw IncorrectOperationException()
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return isReferenceToClass(element)
    }

    override fun getVariants(): Array<Any> {
        // TODO: implement this?
        return emptyArray()
    }

    override fun isSoft(): Boolean {
        return false
    }

    // ***************************************************************************
    // Below are the PsiJavaCodeReferenceElement-specific methods
    override fun processVariants(processor: PsiScopeProcessor) {
        // TODO: maybe just do nothing?
        throw UnsupportedOperationException()
    }

    override fun getReferenceNameElement(): SmaliClassDescriptor? {
        return findChildByClass(SmaliClassDescriptor::class.java)
    }

    override fun getParameterList(): PsiReferenceParameterList? {
        // TODO: (generics) implement this
        return null
    }

    override fun getTypeParameters(): Array<PsiType> {
        // TODO: (generics) implement this
        return emptyArray()
    }

    override fun isQualified(): Boolean {
        // TODO: should this return false for classes in the top level package?
        return true
    }

    override fun getQualifiedName(): String {
        return resolve()?.qualifiedName ?: NameUtils.smaliToJavaType(text)
    }

    override fun advancedResolve(incompleteCode: Boolean): JavaResolveResult {
        return advancedClassResolve()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<JavaResolveResult> {
        return multiClassResolve()
    }

    override fun getQualifier(): PsiElement? {
        return null
    }

    override fun getReferenceName(): String? {
        return name
    }

    companion object {
        val EMPTY_ARRAY: Array<SmaliClassTypeElement> = emptyArray()
    }
}
