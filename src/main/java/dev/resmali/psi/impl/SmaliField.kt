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
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier.ModifierConstant
import com.intellij.psi.PsiType
import com.intellij.psi.impl.PsiImplUtil
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.util.IncorrectOperationException
import com.intellij.util.PlatformIcons
import dev.resmali.psi.SmaliStubElementTypes
import dev.resmali.psi.iface.SmaliModifierListOwner
import dev.resmali.psi.stub.SmaliFieldStub
import dev.resmali.util.IconUtils
import dev.resmali.util.NameUtils
import javax.swing.Icon

class SmaliField : SmaliStubBasedPsiElement<SmaliFieldStub>, PsiField, SmaliModifierListOwner, ItemPresentation {
    constructor(stub: SmaliFieldStub) : super(stub, SmaliStubElementTypes.FIELD)

    constructor(node: ASTNode) : super(node)

    override fun getName(): String {
        val stub = stub
        if (stub != null) {
            return stub.name ?: "<unnamed>"
        }

        val smaliMemberName = findChildByClass(SmaliMemberName::class.java)
        if (smaliMemberName == null || smaliMemberName.text.isEmpty()) {
            return "<unnamed>"
        }
        return smaliMemberName.text
    }

    override fun getModifierList(): SmaliModifierList {
        val modifierList = checkNotNull(stubOrPsiChild<SmaliModifierList>(SmaliStubElementTypes.MODIFIER_LIST))
        return modifierList
    }

    override fun getNameIdentifier(): SmaliMemberName {
        val memberName = checkNotNull(findChildByClass(SmaliMemberName::class.java))
        return memberName
    }

    override fun getPresentation(): ItemPresentation {
        return this
    }

    override fun getDocComment(): PsiDocComment? {
        return null
    }

    override fun isDeprecated(): Boolean {
        return PsiImplUtil.isDeprecatedByAnnotation(this)
    }

    override fun getContainingClass(): PsiClass? {
        return parentByStub as? PsiClass
    }

    override fun getType(): PsiType {
        val stub = stub
        if (stub != null) {
            return NameUtils.resolveSmaliToPsiType(this, stub.smaliTypeName)
        }
        val typeElement = typeElement
        if (typeElement == null) {
            // If we don't have a type (i.e. syntax error), use Object as a safe-ish fallback
            val factory = JavaPsiFacade.getInstance(project).elementFactory
            return factory.createTypeFromText("java.lang.Object", this)
        }
        return typeElement.type
    }

    override fun getTypeElement(): SmaliTypeElement? {
        return findChildByClass(SmaliTypeElement::class.java)
    }

    override fun getInitializer(): PsiExpression? {
        // TODO: implement this
        return null
    }

    override fun hasInitializer(): Boolean {
        // TODO: implement this
        return false
    }

    @Throws(IncorrectOperationException::class)
    override fun normalizeDeclaration() {
        // not applicable
    }

    override fun computeConstantValue(): Any? {
        // TODO: implement this
        return null
    }

    @Throws(IncorrectOperationException::class)
    override fun setName(name: String): PsiElement {
        val smaliMemberName = nameIdentifier
        smaliMemberName.setName(name)
        return this
    }

    override fun hasModifierProperty(@ModifierConstant name: String): Boolean {
        return modifierList.hasModifierProperty(name)
    }

    override fun getAnnotations(): Array<SmaliAnnotation> {
        return stubOrPsiChildren(
            SmaliStubElementTypes.ANNOTATION,
            emptyArray(),
        )
    }

    override fun getApplicableAnnotations(): Array<SmaliAnnotation> {
        return annotations
    }

    override fun findAnnotation(qualifiedName: String): SmaliAnnotation? {
        for (annotation in annotations) {
            if (qualifiedName == annotation.qualifiedName) {
                return annotation
            }
        }
        return null
    }

    override fun addAnnotation(qualifiedName: String): SmaliAnnotation {
        // TODO: implement this
        throw UnsupportedOperationException("Adding annotations is not implemented")
    }

    override fun hasAnnotation(fqn: String): Boolean {
        return super<SmaliModifierListOwner>.hasAnnotation(fqn)
    }

    @Throws(IncorrectOperationException::class)
    override fun setInitializer(initializer: PsiExpression?) {
        // TODO: implement this
    }

    override fun getTextOffset(): Int {
        return nameIdentifier.textOffset
    }

    override fun getPresentableText(): String {
        return "${name}: ${type.presentableText}"
    }

    override fun getLocationString(): String {
        return ""
    }

    override fun getIcon(unused: Boolean): Icon {
        return IconUtils.getElementIcon(this, PlatformIcons.FIELD_ICON)
    }
}
