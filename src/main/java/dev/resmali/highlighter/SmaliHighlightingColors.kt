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
package dev.resmali.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import java.util.Collections

object SmaliHighlightingColors {
    private const val EXTERNAL_NAME_PREFIX = "dev.resmali."
    private val keys = mutableListOf<TextAttributesKey>()

    val ACCESS: TextAttributesKey = createTextAttributesKey(
        "ACCESS", DefaultLanguageHighlighterColors.KEYWORD,
    )
    val ARROW: TextAttributesKey = createTextAttributesKey(
        "ARROW", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL,
    )
    val BRACES: TextAttributesKey = createTextAttributesKey(
        "BRACES", DefaultLanguageHighlighterColors.BRACES,
    )
    val COLON: TextAttributesKey = createTextAttributesKey(
        "COLON", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL,
    )
    val COMMA: TextAttributesKey = createTextAttributesKey(
        "COMMA", DefaultLanguageHighlighterColors.COMMA,
    )
    val COMMENT: TextAttributesKey = createTextAttributesKey(
        "COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT,
    )
    val DIRECTIVE: TextAttributesKey = createTextAttributesKey(
        "DIRECTIVE", DefaultLanguageHighlighterColors.KEYWORD,
    )
    val DOTDOT: TextAttributesKey = createTextAttributesKey(
        "DOTDOT", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL,
    )
    val EQUAL: TextAttributesKey = createTextAttributesKey(
        "EQUAL", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL,
    )
    val IDENTIFIER: TextAttributesKey = createTextAttributesKey(
        "IDENTIFIER", DefaultLanguageHighlighterColors.INSTANCE_METHOD,
    )
    val INSTRUCTION: TextAttributesKey = createTextAttributesKey(
        "INSTRUCTION", DefaultLanguageHighlighterColors.KEYWORD,
    )
    val LITERAL: TextAttributesKey = createTextAttributesKey(
        "LITERAL", DefaultLanguageHighlighterColors.NUMBER,
    )
    val NUMBER: TextAttributesKey = createTextAttributesKey(
        "NUMBER", DefaultLanguageHighlighterColors.NUMBER,
    )
    val ODEX_REFERENCE: TextAttributesKey = createTextAttributesKey(
        "ODEX_REFERENCE", DefaultLanguageHighlighterColors.INSTANCE_METHOD,
    )
    val PARENS: TextAttributesKey = createTextAttributesKey(
        "PARENS", DefaultLanguageHighlighterColors.PARENTHESES,
    )
    val REGISTER: TextAttributesKey = createTextAttributesKey(
        "REGISTER", DefaultLanguageHighlighterColors.LOCAL_VARIABLE,
    )
    val STRING: TextAttributesKey = createTextAttributesKey(
        "STRING", DefaultLanguageHighlighterColors.STRING,
    )
    val TYPE: TextAttributesKey = createTextAttributesKey(
        "TYPE", DefaultLanguageHighlighterColors.CLASS_REFERENCE,
    )
    val VERIFICATION_ERROR_TYPE: TextAttributesKey = createTextAttributesKey(
        "VERIFICATION_ERROR_TYPE", DefaultLanguageHighlighterColors.KEYWORD,
    )

    private fun createTextAttributesKey(name: String, defaultColor: TextAttributesKey): TextAttributesKey {
        val key = TextAttributesKey.createTextAttributesKey(EXTERNAL_NAME_PREFIX + name, defaultColor)
        keys.add(key)
        return key
    }

    @get:JvmStatic
    val allKeys: List<TextAttributesKey>
        get() = Collections.unmodifiableList(keys)
}
