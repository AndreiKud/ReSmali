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

import com.android.tools.smali.dexlib2.analysis.AnalysisException
import com.android.tools.smali.dexlib2.analysis.ClassPath
import com.android.tools.smali.dexlib2.analysis.MethodAnalyzer
import com.intellij.debugger.SourcePosition
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.HierarchicalMethodSignature
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiAnnotationMethod
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiModifier.ModifierConstant
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.PsiTypeParameter
import com.intellij.psi.PsiTypeParameterList
import com.intellij.psi.impl.PsiImplUtil
import com.intellij.psi.impl.PsiSuperMethodImplUtil
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.MethodSignature
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod
import com.intellij.psi.util.PsiFormatUtil
import com.intellij.psi.util.PsiFormatUtilBase
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.UIUtil
import dev.resmali.dexlib.SmalideaMethod
import dev.resmali.dexlib.analysis.SmalideaClassProvider
import dev.resmali.psi.SmaliElementTypes
import dev.resmali.psi.SmaliStubElementTypes
import dev.resmali.psi.iface.SmaliModifierListOwner
import dev.resmali.psi.stub.SmaliMethodStub
import dev.resmali.util.IconUtils
import java.io.IOException
import javax.swing.Icon

class SmaliMethod : SmaliStubBasedPsiElement<SmaliMethodStub>, PsiMethod, SmaliModifierListOwner, PsiAnnotationMethod, ItemPresentation {
    constructor(stub: SmaliMethodStub) : super(stub, SmaliStubElementTypes.METHOD)

    constructor(node: ASTNode) : super(node)

    override fun getName(): String {
        val name = stub?.name ?: nameIdentifier?.text
        return if (name.isNullOrEmpty()) "<unnamed>" else name
    }

    override fun getPresentation(): ItemPresentation {
        return this
    }

    override fun hasTypeParameters(): Boolean {
        // TODO: (generics) implement this
        return false
    }

    val methodPrototype: SmaliMethodPrototype
        get() = requiredStubOrPsiChild(SmaliStubElementTypes.METHOD_PROTOTYPE)

    override fun getReturnType(): PsiType? {
        if (isConstructor) return null
        return methodPrototype.returnType
    }

    override fun getReturnTypeElement(): PsiTypeElement? {
        if (isConstructor) return null
        return this.methodPrototype.returnTypeElement
    }

    override fun getParameterList(): SmaliMethodParamList {
        return methodPrototype.parameterList
    }

    override fun getThrowsList(): SmaliThrowsList {
        return requiredStubOrPsiChild(SmaliStubElementTypes.THROWS_LIST)
    }

    override fun getBody(): PsiCodeBlock? {
        // not applicable
        return null
    }

    val instructions: MutableList<SmaliInstruction>
        get() = findChildrenByType<SmaliInstruction>(SmaliElementTypes.INSTRUCTION).filterNotNull().toMutableList()

    val catchStatements: MutableList<SmaliCatchStatement>
        get() = findChildrenByClass(SmaliCatchStatement::class.java).toMutableList()

    fun getSourcePositionForCodeOffset(offset: Int): SourcePosition? {
        for (instruction in this.instructions) {
            if (instruction.offset >= offset) {
                return SourcePosition.createFromElement(instruction)
            }
        }
        return null
    }

    fun getOffsetForLine(line: Int): Int {
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(containingFile) ?: return -1

        for (instruction in this.instructions) {
            val curLine = document.getLineNumber(instruction.textOffset)
            if (curLine >= line) {
                return instruction.offset
            }
        }
        return -1
    }

    val registerCount: Int
        get() {
            val registersStatement = findChildByClass(
                SmaliRegistersStatement::class.java,
            )
            if (registersStatement == null) {
                return 0
            }
            return registersStatement.registerCount
        }

    val parameterRegisterCount: Int
        get() {
            var parameterRegisterCount = methodPrototype.parameterList.parameterRegisterCount
            if (!this.isStatic) {
                parameterRegisterCount++
            }
            return parameterRegisterCount
        }

    val parameterStatements: Array<SmaliParameterStatement>
        get() = findChildrenByClass(SmaliParameterStatement::class.java)

    override fun isConstructor(): Boolean {
        // TODO: should this return true for the class initializer?
        return hasModifierProperty("constructor") && !hasModifierProperty("static")
    }

    val isStatic: Boolean
        get() = hasModifierProperty("static")

    override fun isVarArgs(): Boolean {
        return hasModifierProperty("varargs")
    }

    override fun getSignature(substitutor: PsiSubstitutor): MethodSignature {
        return MethodSignatureBackedByPsiMethod.create(this, substitutor)
    }

    override fun getNameIdentifier(): SmaliMemberName? {
        return findChildByClass(SmaliMemberName::class.java)
    }

    override fun findSuperMethods(): Array<PsiMethod> {
        return PsiSuperMethodImplUtil.findSuperMethods(this)
    }

    override fun findSuperMethods(checkAccess: Boolean): Array<PsiMethod> {
        return PsiSuperMethodImplUtil.findSuperMethods(this, checkAccess)
    }

    override fun findSuperMethods(parentClass: PsiClass?): Array<PsiMethod> {
        return PsiSuperMethodImplUtil.findSuperMethods(this, parentClass)
    }

    override fun findSuperMethodSignaturesIncludingStatic(checkAccess: Boolean): MutableList<MethodSignatureBackedByPsiMethod> {
        return PsiSuperMethodImplUtil.findSuperMethodSignaturesIncludingStatic(this, checkAccess)
    }

    @Deprecated("Use findDeepestSuperMethods()")
    override fun findDeepestSuperMethod(): PsiMethod? {
        return PsiSuperMethodImplUtil.findDeepestSuperMethod(this)
    }

    override fun findDeepestSuperMethods(): Array<PsiMethod> {
        return PsiSuperMethodImplUtil.findDeepestSuperMethods(this)
    }

    override fun getModifierList(): SmaliModifierList {
        return requiredStubOrPsiChild(SmaliStubElementTypes.MODIFIER_LIST)
    }

    @Throws(IncorrectOperationException::class)
    override fun setName(name: String): PsiElement {
        val smaliMemberName = nameIdentifier ?: throw IncorrectOperationException()
        smaliMemberName.setName(name)
        return this
    }

    override fun getHierarchicalMethodSignature(): HierarchicalMethodSignature {
        return PsiSuperMethodImplUtil.getHierarchicalMethodSignature(this)
    }

    override fun getDocComment(): PsiDocComment? {
        // not applicable
        return null
    }

    override fun isDeprecated(): Boolean {
        return PsiImplUtil.isDeprecatedByAnnotation(this)
    }

    override fun getTypeParameterList(): PsiTypeParameterList? {
        // TODO: (generics) implement this
        return null
    }

    override fun getTypeParameters(): Array<PsiTypeParameter> {
        // TODO: (generics) implement this
        return emptyArray()
    }

    override fun getContainingClass(): SmaliClass? {
        val parent = parentByStub
        if (parent is SmaliClass) {
            return parent
        }
        return null
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

    private val labelMap by lazy {
        buildMap {
            for (label in findChildrenByClass(SmaliLabel::class.java)) {
                putIfAbsent(label.text, label)
            }
        }
    }

    fun getLabel(name: String): SmaliLabel? {
        return labelMap[name]
    }

    private var cachedMethodAnalyzer: MethodAnalyzer? = null

    val methodAnalyzer: MethodAnalyzer?
        get() {
            if (cachedMethodAnalyzer == null && !PsiTreeUtil.hasErrorElements(this)) {
                val classPath = try {
                    ClassPath(
                        SmalideaClassProvider(project, containingFile.virtualFile),
                    )
                } catch (ex: IOException) {
                    throw RuntimeException(ex)
                }

                cachedMethodAnalyzer = try {
                    MethodAnalyzer(classPath, SmalideaMethod(this@SmaliMethod), null, false)
                } catch (ex: AnalysisException) {
                    null
                }
            }
            return cachedMethodAnalyzer
        }

    override fun subtreeChanged() {
        super.subtreeChanged()
        cachedMethodAnalyzer = null
    }

    override fun getTextOffset(): Int {
        val smaliMemberName = nameIdentifier
        if (smaliMemberName != null) {
            return smaliMemberName.textOffset
        }
        return super.getTextOffset()
    }

    override fun getDefaultValue(): PsiAnnotationMemberValue? {
        val containingClass = containingClass
        if (containingClass == null || !containingClass.isAnnotationType) {
            return null
        }

        for (annotation in containingClass.annotations) {
            val annotationType = annotation.qualifiedName ?: continue
            if (annotationType == "dalvik.annotation.AnnotationDefault") {
                val value = annotation.findAttributeValue("value")
                if (value !is SmaliAnnotation) {
                    return null
                }
                return value.findAttributeValue(name)
            }
        }
        return null
    }

    override fun getPresentableText(): String? {
        return PsiFormatUtil.formatMethod(
            this, PsiSubstitutor.EMPTY,
            PsiFormatUtilBase.SHOW_NAME or PsiFormatUtilBase.SHOW_TYPE or PsiFormatUtilBase.TYPE_AFTER or PsiFormatUtilBase.SHOW_PARAMETERS,
            PsiFormatUtilBase.SHOW_TYPE,
        )
    }

    override fun getLocationString(): String? {
        val superMethod = findDeepestSuperMethods().firstOrNull()
        if (superMethod != null) {
            val upArrow = '\u2191'
            val containingClass = superMethod.containingClass
            if (containingClass != null) {
                val location = containingClass.qualifiedName
                return if (UIUtil.getLabelFont().canDisplay(upArrow)) "$upArrow$location" else location
            }
        }
        return ""
    }

    override fun getIcon(unused: Boolean): Icon {
        return IconUtils.getElementIcon(
            this,
            if (hasModifierProperty(PsiModifier.ABSTRACT)) PlatformIcons.ABSTRACT_METHOD_ICON
            else PlatformIcons.METHOD_ICON,
        )
    }
}
