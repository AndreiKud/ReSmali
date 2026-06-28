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
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.IElementType
import com.intellij.util.ArrayUtil
import com.intellij.util.IncorrectOperationException
import dev.resmali.psi.SmaliElementTypes

class SmaliAnnotationElementName : SmaliCompositeElement(SmaliElementTypes.ANNOTATION_ELEMENT_NAME), PsiIdentifier, PsiReference {
    override fun getTokenType(): IElementType {
        return elementType
    }

    override fun getName(): String {
        return text
    }

    private val containingAnnotation: SmaliAnnotation?
        get() = findAncestorByClass(SmaliAnnotation::class.java)

    @Throws(IncorrectOperationException::class)
    override fun bindToElement(element: PsiElement): PsiElement {
        // TODO: implement this if needed
        throw IncorrectOperationException()
    }

    override fun getElement(): PsiElement {
        return this
    }

    override fun getRangeInElement(): TextRange {
        return TextRange(0, textLength)
    }

    override fun resolve(): PsiElement? {
        val smaliAnnotation = containingAnnotation ?: return null

        val annotationType = smaliAnnotation.qualifiedName ?: return null

        val facade = JavaPsiFacade.getInstance(project)
        val annotationClass = facade.findClass(annotationType, resolveScope) ?: return null

        for (method in annotationClass.findMethodsByName(name, true)) {
            if (method.parameterList.parametersCount == 0) {
                return method
            }
        }
        return null
    }

    override fun getCanonicalText(): String {
        // TODO: return a full method reference here?
        return name
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        // TODO: implement this
        throw IncorrectOperationException()
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

    override fun getReference(): PsiReference {
        return this
    }
}
