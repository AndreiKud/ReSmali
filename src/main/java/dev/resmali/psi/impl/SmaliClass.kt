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

import com.intellij.debugger.SourcePosition
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.Pair
import com.intellij.psi.HierarchicalMethodSignature
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassInitializer
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier.ModifierConstant
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.PsiTypeParameter
import com.intellij.psi.PsiTypeParameterList
import com.intellij.psi.ResolveState
import com.intellij.psi.impl.ElementPresentationUtil
import com.intellij.psi.impl.InheritanceImplUtil
import com.intellij.psi.impl.PsiClassImplUtil
import com.intellij.psi.impl.PsiImplUtil
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiUtil
import com.intellij.util.IncorrectOperationException
import com.sun.jdi.Location
import com.sun.jdi.ReferenceType
import dev.resmali.SmaliIcons
import dev.resmali.psi.SmaliStubElementTypes
import dev.resmali.psi.iface.SmaliModifierListOwner
import dev.resmali.psi.leaf.SmaliClassDescriptor
import dev.resmali.psi.stub.SmaliClassStub
import dev.resmali.util.IconUtils
import javax.annotation.Nonnull
import javax.swing.Icon

class SmaliClass : SmaliStubBasedPsiElement<SmaliClassStub>, PsiClass, SmaliModifierListOwner, ItemPresentation {
    constructor(stub: SmaliClassStub) : super(stub, SmaliStubElementTypes.CLASS)

    constructor(node: ASTNode) : super(node)

    @Nonnull
    override fun getName(): String {
        return qualifiedName?.substringAfterLast('.').orEmpty()
    }

    override fun getPresentation(): ItemPresentation {
        return this
    }

    override fun getQualifiedName(): String? {
        return stubOrPsiChild<SmaliClassStatement>(SmaliStubElementTypes.CLASS_STATEMENT)?.qualifiedName
    }

    var packageName: String
        get() {
            return qualifiedName?.substringBeforeLast('.', "").orEmpty()
        }
        set(packageName) {
            val classStatement = classStatement ?: throw IncorrectOperationException()
            val classTypeElement = classStatement.nameElement ?: throw IncorrectOperationException()
            val newName = if (packageName.isNotEmpty()) "$packageName.$name" else name

            classTypeElement.handleElementRename(newName)
        }

    override fun hasTypeParameters(): Boolean {
        // TODO: implement generics
        return false
    }

    override fun isInterface(): Boolean {
        return hasModifierProperty("interface")
    }

    override fun isAnnotationType(): Boolean {
        return hasModifierProperty("annotation")
    }

    override fun isEnum(): Boolean {
        return hasModifierProperty("enum")
    }

    val superStatement: SmaliSuperStatement?
        get() = findChildByClass(SmaliSuperStatement::class.java)

    override fun getExtendsList(): SmaliExtendsList {
        return requiredStubOrPsiChild(SmaliStubElementTypes.EXTENDS_LIST)
    }

    private val implementsStatements: Array<SmaliImplementsStatement>
        get() = findChildrenByClass(SmaliImplementsStatement::class.java)

    val implementedClassReferences: Array<SmaliClassTypeElement>
        get() = implementsStatements.mapNotNull { it.classReference }.toTypedArray()

    override fun getImplementsList(): SmaliImplementsList {
        return requiredStubOrPsiChild(SmaliStubElementTypes.IMPLEMENTS_LIST)
    }

    override fun getExtendsListTypes(): Array<SmaliClassType> {
        return extendsList.referencedTypes
    }

    override fun getImplementsListTypes(): Array<SmaliClassType> {
        return implementsList.referencedTypes
    }

    override fun getSuperClass(): PsiClass? {
        return PsiClassImplUtil.getSuperClass(this)
    }

    override fun getInterfaces(): Array<PsiClass> {
        return PsiClassImplUtil.getInterfaces(this)
    }

    override fun getSupers(): Array<PsiClass> {
        return PsiClassImplUtil.getSupers(this)
    }

    override fun getSuperTypes(): Array<PsiClassType> {
        return PsiClassImplUtil.getSuperTypes(this)
    }

    override fun getFields(): Array<SmaliField> {
        return stubOrPsiChildren(
            SmaliStubElementTypes.FIELD,
            emptyArray(),
        )
    }

    override fun getMethods(): Array<SmaliMethod> {
        return stubOrPsiChildren(
            SmaliStubElementTypes.METHOD,
            emptyArray(),
        )
    }

    override fun getConstructors(): Array<PsiMethod> {
        return PsiImplUtil.getConstructors(this)
    }

    override fun getInnerClasses(): Array<PsiClass> {
        return emptyArray()
    }

    override fun getInitializers(): Array<PsiClassInitializer> {
        // TODO: do we need to return the <clinit> method here?
        return emptyArray()
    }

    override fun getAllFields(): Array<PsiField> {
        return PsiClassImplUtil.getAllFields(this)
    }

    override fun getAllMethods(): Array<PsiMethod> {
        return PsiClassImplUtil.getAllMethods(this)
    }

    override fun getAllInnerClasses(): Array<PsiClass> {
        return emptyArray()
    }

    override fun findFieldByName(name: String?, checkBases: Boolean): PsiField? {
        return PsiClassImplUtil.findFieldByName(this, name, checkBases)
    }

    override fun findMethodBySignature(patternMethod: PsiMethod, checkBases: Boolean): PsiMethod? {
        return PsiClassImplUtil.findMethodBySignature(this, patternMethod, checkBases)
    }

    override fun findMethodsBySignature(patternMethod: PsiMethod, checkBases: Boolean): Array<PsiMethod> {
        return PsiClassImplUtil.findMethodsBySignature(this, patternMethod, checkBases)
    }

    override fun findMethodsByName(name: String?, checkBases: Boolean): Array<PsiMethod> {
        return PsiClassImplUtil.findMethodsByName(this, name, checkBases)
    }

    override fun findMethodsAndTheirSubstitutorsByName(
        name: String,
        checkBases: Boolean,
    ): MutableList<Pair<PsiMethod, PsiSubstitutor>> {
        return PsiClassImplUtil.findMethodsAndTheirSubstitutorsByName(this, name, checkBases)
            .map { pair -> Pair.create(checkNotNull(pair.first), checkNotNull(pair.second)) }
            .toMutableList()
    }

    override fun getAllMethodsAndTheirSubstitutors(): MutableList<Pair<PsiMethod, PsiSubstitutor>> {
        return PsiClassImplUtil.getAllWithSubstitutorsByMap<PsiMethod>(this, PsiClassImplUtil.MemberType.METHOD)
            .map { pair -> Pair.create(checkNotNull(pair.first), checkNotNull(pair.second)) }
            .toMutableList()
    }

    override fun findInnerClassByName(name: String?, checkBases: Boolean): PsiClass? {
        return null
    }

    override fun getLBrace(): PsiElement? {
        return null
    }

    override fun getRBrace(): PsiElement? {
        return null
    }

    private val classStatement: SmaliClassStatement?
        get() = stubOrPsiChild(SmaliStubElementTypes.CLASS_STATEMENT)

    override fun getNameIdentifier(): SmaliClassDescriptor? {
        return classStatement?.nameIdentifier
    }

    override fun getScope(): PsiElement? {
        return null
    }

    override fun isInheritor(baseClass: PsiClass, checkDeep: Boolean): Boolean {
        return InheritanceImplUtil.isInheritor(this, baseClass, checkDeep)
    }

    override fun isInheritorDeep(baseClass: PsiClass, classToByPass: PsiClass?): Boolean {
        return InheritanceImplUtil.isInheritorDeep(this, baseClass, classToByPass)
    }

    override fun getContainingClass(): PsiClass? {
        return null
    }

    override fun getVisibleSignatures(): MutableCollection<HierarchicalMethodSignature> {
        return mutableListOf()
    }

    @Throws(IncorrectOperationException::class)
    override fun setName(name: String): PsiElement {
        val classStatement = classStatement ?: throw IncorrectOperationException()
        val classTypeElement = classStatement.nameElement ?: throw IncorrectOperationException()

        val expectedPath = "/${this.name}.smali"

        val virtualFile = this.containingFile.virtualFile
        if (virtualFile != null) {
            val actualPath = virtualFile.path
            if (actualPath.endsWith(expectedPath)) {
                containingFile.name = "$name.smali"
            }
        }

        val packageName = this.packageName
        val newName = if (packageName.isNotEmpty()) "$packageName.$name" else name
        classTypeElement.handleElementRename(newName)
        return this
    }

    override fun getDocComment(): PsiDocComment? {
        return null
    }

    override fun isDeprecated(): Boolean {
        return false
    }

    override fun getTypeParameterList(): PsiTypeParameterList? {
        return null
    }

    override fun getTypeParameters(): Array<PsiTypeParameter> {
        return emptyArray()
    }

    override fun getModifierList(): SmaliModifierList? {
        return classStatement?.modifierList
    }

    override fun hasModifierProperty(@ModifierConstant name: String): Boolean {
        return modifierList?.hasModifierProperty(name) == true
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
        return annotations.firstOrNull { qualifiedName == it.qualifiedName }
    }

    override fun addAnnotation(qualifiedName: String): SmaliAnnotation {
        // TODO: implement this
        throw UnsupportedOperationException("Adding annotations is not implemented")
    }

    override fun hasAnnotation(fqn: String): Boolean {
        return super<SmaliModifierListOwner>.hasAnnotation(fqn)
    }

    fun getLocationForSourcePosition(
        @Nonnull type: ReferenceType,
        @Nonnull position: SourcePosition,
    ): Location? {
        val smaliMethods: Array<SmaliMethod> = findChildrenByType(
            SmaliStubElementTypes.METHOD, SmaliMethod::class.java,
        ).filterNotNull().toTypedArray()

        for (smaliMethod in smaliMethods) {
            // TODO: check the start line+end line of the method
            val offset = smaliMethod.getOffsetForLine(position.line)
            if (offset != -1) {
                val methods = type.methodsByName(
                    smaliMethod.name,
                    smaliMethod.methodPrototype.text,
                )
                if (methods.isNotEmpty()) {
                    return methods.first().locationOfCodeIndex((offset / 2).toLong())
                }
            }
        }
        return null
    }

    override fun processDeclarations(
        processor: PsiScopeProcessor, state: ResolveState,
        lastParent: PsiElement?, place: PsiElement,
    ): Boolean {
        return PsiClassImplUtil.processDeclarationsInClass(
            this, processor, state, null, lastParent, place,
            PsiUtil.getLanguageLevel(place), false,
        )
    }

    override fun getElementIcon(@Iconable.IconFlags flags: Int): Icon {
        return SmaliIcons.SmaliIcon
    }

    override fun getPresentableText(): String {
        return name
    }

    override fun getLocationString(): String {
        return this.packageName
    }

    override fun getIcon(unused: Boolean): Icon {
        val basicClassKind = ElementPresentationUtil.getBasicClassKind(this)
        return IconUtils.getElementIcon(this, ElementPresentationUtil.getClassIconOfKind(this, basicClassKind))
    }

    override fun getTextOffset(): Int {
        return findChildByClass(SmaliClassStatement::class.java)?.nameElement?.textOffset ?: super.getTextOffset()
    }
}
