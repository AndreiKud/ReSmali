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
import dev.resmali.psi.SmaliStubElementTypes
import dev.resmali.psi.iface.SmaliModifierListOwner
import dev.resmali.psi.leaf.SmaliClassDescriptor
import dev.resmali.psi.stub.SmaliClassStatementStub
import dev.resmali.util.NameUtils

class SmaliClassStatement : SmaliStubBasedPsiElement<SmaliClassStatementStub>, SmaliModifierListOwner {
    constructor(stub: SmaliClassStatementStub) : super(stub, SmaliStubElementTypes.CLASS_STATEMENT)

    constructor(node: ASTNode) : super(node)

    val nameElement: SmaliClassTypeElement?
        get() = findChildByClass(SmaliClassTypeElement::class.java)

    val containingClass: SmaliClass?
        get() = getStubOrPsiParentOfType(SmaliClass::class.java)

    val nameIdentifier: SmaliClassDescriptor?
        get() = nameElement?.referenceNameElement

    val qualifiedName: String?
        /**
         * @return the fully qualified java-style name of the class in this .class statement
         */
        get() {
            val stub = stub
            if (stub != null) {
                return stub.qualifiedName
            }

            val classType = findChildByClass(SmaliClassTypeElement::class.java) ?: run {
                // Since this is a class declared in smali, we don't have to worry about handling inner classes,
                // so we can do a pure textual translation of the class name
                return null
            }

            return NameUtils.smaliToJavaType(classType.smaliName)
        }

    override fun getModifierList(): SmaliModifierList? {
        return stubOrPsiChild(SmaliStubElementTypes.MODIFIER_LIST)
    }

    override fun addAnnotation(qualifiedName: String): SmaliAnnotation {
        val containingClass = containingClass ?: throw UnsupportedOperationException("Detached class statement")
        return containingClass.addAnnotation(qualifiedName)
    }

    override fun getAnnotations(): Array<SmaliAnnotation> {
        return containingClass?.annotations ?: emptyArray()
    }

    override fun getApplicableAnnotations(): Array<SmaliAnnotation> {
        return containingClass?.applicableAnnotations ?: emptyArray()
    }

    override fun findAnnotation(qualifiedName: String): SmaliAnnotation? {
        return containingClass?.findAnnotation(qualifiedName)
    }

    override fun hasModifierProperty(name: String): Boolean {
        return containingClass?.hasModifierProperty(name) == true
    }

    override fun hasAnnotation(fqn: String): Boolean {
        return super.hasAnnotation(fqn)
    }
}
