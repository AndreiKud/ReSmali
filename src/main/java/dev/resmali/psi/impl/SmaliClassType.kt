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

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.search.GlobalSearchScope
import dev.resmali.util.NameUtils

class SmaliClassType @JvmOverloads constructor(private val element: PsiTypeElement, languageLevel: LanguageLevel = LanguageLevel.JDK_1_5) :
    PsiClassType(languageLevel) {
    override fun resolve(): PsiClass? {
        val reference = element.reference ?: return null
        val resolved = reference.resolve()
        if (resolved is PsiClass) {
            return resolved
        }
        return null
    }

    override fun getClassName(): String? {
        val resolved = resolve()
        if (resolved != null) {
            return NameUtils.shortNameFromQualifiedName(resolved.qualifiedName)
        }
        return NameUtils.shortNameFromQualifiedName(element.text)
    }

    override fun getParameters(): Array<PsiType> {
        // TODO: (generics) implement this
        return PsiType.EMPTY_ARRAY
    }

    override fun resolveGenerics(): ClassResolveResult {
        // TODO: (generics) implement this
        return object : ClassResolveResult {
            override fun getElement(): PsiClass? {
                return resolve()
            }

            override fun getSubstitutor(): PsiSubstitutor {
                return PsiSubstitutor.EMPTY
            }

            override fun isPackagePrefixPackageReference(): Boolean {
                return false
            }

            override fun isAccessible(): Boolean {
                return true
            }

            override fun isStaticsScopeCorrect(): Boolean {
                return true
            }

            override fun getCurrentFileResolveScope(): PsiElement? {
                return null
            }

            override fun isValidResult(): Boolean {
                return true
            }
        }
    }

    override fun rawType(): SmaliClassType {
        // TODO: (generics) implement this
        return this
    }

    override fun getPresentableText(): String {
        return canonicalText
    }

    override fun getCanonicalText(): String {
        val psiClass = resolve()
        if (psiClass != null) {
            val qualifiedName = psiClass.qualifiedName
            if (qualifiedName != null) {
                return qualifiedName
            }
        }
        return NameUtils.smaliToJavaType(element.text)
    }

    override fun getInternalCanonicalText(): String {
        return canonicalText
    }

    override fun isValid(): Boolean {
        return element.isValid
    }

    override fun equalsToText(text: String): Boolean {
        return text == canonicalText
    }

    override fun getResolveScope(): GlobalSearchScope {
        return element.resolveScope
    }

    override fun getLanguageLevel(): LanguageLevel {
        return myLanguageLevel
    }

    override fun setLanguageLevel(languageLevel: LanguageLevel): PsiClassType {
        return SmaliClassType(element, languageLevel)
    }
}
