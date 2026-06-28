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

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import dev.resmali.highlighter.SmaliHighlighter
import dev.resmali.highlighter.SmaliHighlightingColors
import javax.swing.Icon

private val SMALI_ATTRIBUTES = SmaliHighlightingColors.allKeys.map { key -> AttributesDescriptor(key.externalName, key) }.toTypedArray()

internal class SmaliColorsPage : ColorSettingsPage {
    override fun getIcon(): Icon {
        return SmaliIcons.SmaliIcon
    }

    override fun getHighlighter(): SyntaxHighlighter {
        return SmaliHighlighter()
    }

    // TODO: error validation "Ljava/Lang/String"?
    override fun getDemoText(): String {
        return """
            .class public Lorg/jf/smalidea/ColorExample;
            .super Ljava/lang/Object;
            .source "ColorExample.smali"

            .field public exampleField:I = 1234

            .field public boolField:Z = true

            # This is an example comment

            .method public constructor <init>()V
                .registers 1
                invoke-direct {p0}, Ljava/lang/Object;-><init>()V
                return-void
            .end method

            .method public exampleMethod()V
                .registers 10

                const v0, 1234
                const-string v1, "An Example String"

                invoke-virtual {p0, v0, v1}, Lorg/jf/smalidea/ColorExample;->anotherMethod(ILjava/lang/String;)V

                move v2, v1
                move v1, v0
                move v0, p0

                invoke-virtual/range {v0 .. v2}, Lorg/jf/smalidea/ColorExample;->anotherMethod(ILjava/lang/String;)V

                return-void
            .end method

            .method public anotherMethod(ILjava/Lang/String;)V
                .registers 10

                # This is another example comment

                return-void
            .end method

            .method public odexInstructions()V
                .registers 10
                invoke-virtual {p0}, vtable@0x1b

                iget-quick p0, field@0x1

                execute-inline {p0}, inline@0xa

                throw-verification-error illegal-method-access, Lblah;->Blort()V
            .end method
        """.trimIndent()
    }

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
        return SMALI_ATTRIBUTES
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey>? {
        return null
    }

    override fun getColorDescriptors(): Array<ColorDescriptor> {
        return ColorDescriptor.EMPTY_ARRAY
    }

    override fun getDisplayName(): String {
        return "ReSmali"
    }
}
