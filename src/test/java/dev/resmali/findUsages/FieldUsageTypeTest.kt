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
package dev.resmali.findUsages

import com.intellij.usages.impl.rules.UsageType

class FieldUsageTypeTest : UsageTypeTest(UsageTypeProviderFactory { SmaliUsageTypeProvider() }) {
    @Throws(Exception::class)
    fun testFieldUsageTypes() {
        doTest(
            "blah.smali",
            buildString {
                appendLine(
                    """
                        .class public Lblah;
                        .super Ljava/lang/Object;

                        .annotation runtime Lblah;
                            element = Lblah;->bl<ref:1>ah:Lblah;
                            element2 = .enum Lblah;->bl<ref:2>ah:Lblah;
                        .end annotation

                        .field public blah:Lblah;

                        .method public blah(Lblah;)Lblah;
                            .registers 2

                            iget v0, v0, Lblah;->bl<ref:3>ah:Lblah;
                            iget-object v0, v0, Lblah;->bl<ref:4>ah:Lblah;
                            iget-byte v0, v0, Lblah;->bl<ref:5>ah:Lblah;
                            iget-char v0, v0, Lblah;->bl<ref:6>ah:Lblah;
                            iget-object v0, v0, Lblah;->bl<ref:7>ah:Lblah;
                            iget-object-volatile v0, v0, Lblah;->bl<ref:8>ah:Lblah;
                            iget-short v0, v0, Lblah;->bl<ref:9>ah:Lblah;
                            iget-volatile v0, v0, Lblah;->bl<ref:10>ah:Lblah;
                            iget-wide v0, v0, Lblah;->bl<ref:11>ah:Lblah;
                            iget-wide-volatile v0, v0, Lblah;->bl<ref:12>ah:Lblah;
                            sget v0, Lblah;->bl<ref:13>ah:Lblah;
                            sget-boolean v0, Lblah;->bl<ref:14>ah:Lblah;
                            sget-byte v0, Lblah;->bl<ref:15>ah:Lblah;
                            sget-char v0, Lblah;->bl<ref:16>ah:Lblah;
                            sget-object v0, Lblah;->bl<ref:17>ah:Lblah;
                            sget-object-volatile v0, Lblah;->bl<ref:18>ah:Lblah;
                            sget-short v0, Lblah;->bl<ref:19>ah:Lblah;
                            sget-volatile v0, Lblah;->bl<ref:20>ah:Lblah;
                            sget-wide v0, Lblah;->bl<ref:21>ah:Lblah;
                            sget-wide-volatile v0, Lblah;->bl<ref:22>ah:Lblah;

                            iput v0, v0, Lblah;->bl<ref:23>ah:Lblah;
                            iput-object v0, v0, Lblah;->bl<ref:24>ah:Lblah;
                            iput-byte v0, v0, Lblah;->bl<ref:25>ah:Lblah;
                            iput-char v0, v0, Lblah;->bl<ref:26>ah:Lblah;
                            iput-object v0, v0, Lblah;->bl<ref:27>ah:Lblah;
                            iput-object-volatile v0, v0, Lblah;->bl<ref:28>ah:Lblah;
                            iput-short v0, v0, Lblah;->bl<ref:29>ah:Lblah;
                            iput-volatile v0, v0, Lblah;->bl<ref:30>ah:Lblah;
                            iput-wide v0, v0, Lblah;->bl<ref:31>ah:Lblah;
                            iput-wide-volatile v0, v0, Lblah;->bl<ref:32>ah:Lblah;
                            sput v0, Lblah;->bl<ref:33>ah:Lblah;
                            sput-boolean v0, Lblah;->bl<ref:34>ah:Lblah;
                            sput-byte v0, Lblah;->bl<ref:35>ah:Lblah;
                            sput-char v0, Lblah;->bl<ref:36>ah:Lblah;
                            sput-object v0, Lblah;->bl<ref:37>ah:Lblah;
                    """.trimIndent(),
                )
                // TODO: sput-object-volatile is unsupported at the default API level.
                // append("    sput-object-volatile v0, Lblah;->bl<ref:38>ah:Lblah;\n")
                appendLine(
                    """
                            sput-short v0, Lblah;->bl<ref:39>ah:Lblah;
                            sput-volatile v0, Lblah;->bl<ref:40>ah:Lblah;
                            sput-wide v0, Lblah;->bl<ref:41>ah:Lblah;
                            sput-wide-volatile v0, Lblah;->bl<ref:42>ah:Lblah;

                            throw-verification-error generic-error, Lblah;->bl<ref:43>ah:Lblah;

                            return-void
                        .end method
                    """.trimIndent(),
                )
            },
            1, SmaliUsageTypeProvider.Types.LITERAL,
            2, SmaliUsageTypeProvider.Types.LITERAL,
            3, UsageType.READ,
            4, UsageType.READ,
            5, UsageType.READ,
            6, UsageType.READ,
            7, UsageType.READ,
            8, UsageType.READ,
            9, UsageType.READ,
            10, UsageType.READ,
            11, UsageType.READ,
            12, UsageType.READ,
            13, UsageType.READ,
            14, UsageType.READ,
            15, UsageType.READ,
            16, UsageType.READ,
            17, UsageType.READ,
            18, UsageType.READ,
            19, UsageType.READ,
            20, UsageType.READ,
            21, UsageType.READ,
            22, UsageType.READ,
            23, UsageType.WRITE,
            24, UsageType.WRITE,
            25, UsageType.WRITE,
            26, UsageType.WRITE,
            27, UsageType.WRITE,
            28, UsageType.WRITE,
            29, UsageType.WRITE,
            30, UsageType.WRITE,
            31, UsageType.WRITE,
            32, UsageType.WRITE,
            33, UsageType.WRITE,
            34, UsageType.WRITE,
            35, UsageType.WRITE,
            36, UsageType.WRITE,
            37, UsageType.WRITE,
            // TODO: sput object volatile is no longer supported at default api level,
            // thus disable his usage until Smalidea supports to define an api level
            // 38, UsageType.WRITE,
            39, UsageType.WRITE,
            40, UsageType.WRITE,
            41, UsageType.WRITE,
            42, UsageType.WRITE,
            43, SmaliUsageTypeProvider.Types.VERIFICATION_ERROR,
        )
    }
}
