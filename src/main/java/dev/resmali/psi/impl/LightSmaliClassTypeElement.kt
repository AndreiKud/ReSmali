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
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceParameterList
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.impl.light.LightElement
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.util.IncorrectOperationException
import dev.resmali.SmaliLanguage
import dev.resmali.util.NameUtils

class LightSmaliClassTypeElement(manager: PsiManager, private val smaliName: String) : LightElement(manager, SmaliLanguage.INSTANCE),
    PsiTypeElement, PsiReference, PsiJavaCodeReferenceElement {
    override fun toString(): String {
        return "LightSmaliClassTypeElement:$smaliName"
    }

    override fun getType(): PsiType {
        return SmaliClassType(this)
    }

    override fun getInnermostComponentReferenceElement(): LightSmaliClassTypeElement {
        return this
    }

    override fun getText(): String {
        return smaliName
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

    override fun resolve(): PsiClass? {
        return NameUtils.resolveSmaliType(this, smaliName)
    }

    override fun getCanonicalText(): String {
        return NameUtils.resolveSmaliToJavaType(this, smaliName)
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        throw UnsupportedOperationException()
    }

    @Throws(IncorrectOperationException::class)
    override fun bindToElement(element: PsiElement): PsiElement {
        throw UnsupportedOperationException()
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return isReferenceToClass(element)
    }

    override fun getVariants(): Array<Any> {
        throw RuntimeException("Variants are not available for light references")
    }

    override fun isSoft(): Boolean {
        return false
    }

    override fun getAnnotations(): Array<PsiAnnotation> {
        return emptyArray()
    }

    override fun getApplicableAnnotations(): Array<PsiAnnotation> {
        return emptyArray()
    }

    override fun findAnnotation(qualifiedName: String): PsiAnnotation? {
        return null
    }

    override fun addAnnotation(qualifiedName: String): PsiAnnotation {
        throw UnsupportedOperationException()
    }

    // ***************************************************************************
    // Below are the PsiJavaCodeReferenceElement-specific methods
    override fun processVariants(processor: PsiScopeProcessor) {
        // TODO: maybe just do nothing?
        throw UnsupportedOperationException()
    }

    override fun getReferenceNameElement(): PsiElement? {
        // TODO: implement if needed
        throw UnsupportedOperationException()
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
        return canonicalText
    }

    override fun advancedResolve(incompleteCode: Boolean): JavaResolveResult {
        return advancedClassResolve()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<JavaResolveResult> {
        return multiClassResolve()
    }

    override fun getQualifier(): PsiElement? {
        // TODO: implement this if needed
        throw UnsupportedOperationException()
    }

    override fun getReferenceName(): String? {
        return name
    }
}
