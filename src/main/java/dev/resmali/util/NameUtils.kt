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
package dev.resmali.util

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.search.GlobalSearchScope

object NameUtils {
    private val javaToSmaliPrimitiveTypes = mapOf(
        "boolean" to "Z",
        "byte" to "B",
        "char" to "C",
        "short" to "S",
        "int" to "I",
        "long" to "J",
        "float" to "F",
        "double" to "D",
    )

    fun javaToSmaliType(psiType: PsiType): String {
        if (psiType is PsiClassType) {
            val psiClass = psiType.resolve()
            if (psiClass != null) {
                return javaToSmaliType(psiClass)
            }
        }
        return javaToSmaliType(psiType.canonicalText)
    }

    fun javaToSmaliType(psiClass: PsiClass): String {
        val qualifiedName = psiClass.qualifiedName
        requireNotNull(qualifiedName) { "This method does not support anonymous classes" }
        val parent = psiClass.containingClass
        if (parent != null) {
            val offset = qualifiedName.lastIndexOf('.')
            if (offset == -1) {
                return javaToSmaliType(qualifiedName)
            }
            val parentName = qualifiedName.substring(0, offset)
            assert(parentName == parent.qualifiedName)
            val className = qualifiedName.substring(offset + 1, qualifiedName.length)
            assert(className == psiClass.name)
            return javaToSmaliType("$parentName$$className")
        } else {
            return javaToSmaliType(qualifiedName)
        }
    }

    @JvmStatic
    fun javaToSmaliType(javaType: String): String {
        if (javaType.last() == ']') {
            var dimensions = 0
            var firstArrayChar = -1
            for ((i, character) in javaType.withIndex()) {
                if (character == '[') {
                    if (firstArrayChar == -1) {
                        firstArrayChar = i
                    }
                    dimensions++
                }
            }
            if (dimensions > 0) {
                val sb = StringBuilder(firstArrayChar + 2 + dimensions)
                repeat(dimensions) {
                    sb.append('[')
                }
                convertSimpleJavaToSmaliType(javaType.substring(0, firstArrayChar), sb)
                return sb.toString()
            }
        }

        return simpleJavaToSmaliType(javaType)
    }

    private fun convertSimpleJavaToSmaliType(javaType: String, dest: StringBuilder) {
        val smaliType = javaToSmaliPrimitiveTypes[javaType]
        if (smaliType != null) {
            dest.append(smaliType)
        } else {
            dest.append('L')
            for (character in javaType) {
                if (character == '.') {
                    dest.append('/')
                } else {
                    dest.append(character)
                }
            }
            dest.append(';')
        }
    }

    fun resolveSmaliType(
        project: Project, scope: GlobalSearchScope,
        smaliType: String,
    ): PsiClass? {
        if (DumbService.isDumb(project)) {
            return null
        }

        val facade = JavaPsiFacade.getInstance(project)

        val javaType = smaliToJavaType(smaliType)

        var psiClass = facade.findClass(javaType, scope)
        if (psiClass != null) {
            return psiClass
        }

        var offset = javaType.lastIndexOf('.')
        if (offset < 0) {
            offset = 0
        }
        // find the first $ after the last .
        offset = javaType.indexOf('$', offset + 1)
        if (offset < 0) {
            return null
        }

        while (offset > 0 && offset < javaType.length - 1) {
            val left = javaType.substring(0, offset)
            psiClass = facade.findClass(left, scope)
            if (psiClass != null) {
                psiClass = findInnerClass(psiClass, javaType.substring(offset + 1, javaType.length), facade, scope)
                if (psiClass != null) {
                    return psiClass
                }
            }
            offset = javaType.indexOf('$', offset + 1)
        }
        return null
    }

    fun resolveSmaliType(
        element: PsiElement,
        smaliType: String,
    ): PsiClass? {
        // UseScope as a fallback when smali files are not marked as sources
        val scope = ResolveScopeManager.getElementResolveScope(element).union(ResolveScopeManager.getElementUseScope(element))
        return resolveSmaliType(element.project, scope, smaliType)
    }

    private fun findInnerClass(
        outerClass: PsiClass, innerText: String, facade: JavaPsiFacade,
        scope: GlobalSearchScope,
    ): PsiClass? {
        var offset = innerText.indexOf('$')
        if (offset < 0) {
            offset = innerText.length
        }

        while (offset > 0 && offset <= innerText.length) {
            val left = innerText.substring(0, offset)
            val nextInner = "${outerClass.qualifiedName}.$left"
            var psiClass = facade.findClass(nextInner, scope)
            if (psiClass != null) {
                if (offset < innerText.length) {
                    psiClass = findInnerClass(
                        psiClass, innerText.substring(offset + 1, innerText.length), facade,
                        scope,
                    )
                    if (psiClass != null) {
                        return psiClass
                    }
                } else {
                    return psiClass
                }
            }
            if (offset >= innerText.length) {
                break
            }
            offset = innerText.indexOf('$', offset + 1)
            if (offset < 0) {
                offset = innerText.length
            }
        }
        return null
    }

    private fun simpleJavaToSmaliType(simpleJavaType: String): String {
        val sb = StringBuilder(simpleJavaType.length + 2)
        convertSimpleJavaToSmaliType(simpleJavaType, sb)
        sb.trimToSize()
        return sb.toString()
    }

    @JvmStatic
    fun smaliToJavaType(smaliType: String): String {
        if (smaliType.first() == '[') {
            return convertSmaliArrayToJava(smaliType)
        } else {
            val sb = StringBuilder(smaliType.length)
            convertAndAppendNonArraySmaliTypeToJava(smaliType, sb)
            return sb.toString()
        }
    }

    private fun resolveSmaliToJavaType(
        project: Project, scope: GlobalSearchScope,
        smaliType: String,
    ): String {
        // First, try to resolve the type and get its qualified name, so that we can make sure
        // to use the correct name for inner classes
        val resolvedType = resolveSmaliType(project, scope, smaliType)
        if (resolvedType != null) {
            val qualifiedName = resolvedType.qualifiedName
            if (qualifiedName != null) {
                return qualifiedName
            }
        }

        // if we can't find it, just do a textual conversion of the name
        return smaliToJavaType(smaliType)
    }

    fun resolveSmaliToJavaType(element: PsiElement, smaliType: String): String {
        return resolveSmaliToJavaType(element.project, element.resolveScope, smaliType)
    }

    fun resolveSmaliToPsiType(element: PsiElement, smaliType: String): PsiType {
        val resolvedType = resolveSmaliType(element, smaliType)
        if (resolvedType != null) {
            val factory = JavaPsiFacade.getInstance(element.project).elementFactory
            return factory.createType(resolvedType)
        }

        val javaType = smaliToJavaType(smaliType)
        val factory = JavaPsiFacade.getInstance(element.project).elementFactory
        return factory.createTypeFromText(javaType, element)
    }

    private fun convertSmaliArrayToJava(smaliType: String): String {
        var dimensions = 0
        while (smaliType[dimensions] == '[') {
            dimensions++
        }

        val sb = StringBuilder(smaliType.length + dimensions)
        convertAndAppendNonArraySmaliTypeToJava(smaliType.substring(dimensions), sb)
        for (i in 0..<dimensions) {
            sb.append("[]")
        }
        return sb.toString()
    }

    private fun convertAndAppendNonArraySmaliTypeToJava(smaliType: String, dest: StringBuilder) {
        when (smaliType.first()) {
            'Z' -> {
                dest.append("boolean")
                return
            }
            'B' -> {
                dest.append("byte")
                return
            }
            'C' -> {
                dest.append("char")
                return
            }
            'S' -> {
                dest.append("short")
                return
            }
            'I' -> {
                dest.append("int")
                return
            }
            'J' -> {
                dest.append("long")
                return
            }
            'F' -> {
                dest.append("float")
                return
            }
            'D' -> {
                dest.append("double")
                return
            }
            'L' -> {
                var i = 1
                while (i < smaliType.length - 1) {
                    val c = smaliType[i]
                    if (c == '/') {
                        dest.append('.')
                    } else {
                        dest.append(c)
                    }
                    i++
                }
                return
            }
            'V' -> {
                dest.append("void")
                return
            }
            'U' -> {
                if (smaliType == "Ujava/lang/Object;") {
                    dest.append("java.lang.Object")
                    return
                }
                throw RuntimeException("Invalid smali type: $smaliType")
            }
            else -> throw RuntimeException("Invalid smali type: $smaliType")
        }
    }

    @JvmStatic
    fun shortNameFromQualifiedName(qualifiedName: String?): String? {
        if (qualifiedName == null) {
            return null
        }

        val index = qualifiedName.lastIndexOf('.')
        if (index == -1) {
            return qualifiedName
        }
        return qualifiedName.substring(index + 1)
    }
}
