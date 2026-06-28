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

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType

abstract class SmaliStubBasedPsiElement<T : StubElement<*>> : StubBasedPsiElementBase<T>, StubBasedPsiElement<T> {
    protected constructor(stub: T, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    protected constructor(node: ASTNode) : super(node)

    @Suppress("UNCHECKED_CAST")
    protected fun <E : PsiElement> stubOrPsiChild(elementType: IElementType): E? {
        return getStubOrPsiChild(elementType) as? E
    }

    protected fun <E : PsiElement> requiredStubOrPsiChild(elementType: IElementType): E {
        return checkNotNull(stubOrPsiChild(elementType))
    }

    protected fun <E : PsiElement> stubOrPsiChildren(
        elementType: IElementType,
        array: Array<E>,
    ): Array<E> {
        return getStubOrPsiChildren(elementType, array)
    }

    protected fun <E : PsiElement> findStubOrPsiAncestorOfType(aClass: Class<E>): E? {
        val stub = stub
        if (stub != null) {
            var parent = stub.parentStub
            while (parent != null) {
                val parentPsi = parent.psi
                if (aClass.isInstance(parentPsi)) {
                    return aClass.cast(parentPsi)
                }
                parent = parent.parentStub
            }
            return null
        }

        var parent = parent
        while (parent != null) {
            if (aClass.isInstance(parent)) {
                return aClass.cast(parent)
            }
            parent = parent.parent
        }
        return null
    }

    override fun toString(): String {
        return "${javaClass.simpleName}($elementTypeImpl)"
    }
}
