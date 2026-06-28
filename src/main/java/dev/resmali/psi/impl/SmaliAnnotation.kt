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

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiAnnotationOwner
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.impl.PsiImplUtil
import dev.resmali.psi.SmaliStubElementTypes
import dev.resmali.psi.stub.SmaliAnnotationStub

class SmaliAnnotation : SmaliStubBasedPsiElement<SmaliAnnotationStub>, PsiAnnotation {
    constructor(stub: SmaliAnnotationStub) : super(stub, SmaliStubElementTypes.ANNOTATION)

    constructor(node: ASTNode) : super(node)

    override fun getParameterList(): SmaliAnnotationParameterList {
        val paramList = checkNotNull(findChildByClass(SmaliAnnotationParameterList::class.java))
        return paramList
    }

    override fun getQualifiedName(): String? {
        val nameElement = nameReferenceElement
        if (nameElement != null) {
            return nameElement.qualifiedName
        }
        return null
    }

    val smaliName: String?
        get() {
            val stub = stub
            if (stub != null) {
                return stub.annotationSmaliTypeName
            }

            val classType = findChildByClass(SmaliClassTypeElement::class.java) ?: return null
            return classType.smaliName
        }

    override fun getNameReferenceElement(): PsiJavaCodeReferenceElement? {
        val stub = stub
        if (stub != null) {
            val smaliName = stub.annotationSmaliTypeName
            if (smaliName != null) {
                return LightSmaliClassTypeElement(manager, smaliName)
            }
        }
        return findChildByClass(SmaliClassTypeElement::class.java)
    }

    override fun findAttributeValue(attributeName: String?): PsiAnnotationMemberValue? {
        return PsiImplUtil.findAttributeValue(this, attributeName)
    }

    override fun findDeclaredAttributeValue(attributeName: String?): PsiAnnotationMemberValue? {
        return PsiImplUtil.findDeclaredAttributeValue(this, attributeName)
    }

    // TODO: implement this
    override fun <T : PsiAnnotationMemberValue> setDeclaredAttributeValue(attributeName: String?, value: T?): T? {
        throw UnsupportedOperationException()
    }

    override fun getOwner(): PsiAnnotationOwner? {
        return parentByStub as? PsiAnnotationOwner
    }
}
