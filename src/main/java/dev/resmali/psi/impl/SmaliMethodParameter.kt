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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiModifier.ModifierConstant
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import com.intellij.util.IncorrectOperationException
import dev.resmali.psi.SmaliStubElementTypes
import dev.resmali.psi.iface.SmaliModifierListOwner
import dev.resmali.psi.stub.SmaliMethodParameterStub
import dev.resmali.util.NameUtils

class SmaliMethodParameter : SmaliStubBasedPsiElement<SmaliMethodParameterStub>, PsiParameter, SmaliModifierListOwner {
    constructor(stub: SmaliMethodParameterStub) : super(stub, SmaliStubElementTypes.METHOD_PARAMETER)

    constructor(node: ASTNode) : super(node)

    override fun getModifierList(): SmaliModifierList {
        return requiredStubOrPsiChild(SmaliStubElementTypes.MODIFIER_LIST)
    }

    override fun getDeclarationScope(): PsiElement {
        return this.parentMethod
    }

    override fun isVarArgs(): Boolean {
        if (type.arrayDimensions == 0 || !parentMethod.isVarArgs) {
            return false
        }

        val paramList = getStubOrPsiParentOfType(SmaliMethodParamList::class.java) ?: return false
        val parameters = paramList.parameters
        // is this the last parameter?
        return parameters[parameters.size - 1] === this
    }

    override fun getTypeElement(): SmaliTypeElement {
        val typeElement = checkNotNull(findChildByClass(SmaliTypeElement::class.java))
        return typeElement
    }

    override fun getType(): PsiType {
        val stub = stub
        if (stub != null) {
            return NameUtils.resolveSmaliToPsiType(this, stub.smaliTypeName)
        }
        return typeElement.type
    }

    override fun getInitializer(): PsiExpression? {
        // not applicable
        return null
    }

    override fun hasInitializer(): Boolean {
        return false
    }

    @Throws(IncorrectOperationException::class)
    override fun normalizeDeclaration() {
        // not applicable
    }

    override fun computeConstantValue(): Any? {
        // not applicable
        return null
    }

    override fun getName(): String {
        val stub = stub
        if (stub != null) {
            return stub.name.orEmpty()
        }
        // TODO: get the actual string value
        return nameIdentifier?.text.orEmpty()
    }

    override fun getNameIdentifier(): SmaliLocalName? {
        val parameterStatement = findParameterStatement() ?: return null

        return parameterStatement.nameIdentifier
    }

    @Throws(IncorrectOperationException::class)
    override fun setName(name: String): PsiElement? {
        // TODO: implement this
        throw UnsupportedOperationException()
    }

    override fun hasModifierProperty(@ModifierConstant name: String): Boolean {
        // not applicable
        return false
    }

    val registerCount: Int
        /**
         * Returns the number of registers required for this parameter. 1 for most types, but 2 for double/long.
         */
        get() {
            val type = type
            if (type == PsiTypes.doubleType() || type == PsiTypes.longType()) {
                return 2
            }
            return 1
        }

    private val parentMethod: SmaliMethod
        get() {
            val smaliMethod = checkNotNull(findStubOrPsiAncestorOfType(SmaliMethod::class.java))
            return smaliMethod
        }

    val parameterRegisterNumber: Int
        /**
         * Gets the parameter register number of these parameters. This is the number of a pNN style register reference.
         */
        get() {
            // TODO: it might be a good idea to cache this, or at least do it non-recursively
            val prevSibling = prevSibling ?: return if (parentMethod.isStatic) 0 else 1
            assert(prevSibling is SmaliMethodParameter)
            val prevParam = prevSibling as SmaliMethodParameter
            return prevParam.parameterRegisterNumber + prevParam.registerCount
        }

    val registerNumber: Int
        /**
         * Gets the register number of these parameters. This is the number of a rNN style register reference.
         */
        get() {
            val parentMethod = this.parentMethod
            return this.parameterRegisterNumber + parentMethod.registerCount - parentMethod.parameterRegisterCount
        }

    private fun findParameterStatement(): SmaliParameterStatement? {
        val parentMethod = this.parentMethod

        for (parameterStatement in parentMethod.parameterStatements) {
            val registerReference = parameterStatement.parameterRegister
            if (registerReference != null && registerReference.registerNumber == this.registerNumber) {
                return parameterStatement
            }
        }
        return null
    }

    override fun getAnnotations(): Array<SmaliAnnotation> {
        val parameterStatement = findParameterStatement() ?: return emptyArray()
        return parameterStatement.annotations
    }

    override fun getApplicableAnnotations(): Array<SmaliAnnotation> {
        return annotations
    }

    override fun findAnnotation(qualifiedName: String): SmaliAnnotation? {
        val parameterStatement = findParameterStatement() ?: return null
        return parameterStatement.findAnnotation(qualifiedName)
    }

    override fun addAnnotation(qualifiedName: String): SmaliAnnotation {
        // TODO: add a parameter statement if not found
        val parameterStatement = findParameterStatement() ?: throw UnsupportedOperationException()
        return parameterStatement.addAnnotation(qualifiedName)
    }

    override fun hasAnnotation(fqn: String): Boolean {
        return super<SmaliModifierListOwner>.hasAnnotation(fqn)
    }
}
