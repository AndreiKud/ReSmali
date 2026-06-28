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

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import java.util.Random

private val helloWorldText = """
    .class public LHelloWorld;
    .super Ljava/lang/Object;
    .method public static main([Ljava/lang/String;)V
        .registers 2
        sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;
        const-string v1, "Hello World!"
        invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
        return-void
    .end method
""".trimIndent()

private val helloWorldTokens = """
    CLASS_DIRECTIVE ('.class')
    WHITE_SPACE (' ')
    ACCESS_SPEC ('public')
    WHITE_SPACE (' ')
    CLASS_DESCRIPTOR ('LHelloWorld;')
    WHITE_SPACE ('\n')
    SUPER_DIRECTIVE ('.super')
    WHITE_SPACE (' ')
    CLASS_DESCRIPTOR ('Ljava/lang/Object;')
    WHITE_SPACE ('\n')
    METHOD_DIRECTIVE ('.method')
    WHITE_SPACE (' ')
    ACCESS_SPEC ('public')
    WHITE_SPACE (' ')
    ACCESS_SPEC ('static')
    WHITE_SPACE (' ')
    SIMPLE_NAME ('main')
    OPEN_PAREN ('(')
    ARRAY_TYPE_PREFIX ('[')
    CLASS_DESCRIPTOR ('Ljava/lang/String;')
    CLOSE_PAREN (')')
    VOID_TYPE ('V')
    WHITE_SPACE ('\n    ')
    REGISTERS_DIRECTIVE ('.registers')
    WHITE_SPACE (' ')
    POSITIVE_INTEGER_LITERAL ('2')
    WHITE_SPACE ('\n    ')
    INSTRUCTION_FORMAT21c_FIELD ('sget-object')
    WHITE_SPACE (' ')
    REGISTER ('v0')
    COMMA (',')
    WHITE_SPACE (' ')
    CLASS_DESCRIPTOR ('Ljava/lang/System;')
    ARROW ('->')
    SIMPLE_NAME ('out')
    COLON (':')
    CLASS_DESCRIPTOR ('Ljava/io/PrintStream;')
    WHITE_SPACE ('\n    ')
    INSTRUCTION_FORMAT21c_STRING ('const-string')
    WHITE_SPACE (' ')
    REGISTER ('v1')
    COMMA (',')
    WHITE_SPACE (' ')
    STRING_LITERAL ('"Hello World!"')
    WHITE_SPACE ('\n    ')
    INSTRUCTION_FORMAT35c_METHOD ('invoke-virtual')
    WHITE_SPACE (' ')
    OPEN_BRACE ('{')
    REGISTER ('v0')
    COMMA (',')
    WHITE_SPACE (' ')
    REGISTER ('v1')
    CLOSE_BRACE ('}')
    COMMA (',')
    WHITE_SPACE (' ')
    CLASS_DESCRIPTOR ('Ljava/io/PrintStream;')
    ARROW ('->')
    SIMPLE_NAME ('println')
    OPEN_PAREN ('(')
    CLASS_DESCRIPTOR ('Ljava/lang/String;')
    CLOSE_PAREN (')')
    VOID_TYPE ('V')
    WHITE_SPACE ('\n    ')
    INSTRUCTION_FORMAT10x ('return-void')
    WHITE_SPACE ('\n')
    END_METHOD_DIRECTIVE ('.end method')
""".trimIndent()

/**
 * This is mostly just a smoke test to make sure the lexer is working. The lexer itself has its
 * own tests in the smali module
 */
class SmaliLexerTest : LexerTestCase() {
    fun testHelloWorld() {
        doTest(helloWorldText, helloWorldTokens)
    }

    override fun createLexer(): Lexer {
        return SmaliLexer()
    }

    override fun getDirPath(): String {
        return ""
    }

    fun testErrorToken() {
        val text = ".class public .blah"
        doTest(
            text,
            "CLASS_DIRECTIVE ('.class')\nWHITE_SPACE (' ')\nACCESS_SPEC ('public')\nWHITE_SPACE (' ')\nBAD_CHARACTER ('.blah')\n",
        )
    }

    /**
     * Type out an example smali file character by character, ensuring that no exceptions are thrown
     */
    fun testPartialText() {
        for (i in 1..<helloWorldText.length) {
            printTokens(helloWorldText.substring(i), 0)
        }
    }

    /**
     * Generate some random text and make sure the lexer doesn't throw any exceptions
     */
    fun testRandomText() {
        for (i in 0..99) {
            val randomString = randomString(1000)

            printTokens(randomString, 0)
        }
    }

    private val random = Random(123456789)
    private fun randomString(length: Int): String {
        val sb = StringBuilder()
        for (i in 0..<length) {
            val type = random.nextInt(10)

            if (type == 9) {
                var randomCodepoint: Int
                do {
                    randomCodepoint = random.nextInt()
                } while (!Character.isValidCodePoint(randomCodepoint))
                sb.appendCodePoint(randomCodepoint)
            } else if (type == 8) {
                var randomChar: Char
                do {
                    randomChar = random.nextInt(1 shl 16).toChar()
                } while (!Character.isValidCodePoint(randomChar.code))
                sb.append(randomChar)
            } else if (type > 4) {
                sb.append(random.nextInt(256).toChar())
            } else if (type == 4) {
                sb.append(' ')
            } else {
                sb.append(random.nextInt(128).toChar())
            }
        }

        return sb.toString()
    }
}
