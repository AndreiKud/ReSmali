/*
 * Copyright 2015, Google Inc.
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

import com.android.tools.smali.dexlib2.Opcode
import com.intellij.testFramework.JavaResolveTestCase
import dev.resmali.psi.impl.SmaliInstruction
import dev.resmali.psi.impl.SmaliLabelReference
import org.junit.Assert

class SmaliLabelReferenceTest : JavaResolveTestCase() {
    @Throws(Exception::class)
    fun testLabelReference() {
        val text = """
            .class public Lmy/pkg/blah; .super Ljava/lang/Object;
            .method public getRandomParentType(I)I
                .registers 4
                .param p1, "edge"    # I

                .prologue
                const/4 v1, 0x2

                .line 179
                if-nez p1, :cond_5

                move v0, v1

                .line 185
                :goto_4
                return v0

                .line 182
                :cond_5
                if-ne p1, v1, :cond_f

                .line 183
                sget-object v0, Lorg/jf/Penroser/PenroserApp;->random:Ljava/util/Random;

                const/4 v1, 0x3

                invoke-virtual {v0, v1}, Ljava/util/Random;->nextInt(I)I

                move-result v0

                goto :goto_4

                .line 185
                :cond_f
                sget-object v0, Lorg/jf/Penroser/PenroserApp;->random:Ljava/util/Random;

                invoke-virtual {v0, v1}, Ljava/util/Random;->nextInt(I)I

                move-result v0

                goto :go<ref>to_4
            .end method
        """.trimIndent()

        val labelReference = checkNotNull(configureByFileText(text, "blah.smali") as? SmaliLabelReference)
        Assert.assertEquals("goto_4", labelReference.name)

        val resolvedLabel = checkNotNull(labelReference.resolve())
        Assert.assertEquals("goto_4", resolvedLabel.name)

        val nextInstruction = checkNotNull(resolvedLabel.findNextSiblingByClass(SmaliInstruction::class.java))
        Assert.assertEquals(8, nextInstruction.offset.toLong())
        Assert.assertEquals(Opcode.RETURN, nextInstruction.opcode)
    }
}
