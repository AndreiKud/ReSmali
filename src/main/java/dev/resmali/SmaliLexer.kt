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

import com.android.tools.smali.smali.smaliFlexLexer
import com.android.tools.smali.smali.smaliParser
import com.android.tools.smali.smali.util.BlankReader
import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.antlr.runtime.CommonToken

class SmaliLexer : LexerBase() {
    // TODO: need to plumb in the api level somehow
    private val lexer = smaliFlexLexer(BlankReader.INSTANCE, 30)
    private var token: CommonToken? = null
    private var state = 0
    private var endOffset = 0
    private var text: CharSequence? = null

    init {
        lexer.setSuppressErrors(true)
    }

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        text = buffer
        lexer.reset(buffer, startOffset, endOffset, initialState)
        this.endOffset = endOffset
        this.token = null
        this.state = 0
    }

    override fun getTokenSequence(): CharSequence {
        return tokenText
    }

    override fun getTokenText(): String {
        return currentToken.text
    }

    override fun getState(): Int {
        ensureToken()
        return state
    }

    override fun getTokenType(): IElementType? {
        return mapTokenTypeToElementType(currentToken.type)
    }

    private fun mapTokenTypeToElementType(tokenType: Int): IElementType? {
        if (tokenType == smaliParser.WHITE_SPACE) {
            return TokenType.WHITE_SPACE
        }
        if (tokenType == smaliParser.INVALID_TOKEN) {
            return TokenType.BAD_CHARACTER
        }
        if (tokenType == smaliParser.EOF) {
            return null
        }
        return SmaliTokens.getElementType(tokenType)
    }

    override fun getTokenStart(): Int {
        return currentToken.startIndex
    }

    override fun getTokenEnd(): Int {
        return currentToken.stopIndex + 1
    }

    override fun advance() {
        token = null
        state = 0
    }

    override fun getBufferSequence(): CharSequence {
        return checkNotNull(text) { "Lexer has not been started" }
    }

    override fun getBufferEnd(): Int {
        return endOffset
    }

    private fun ensureToken() {
        if (token == null) {
            token = lexer.nextToken() as? CommonToken
            state = lexer.yystate()
        }
        checkNotNull(token)
    }

    private val currentToken: CommonToken
        get() {
            ensureToken()
            return checkNotNull(token)
        }
}
