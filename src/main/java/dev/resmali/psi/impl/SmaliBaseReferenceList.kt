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
import com.intellij.psi.PsiReferenceList
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import dev.resmali.psi.stub.SmaliBaseReferenceListStub
import dev.resmali.util.NameUtils

abstract class SmaliBaseReferenceList<StubT : SmaliBaseReferenceListStub<*>> : SmaliStubBasedPsiElement<StubT>, StubBasedPsiElement<StubT>,
    PsiReferenceList {
    protected constructor(stub: StubT, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    protected constructor(node: ASTNode) : super(node)

    override fun getReferencedTypes(): Array<SmaliClassType> {
        val stub = stub
        if (stub != null) {
            return stub.referencedTypes
        }

        return referenceElements.map(SmaliClassTypeElement::getType).toTypedArray()
    }

    val referenceNames: Array<String>
        get() {
            val stub: SmaliBaseReferenceListStub<*>? = stub

            if (stub != null) {
                val smaliNames = stub.smaliTypeNames
                return smaliNames.map { NameUtils.resolveSmaliToJavaType(this, it) }.toTypedArray()
            }

            return referenceElements.map(SmaliClassTypeElement::getCanonicalText).toTypedArray()
        }

    val smaliNames: Array<String>
        get() {
            val stub: SmaliBaseReferenceListStub<*>? = stub

            if (stub != null) {
                return stub.smaliTypeNames
            }

            return referenceElements.map(SmaliClassTypeElement::smaliName).toTypedArray()
        }

    override fun isWritable(): Boolean {
        return false
    }

    abstract override fun getReferenceElements(): Array<SmaliClassTypeElement>
}
