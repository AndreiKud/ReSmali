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
package dev.resmali

import com.android.tools.smali.smali.smaliParser
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import dev.resmali.highlighter.SmaliHighlightingColors

// The token properties are populated reflectively from ANTLR token names.
@Suppress("unused", "MemberVisibilityCanBePrivate")
object SmaliTokens {
    private val ELEMENT_TYPES: Array<IElementType?>

    fun getElementType(tokenType: Int): IElementType? {
        return ELEMENT_TYPES[tokenType]
    }

    lateinit var ACCESS_SPEC: IElementType
    lateinit var ANNOTATION_DIRECTIVE: IElementType
    lateinit var ANNOTATION_VISIBILITY: IElementType
    lateinit var ARRAY_DATA_DIRECTIVE: IElementType
    lateinit var ARRAY_TYPE_PREFIX: IElementType
    lateinit var ARROW: IElementType
    lateinit var BOOL_LITERAL: IElementType
    lateinit var BYTE_LITERAL: IElementType
    lateinit var CATCH_DIRECTIVE: IElementType
    lateinit var CATCHALL_DIRECTIVE: IElementType
    lateinit var CHAR_LITERAL: IElementType
    lateinit var CLASS_DESCRIPTOR: IElementType
    lateinit var CLASS_DIRECTIVE: IElementType
    lateinit var CLOSE_BRACE: IElementType
    lateinit var CLOSE_PAREN: IElementType
    lateinit var COLON: IElementType
    lateinit var COMMA: IElementType
    lateinit var DOTDOT: IElementType
    lateinit var DOUBLE_LITERAL: IElementType
    lateinit var DOUBLE_LITERAL_OR_ID: IElementType
    lateinit var END_ANNOTATION_DIRECTIVE: IElementType
    lateinit var END_ARRAY_DATA_DIRECTIVE: IElementType
    lateinit var END_FIELD_DIRECTIVE: IElementType
    lateinit var END_LOCAL_DIRECTIVE: IElementType
    lateinit var END_METHOD_DIRECTIVE: IElementType
    lateinit var END_PACKED_SWITCH_DIRECTIVE: IElementType
    lateinit var END_PARAMETER_DIRECTIVE: IElementType
    lateinit var END_SPARSE_SWITCH_DIRECTIVE: IElementType
    lateinit var END_SUBANNOTATION_DIRECTIVE: IElementType
    lateinit var ENUM_DIRECTIVE: IElementType
    lateinit var EPILOGUE_DIRECTIVE: IElementType
    lateinit var EQUAL: IElementType
    lateinit var FIELD_DIRECTIVE: IElementType
    lateinit var FIELD_OFFSET: IElementType
    lateinit var FLOAT_LITERAL: IElementType
    lateinit var FLOAT_LITERAL_OR_ID: IElementType
    lateinit var HIDDENAPI_RESTRICTION: IElementType
    lateinit var IMPLEMENTS_DIRECTIVE: IElementType
    lateinit var INLINE_INDEX: IElementType
    lateinit var INSTRUCTION_FORMAT10t: IElementType
    lateinit var INSTRUCTION_FORMAT10x: IElementType
    lateinit var INSTRUCTION_FORMAT10x_ODEX: IElementType
    lateinit var INSTRUCTION_FORMAT11n: IElementType
    lateinit var INSTRUCTION_FORMAT11x: IElementType
    lateinit var INSTRUCTION_FORMAT12x: IElementType
    lateinit var INSTRUCTION_FORMAT12x_OR_ID: IElementType
    lateinit var INSTRUCTION_FORMAT20bc: IElementType
    lateinit var INSTRUCTION_FORMAT20t: IElementType
    lateinit var INSTRUCTION_FORMAT21c_FIELD: IElementType
    lateinit var INSTRUCTION_FORMAT21c_FIELD_ODEX: IElementType
    lateinit var INSTRUCTION_FORMAT21c_STRING: IElementType
    lateinit var INSTRUCTION_FORMAT21c_TYPE: IElementType
    lateinit var INSTRUCTION_FORMAT21ih: IElementType
    lateinit var INSTRUCTION_FORMAT21lh: IElementType
    lateinit var INSTRUCTION_FORMAT21s: IElementType
    lateinit var INSTRUCTION_FORMAT21t: IElementType
    lateinit var INSTRUCTION_FORMAT22b: IElementType
    lateinit var INSTRUCTION_FORMAT22c_FIELD: IElementType
    lateinit var INSTRUCTION_FORMAT22c_FIELD_ODEX: IElementType
    lateinit var INSTRUCTION_FORMAT22c_TYPE: IElementType
    lateinit var INSTRUCTION_FORMAT22cs_FIELD: IElementType
    lateinit var INSTRUCTION_FORMAT22s: IElementType
    lateinit var INSTRUCTION_FORMAT22s_OR_ID: IElementType
    lateinit var INSTRUCTION_FORMAT22t: IElementType
    lateinit var INSTRUCTION_FORMAT22x: IElementType
    lateinit var INSTRUCTION_FORMAT23x: IElementType
    lateinit var INSTRUCTION_FORMAT30t: IElementType
    lateinit var INSTRUCTION_FORMAT31c: IElementType
    lateinit var INSTRUCTION_FORMAT31i: IElementType
    lateinit var INSTRUCTION_FORMAT31i_OR_ID: IElementType
    lateinit var INSTRUCTION_FORMAT31t: IElementType
    lateinit var INSTRUCTION_FORMAT32x: IElementType
    lateinit var INSTRUCTION_FORMAT35c_METHOD: IElementType
    lateinit var INSTRUCTION_FORMAT35c_METHOD_ODEX: IElementType
    lateinit var INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE: IElementType
    lateinit var INSTRUCTION_FORMAT35c_TYPE: IElementType
    lateinit var INSTRUCTION_FORMAT35mi_METHOD: IElementType
    lateinit var INSTRUCTION_FORMAT35ms_METHOD: IElementType
    lateinit var INSTRUCTION_FORMAT3rc_METHOD: IElementType
    lateinit var INSTRUCTION_FORMAT3rc_METHOD_ODEX: IElementType
    lateinit var INSTRUCTION_FORMAT3rc_TYPE: IElementType
    lateinit var INSTRUCTION_FORMAT3rmi_METHOD: IElementType
    lateinit var INSTRUCTION_FORMAT3rms_METHOD: IElementType
    lateinit var INSTRUCTION_FORMAT51l: IElementType
    lateinit var LINE_COMMENT: IElementType
    lateinit var LINE_DIRECTIVE: IElementType
    lateinit var LOCAL_DIRECTIVE: IElementType
    lateinit var LOCALS_DIRECTIVE: IElementType
    lateinit var LONG_LITERAL: IElementType
    lateinit var METHOD_DIRECTIVE: IElementType
    lateinit var MEMBER_NAME: IElementType
    lateinit var NEGATIVE_INTEGER_LITERAL: IElementType
    lateinit var NULL_LITERAL: IElementType
    lateinit var OPEN_BRACE: IElementType
    lateinit var OPEN_PAREN: IElementType
    lateinit var PACKED_SWITCH_DIRECTIVE: IElementType
    lateinit var PARAM_LIST_OR_ID_PRIMITIVE_TYPE: IElementType
    lateinit var PARAMETER_DIRECTIVE: IElementType
    lateinit var POSITIVE_INTEGER_LITERAL: IElementType
    lateinit var PRIMITIVE_TYPE: IElementType
    lateinit var PROLOGUE_DIRECTIVE: IElementType
    lateinit var REGISTER: IElementType
    lateinit var REGISTERS_DIRECTIVE: IElementType
    lateinit var RESTART_LOCAL_DIRECTIVE: IElementType
    lateinit var SHORT_LITERAL: IElementType
    lateinit var SIMPLE_NAME: IElementType
    lateinit var SOURCE_DIRECTIVE: IElementType
    lateinit var SPARSE_SWITCH_DIRECTIVE: IElementType
    lateinit var STRING_LITERAL: IElementType
    lateinit var SUBANNOTATION_DIRECTIVE: IElementType
    lateinit var SUPER_DIRECTIVE: IElementType
    lateinit var VERIFICATION_ERROR_TYPE: IElementType
    lateinit var VOID_TYPE: IElementType
    lateinit var VTABLE_INDEX: IElementType

    val INSTRUCTION_TOKENS: TokenSet

    init {
        val tokenColors = mutableMapOf<String, TextAttributesKey>()

        tokenColors["ACCESS_SPEC"] = SmaliHighlightingColors.ACCESS
        tokenColors["ANNOTATION_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["ANNOTATION_VISIBILITY"] = SmaliHighlightingColors.ACCESS
        tokenColors["ARRAY_DATA_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["ARRAY_TYPE_PREFIX"] = SmaliHighlightingColors.TYPE
        tokenColors["ARROW"] = SmaliHighlightingColors.ARROW
        tokenColors["BOOL_LITERAL"] = SmaliHighlightingColors.LITERAL
        tokenColors["BYTE_LITERAL"] = SmaliHighlightingColors.NUMBER
        tokenColors["CATCH_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["CATCHALL_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["CHAR_LITERAL"] = SmaliHighlightingColors.STRING
        tokenColors["CLASS_DESCRIPTOR"] = SmaliHighlightingColors.TYPE
        tokenColors["CLASS_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["CLOSE_BRACE"] = SmaliHighlightingColors.BRACES
        tokenColors["CLOSE_PAREN"] = SmaliHighlightingColors.PARENS
        tokenColors["COLON"] = SmaliHighlightingColors.COLON
        tokenColors["COMMA"] = SmaliHighlightingColors.COMMA
        tokenColors["DOTDOT"] = SmaliHighlightingColors.DOTDOT
        tokenColors["DOUBLE_LITERAL"] = SmaliHighlightingColors.NUMBER
        tokenColors["DOUBLE_LITERAL_OR_ID"] = SmaliHighlightingColors.NUMBER
        tokenColors["END_ANNOTATION_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["END_ARRAY_DATA_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["END_FIELD_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["END_LOCAL_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["END_METHOD_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["END_PACKED_SWITCH_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["END_PARAMETER_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["END_SPARSE_SWITCH_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["END_SUBANNOTATION_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["ENUM_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["EPILOGUE_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["EQUAL"] = SmaliHighlightingColors.EQUAL
        tokenColors["FIELD_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["FIELD_OFFSET"] = SmaliHighlightingColors.ODEX_REFERENCE
        tokenColors["FLOAT_LITERAL"] = SmaliHighlightingColors.NUMBER
        tokenColors["FLOAT_LITERAL_OR_ID"] = SmaliHighlightingColors.NUMBER
        tokenColors["HIDDENAPI_RESTRICTION"] = SmaliHighlightingColors.ACCESS
        tokenColors["IMPLEMENTS_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["INLINE_INDEX"] = SmaliHighlightingColors.ODEX_REFERENCE
        tokenColors["INSTRUCTION_FORMAT10t"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT10x"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT10x_ODEX"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT11n"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT11x"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT12x"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT12x_OR_ID"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT20bc"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT20t"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT21c_FIELD"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT21c_FIELD_ODEX"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT21c_STRING"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT21c_TYPE"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT21ih"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT21lh"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT21s"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT21t"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT22b"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT22c_FIELD"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT22c_FIELD_ODEX"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT22c_TYPE"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT22cs_FIELD"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT22s"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT22s_OR_ID"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT22t"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT22x"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT23x"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT30t"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT31c"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT31i"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT31i_OR_ID"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT31t"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT32x"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT35c_METHOD"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT35c_METHOD_ODEX"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT35c_TYPE"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT35mi_METHOD"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT35ms_METHOD"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT3rc_METHOD"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT3rc_METHOD_ODEX"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT3rc_TYPE"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT3rmi_METHOD"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT3rms_METHOD"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["INSTRUCTION_FORMAT51l"] = SmaliHighlightingColors.INSTRUCTION
        tokenColors["LINE_COMMENT"] = SmaliHighlightingColors.COMMENT
        tokenColors["LINE_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["LOCAL_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["LOCALS_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["LONG_LITERAL"] = SmaliHighlightingColors.NUMBER
        tokenColors["MEMBER_NAME"] = SmaliHighlightingColors.IDENTIFIER
        tokenColors["METHOD_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["NEGATIVE_INTEGER_LITERAL"] = SmaliHighlightingColors.NUMBER
        tokenColors["NULL_LITERAL"] = SmaliHighlightingColors.LITERAL
        tokenColors["OPEN_BRACE"] = SmaliHighlightingColors.BRACES
        tokenColors["OPEN_PAREN"] = SmaliHighlightingColors.PARENS
        tokenColors["PACKED_SWITCH_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["PARAM_LIST_OR_ID_PRIMITIVE_TYPE"] = SmaliHighlightingColors.TYPE
        tokenColors["PARAMETER_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["POSITIVE_INTEGER_LITERAL"] = SmaliHighlightingColors.NUMBER
        tokenColors["PRIMITIVE_TYPE"] = SmaliHighlightingColors.TYPE
        tokenColors["PROLOGUE_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["REGISTER"] = SmaliHighlightingColors.REGISTER
        tokenColors["REGISTERS_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["RESTART_LOCAL_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["SHORT_LITERAL"] = SmaliHighlightingColors.NUMBER
        tokenColors["SIMPLE_NAME"] = SmaliHighlightingColors.IDENTIFIER
        tokenColors["SOURCE_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["SPARSE_SWITCH_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["STRING_LITERAL"] = SmaliHighlightingColors.STRING
        tokenColors["SUBANNOTATION_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["SUPER_DIRECTIVE"] = SmaliHighlightingColors.DIRECTIVE
        tokenColors["VERIFICATION_ERROR_TYPE"] = SmaliHighlightingColors.VERIFICATION_ERROR_TYPE
        tokenColors["VOID_TYPE"] = SmaliHighlightingColors.TYPE
        tokenColors["VTABLE_INDEX"] = SmaliHighlightingColors.ODEX_REFERENCE

        val tokenCount = smaliParser.tokenNames.size
        ELEMENT_TYPES = arrayOfNulls(tokenCount)

        for (tokenId in 0..<tokenCount) {
            val tokenName = smaliParser.tokenNames[tokenId]
            val field = try {
                SmaliTokens::class.java.getDeclaredField(tokenName).apply {
                    isAccessible = true
                }
            } catch (ex: NoSuchFieldException) {
                continue
            }

            val textAttributesKey = tokenColors[tokenName] ?: error("No color attribute for token $tokenName")

            val elementType = SmaliLexicalElementType(tokenId, tokenName, textAttributesKey)
            ELEMENT_TYPES[tokenId] = elementType

            try {
                field.set(null, elementType)
            } catch (ex: IllegalAccessException) {
                throw RuntimeException(ex)
            }
        }

        INSTRUCTION_TOKENS = TokenSet.create(
            INSTRUCTION_FORMAT10t,
            INSTRUCTION_FORMAT10x,
            INSTRUCTION_FORMAT10x_ODEX,
            INSTRUCTION_FORMAT11n,
            INSTRUCTION_FORMAT11x,
            INSTRUCTION_FORMAT12x_OR_ID,
            INSTRUCTION_FORMAT12x,
            INSTRUCTION_FORMAT20bc,
            INSTRUCTION_FORMAT20t,
            INSTRUCTION_FORMAT21c_FIELD,
            INSTRUCTION_FORMAT21c_FIELD_ODEX,
            INSTRUCTION_FORMAT21c_STRING,
            INSTRUCTION_FORMAT21c_TYPE,
            INSTRUCTION_FORMAT21ih,
            INSTRUCTION_FORMAT21lh,
            INSTRUCTION_FORMAT21s,
            INSTRUCTION_FORMAT21t,
            INSTRUCTION_FORMAT22b,
            INSTRUCTION_FORMAT22c_FIELD,
            INSTRUCTION_FORMAT22c_FIELD_ODEX,
            INSTRUCTION_FORMAT22c_TYPE,
            INSTRUCTION_FORMAT22cs_FIELD,
            INSTRUCTION_FORMAT22s_OR_ID,
            INSTRUCTION_FORMAT22s,
            INSTRUCTION_FORMAT22t,
            INSTRUCTION_FORMAT22x,
            INSTRUCTION_FORMAT23x,
            INSTRUCTION_FORMAT30t,
            INSTRUCTION_FORMAT31c,
            INSTRUCTION_FORMAT31i_OR_ID,
            INSTRUCTION_FORMAT31i,
            INSTRUCTION_FORMAT31t,
            INSTRUCTION_FORMAT32x,
            INSTRUCTION_FORMAT35c_METHOD,
            INSTRUCTION_FORMAT35c_METHOD_ODEX,
            INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE,
            INSTRUCTION_FORMAT35c_TYPE,
            INSTRUCTION_FORMAT35mi_METHOD,
            INSTRUCTION_FORMAT35ms_METHOD,
            INSTRUCTION_FORMAT3rc_METHOD,
            INSTRUCTION_FORMAT3rc_METHOD_ODEX,
            INSTRUCTION_FORMAT3rc_TYPE,
            INSTRUCTION_FORMAT3rmi_METHOD,
            INSTRUCTION_FORMAT3rms_METHOD,
            INSTRUCTION_FORMAT51l,
            ARRAY_DATA_DIRECTIVE,
            PACKED_SWITCH_DIRECTIVE,
            SPARSE_SWITCH_DIRECTIVE,
        )
    }
}
