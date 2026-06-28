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

import com.android.tools.smali.dexlib2.AccessFlags
import com.intellij.lang.ASTNode
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiModifier.ModifierConstant
import com.intellij.psi.PsiModifierList
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.impl.source.tree.Factory
import com.intellij.psi.impl.source.tree.TreeElement
import com.intellij.util.IncorrectOperationException
import dev.resmali.SmaliTokens
import dev.resmali.psi.iface.SmaliModifierListOwner
import dev.resmali.psi.stub.SmaliModifierListStub
import dev.resmali.psi.stub.element.SmaliModifierListElementType
import javax.annotation.Nonnull

class SmaliModifierList : SmaliStubBasedPsiElement<SmaliModifierListStub>, StubBasedPsiElement<SmaliModifierListStub>, PsiModifierList {
    constructor(node: ASTNode) : super(node)

    constructor(stub: SmaliModifierListStub) : super(stub, SmaliModifierListElementType.INSTANCE)

    val accessFlags: Int
        get() {
            val stub = stub
            if (stub != null) {
                return stub.accessFlags
            }

            var flags = 0

            for (accessSpec in findChildrenByType<PsiElement>(SmaliTokens.ACCESS_SPEC)) {
                val flag = AccessFlags.getAccessFlag(
                    accessSpec.text,
                )
                if (flag != null) {
                    flags = flags or flag.value
                }
            }

            return flags
        }

    override fun hasModifierProperty(@ModifierConstant name: String): Boolean {
        return hasExplicitModifier(name)
    }

    override fun hasExplicitModifier(@ModifierConstant name: String): Boolean {
        val stub = stub
        if (stub != null) {
            val flag = AccessFlags.getAccessFlag(name) ?: return false
            return (stub.accessFlags and flag.value) != 0
        }

        for (accessSpec in findChildrenByType<PsiElement>(SmaliTokens.ACCESS_SPEC)) {
            if (accessSpec.text == name) {
                return true
            }
        }

        return false
    }

    @Throws(IncorrectOperationException::class)
    override fun setModifierProperty(@ModifierConstant name: String, addModifier: Boolean) {
        if (addModifier) {
            val leaf: TreeElement = Factory.createSingleLeafElement(SmaliTokens.ACCESS_SPEC, name, null, manager)

            WriteCommandAction.writeCommandAction(project).run<RuntimeException> {
                addInternal(leaf, leaf, null, null)
            }
        } else {
            val accessSpec = getAccessFlagElement(name)
            if (accessSpec != null) {
                WriteCommandAction.writeCommandAction(project).run<RuntimeException> {
                    accessSpec.delete()
                }
            }
        }
    }

    @Throws(IncorrectOperationException::class)
    override fun checkSetModifierProperty(@ModifierConstant name: String, addModifier: Boolean) {
    }

    @get:Nonnull
    private val parentForAnnotations: SmaliModifierListOwner
        get() {
            val parent = checkNotNull(
                getStubOrPsiParentOfType(PsiModifierListOwner::class.java) as SmaliModifierListOwner,
            )
            return parent
        }

    override fun getAnnotations(): Array<SmaliAnnotation> {
        return this.parentForAnnotations.annotations
    }

    override fun getApplicableAnnotations(): Array<SmaliAnnotation> {
        return this.parentForAnnotations.applicableAnnotations
    }

    override fun findAnnotation(qualifiedName: String): SmaliAnnotation? {
        return this.parentForAnnotations.findAnnotation(qualifiedName)
    }

    override fun addAnnotation(qualifiedName: String): SmaliAnnotation {
        return this.parentForAnnotations.addAnnotation(qualifiedName)
    }

    private fun getAccessFlagElement(accessFlag: String): PsiElement? {
        for (accessSpec in findChildrenByType<PsiElement>(SmaliTokens.ACCESS_SPEC)) {
            if (accessSpec.text == accessFlag) {
                return accessSpec
            }
        }
        return null
    }
}
