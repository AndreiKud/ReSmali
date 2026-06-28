/*
 * Copyright 2012, Google Inc.
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

import com.android.tools.smali.smali.InvalidToken
import com.android.tools.smali.smali.smaliParser
import com.intellij.lang.PsiBuilder
import com.intellij.psi.TokenType
import org.antlr.runtime.CommonToken
import org.antlr.runtime.Token
import org.antlr.runtime.TokenSource
import org.antlr.runtime.TokenStream
import javax.annotation.Nonnull

class PsiBuilderTokenStream(@field:Nonnull @param:Nonnull private val psiBuilder: PsiBuilder) : TokenStream {
    private var currentToken: CommonToken? = null

    @Nonnull
    private val markers = ArrayList<PsiBuilder.Marker>()

    override fun LT(k: Int): Token? {
        if (k == 1) {
            if (currentToken == null) {
                buildCurrentToken()
            }
            return currentToken
        }
        throw UnsupportedOperationException()
    }

    override fun range(): Int {
        return if (currentToken == null) 0 else 1
    }

    override fun get(i: Int): Token? {
        throw UnsupportedOperationException()
    }

    override fun getTokenSource(): TokenSource? {
        throw UnsupportedOperationException()
    }

    override fun toString(start: Int, stop: Int): String? {
        throw UnsupportedOperationException()
    }

    override fun toString(start: Token?, stop: Token?): String? {
        throw UnsupportedOperationException()
    }

    override fun consume() {
        psiBuilder.advanceLexer()
        buildCurrentToken()
    }

    private fun buildCurrentToken() {
        val element = psiBuilder.tokenType
        currentToken = when {
            element == null -> CommonToken(Token.EOF)
            element is SmaliLexicalElementType -> CommonToken(element.tokenId, psiBuilder.tokenText)
            element === TokenType.BAD_CHARACTER -> InvalidToken("", psiBuilder.tokenText)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun LA(i: Int): Int {
        val elementType = psiBuilder.lookAhead(i - 1)
        if (elementType == null) {
            return -1
        } else if (elementType is SmaliLexicalElementType) {
            return elementType.tokenId
        } else if (elementType === TokenType.BAD_CHARACTER) {
            return smaliParser.INVALID_TOKEN
        }
        throw UnsupportedOperationException()
    }

    override fun mark(): Int {
        val ret = markers.size
        markers.add(psiBuilder.mark())
        return ret
    }

    override fun index(): Int {
        return psiBuilder.currentOffset
    }

    override fun rewind(markerIndex: Int) {
        val marker = markers[markerIndex]
        marker.rollbackTo()
        while (markerIndex < markers.size) {
            markers.removeAt(markerIndex)
        }
    }

    override fun rewind() {
        rewind(markers.size - 1)
        mark()
    }

    override fun release(markerIndex: Int) {
        while (markerIndex < markers.size) {
            markers.removeAt(markerIndex).drop()
        }
    }

    override fun seek(index: Int) {
        if (index < psiBuilder.currentOffset) {
            throw UnsupportedOperationException()
        }
        while (index > psiBuilder.currentOffset) {
            consume()
        }
    }

    override fun size(): Int {
        throw UnsupportedOperationException()
    }

    override fun getSourceName(): String? {
        return null
    }
}
