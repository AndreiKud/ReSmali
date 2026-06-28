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
package dev.resmali.dexlib

import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.formatter.DexFormatter
import com.android.tools.smali.dexlib2.iface.ExceptionHandler
import com.android.tools.smali.dexlib2.iface.MethodParameter
import com.android.tools.smali.dexlib2.iface.TryBlock
import com.android.tools.smali.dexlib2.iface.instruction.SwitchElement
import com.android.tools.smali.dexlib2.iface.instruction.formats.ArrayPayload
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction10t
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction10x
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction11n
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction11x
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction12x
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction20t
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21ih
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21lh
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21s
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21t
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22b
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22s
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22t
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22x
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction23x
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction30t
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction32x
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction3rc
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction51l
import com.android.tools.smali.dexlib2.iface.instruction.formats.PackedSwitchPayload
import com.android.tools.smali.dexlib2.iface.instruction.formats.SparseSwitchPayload
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import dev.resmali.psi.impl.SmaliFile
import org.junit.Assert

class SmalideaMethodTest : LightJavaCodeInsightFixtureTestCase() {
    fun testSmalideaMethod() {
        val text = """
            .class public Lmy/pkg/blah; .super Ljava/lang/Object;
            .method public someMethodName(I)I
                .registers 4
                .param p1, "edge"    # I
                goto :here  #0: 10t
                :here
                return-void  #1: 21c
                const/4 v0, 1234 #2: 11n
                monitor-enter v1, #3: 11x
                move v1, v0 #4: 12x
                goto/16 :here #5: 20t
                sget v0, La/b/c;->blah:I #6: 21c
                const/high16 v0, 0x12340000 #7: 21ih
                const-wide/high16 v0, 0x1234000000000000L #8: 21lh
                const-wide/16 v0, 1234 #9: 21s
                if-eqz v0, :here #10: 21t
                add-int/lit8 v0, v1, 123 #11: 22b
                iget v1, v2, Labc;->blort:Z #12: 22c
                add-int/lit16 v0, v1, 1234 #13: 22s
                if-eq v0, v1, :here #14: 22t
                move/from16 v0, v1 #15: 22x
                cmpl-float v0, v1, v2 #16: 23x
                goto/32 :here #17: 30t
                const-string/jumbo v0, "abcd" #18: 31c
                const v0, 1234 #19: 31i
                move/16 v0, v1 #20: 32x
                invoke-virtual {v0, v1, v2, v3, v4}, Lblah;->blort(IIII)I #21: 35c
                invoke-virtual/range {v0..v4}, Lblah;->blort(IIII)I #22: 3rc
                const-wide v0, 0x1234567890L #23: 51i
            .end method
        """.trimIndent()

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        val smaliMethod = smaliClass.methods[0]

        val method = SmalideaMethod(smaliMethod)
        Assert.assertEquals("Lmy/pkg/blah;", method.definingClass)
        Assert.assertEquals("someMethodName", method.name)
        Assert.assertEquals("I", method.returnType)

        val parameterTypes: MutableList<out CharSequence> = method.parameterTypes
        Assert.assertEquals(1, parameterTypes.size.toLong())
        Assert.assertEquals("I", parameterTypes[0])

        val parameters: MutableList<out MethodParameter> = method.parameters
        Assert.assertEquals(1, parameters.size.toLong())
        Assert.assertEquals("I", parameters[0].type)
        Assert.assertEquals("edge", parameters[0].name)

        Assert.assertEquals(AccessFlags.PUBLIC.value.toLong(), method.accessFlags.toLong())

        val impl = checkNotNull(method.implementation)

        Assert.assertEquals(4, impl.registerCount.toLong())

        val instructions = impl.instructions.toList()

        run {
            val instruction = instructions[0] as Instruction10t
            Assert.assertEquals(Opcode.GOTO, instruction.opcode)
            Assert.assertEquals(1, instruction.codeOffset.toLong())
        }

        run {
            val instruction = instructions[1] as Instruction10x
            Assert.assertEquals(Opcode.RETURN_VOID, instruction.opcode)
        }

        run {
            val instruction = instructions[2] as Instruction11n
            Assert.assertEquals(Opcode.CONST_4, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(1234, instruction.narrowLiteral.toLong())
        }

        run {
            val instruction = instructions[3] as Instruction11x
            Assert.assertEquals(Opcode.MONITOR_ENTER, instruction.opcode)
            Assert.assertEquals(1, instruction.registerA.toLong())
        }

        run {
            val instruction = instructions[4] as Instruction12x
            Assert.assertEquals(Opcode.MOVE, instruction.opcode)
            Assert.assertEquals(1, instruction.registerA.toLong())
            Assert.assertEquals(0, instruction.registerB.toLong())
        }

        run {
            val instruction = instructions[5] as Instruction20t
            Assert.assertEquals(Opcode.GOTO_16, instruction.opcode)
            Assert.assertEquals(-4, instruction.codeOffset.toLong())
        }

        run {
            val instruction = instructions[6] as Instruction21c
            Assert.assertEquals(Opcode.SGET, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(
                "La/b/c;->blah:I",
                DexFormatter.INSTANCE.getFieldDescriptor(
                    instruction.reference as FieldReference,
                ),
            )
        }

        run {
            val instruction = instructions[7] as Instruction21ih
            Assert.assertEquals(Opcode.CONST_HIGH16, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(0x1234, instruction.hatLiteral.toLong())
            Assert.assertEquals(0x12340000, instruction.narrowLiteral.toLong())
            Assert.assertEquals(0x12340000, instruction.wideLiteral)
        }

        run {
            val instruction = instructions[8] as Instruction21lh
            Assert.assertEquals(Opcode.CONST_WIDE_HIGH16, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(0x1234, instruction.hatLiteral.toLong())
            Assert.assertEquals(0x1234000000000000L, instruction.wideLiteral)
        }

        run {
            val instruction = instructions[9] as Instruction21s
            Assert.assertEquals(Opcode.CONST_WIDE_16, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(1234, instruction.wideLiteral)
        }

        run {
            val instruction = instructions[10] as Instruction21t
            Assert.assertEquals(Opcode.IF_EQZ, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(-14, instruction.codeOffset.toLong())
        }

        run {
            val instruction = instructions[11] as Instruction22b
            Assert.assertEquals(Opcode.ADD_INT_LIT8, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(1, instruction.registerB.toLong())
            Assert.assertEquals(123, instruction.narrowLiteral.toLong())
        }

        run {
            val instruction = instructions[12] as Instruction22c
            Assert.assertEquals(Opcode.IGET, instruction.opcode)
            Assert.assertEquals(1, instruction.registerA.toLong())
            Assert.assertEquals(2, instruction.registerB.toLong())
            Assert.assertEquals(
                "Labc;->blort:Z",
                DexFormatter.INSTANCE.getFieldDescriptor(
                    instruction.reference as FieldReference,
                ),
            )
        }

        run {
            val instruction = instructions[13] as Instruction22s
            Assert.assertEquals(Opcode.ADD_INT_LIT16, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(1, instruction.registerB.toLong())
            Assert.assertEquals(1234, instruction.narrowLiteral.toLong())
        }

        run {
            val instruction = instructions[14] as Instruction22t
            Assert.assertEquals(Opcode.IF_EQ, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(1, instruction.registerB.toLong())
            Assert.assertEquals(-22, instruction.codeOffset.toLong())
        }

        run {
            val instruction = instructions[15] as Instruction22x
            Assert.assertEquals(Opcode.MOVE_FROM16, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(1, instruction.registerB.toLong())
        }

        run {
            val instruction = instructions[16] as Instruction23x
            Assert.assertEquals(Opcode.CMPL_FLOAT, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(1, instruction.registerB.toLong())
            Assert.assertEquals(2, instruction.registerC.toLong())
        }

        run {
            val instruction = instructions[17] as Instruction30t
            Assert.assertEquals(Opcode.GOTO_32, instruction.opcode)
            Assert.assertEquals(-28, instruction.codeOffset.toLong())
        }

        run {
            val instruction = instructions[18] as Instruction31c
            Assert.assertEquals(Opcode.CONST_STRING_JUMBO, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals("abcd", (instruction.reference as StringReference).string)
        }

        run {
            val instruction = instructions[19] as Instruction31i
            Assert.assertEquals(Opcode.CONST, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(1234, instruction.narrowLiteral.toLong())
        }

        run {
            val instruction = instructions[20] as Instruction32x
            Assert.assertEquals(Opcode.MOVE_16, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(1, instruction.registerB.toLong())
        }

        run {
            val instruction = instructions[21] as Instruction35c
            Assert.assertEquals(Opcode.INVOKE_VIRTUAL, instruction.opcode)
            Assert.assertEquals(0, instruction.registerC.toLong())
            Assert.assertEquals(1, instruction.registerD.toLong())
            Assert.assertEquals(2, instruction.registerE.toLong())
            Assert.assertEquals(3, instruction.registerF.toLong())
            Assert.assertEquals(4, instruction.registerG.toLong())
            Assert.assertEquals("Lblah;->blort(IIII)I", DexFormatter.INSTANCE.getReference(instruction.reference))
        }

        run {
            val instruction = instructions[22] as Instruction3rc
            Assert.assertEquals(Opcode.INVOKE_VIRTUAL_RANGE, instruction.opcode)
            Assert.assertEquals(0, instruction.startRegister.toLong())
            Assert.assertEquals(5, instruction.registerCount.toLong())
            Assert.assertEquals("Lblah;->blort(IIII)I", DexFormatter.INSTANCE.getReference(instruction.reference))
        }

        run {
            val instruction = instructions[23] as Instruction51l
            Assert.assertEquals(Opcode.CONST_WIDE, instruction.opcode)
            Assert.assertEquals(0, instruction.registerA.toLong())
            Assert.assertEquals(0x1234567890L, instruction.wideLiteral)
        }
    }

    fun testCatchBlocks() {
        val text = $$"""
            .class public Lmy/pkg/blah; .super Ljava/lang/Object;
            .method public onCreateEngine()Landroid/service/wallpaper/WallpaperService$Engine;
                .registers 5

                .prologue
                .line 88
                new-instance v0, Lorg/jf/Penroser/PenroserLiveWallpaper$PenroserGLEngine;

                invoke-direct {v0, p0}, Lorg/jf/Penroser/PenroserLiveWallpaper$PenroserGLEngine;-><init>(Lorg/jf/Penroser/PenroserLiveWallpaper;)V

                .line 89
                .local v0, "engine":Lorg/jf/Penroser/PenroserLiveWallpaper$PenroserGLEngine;
                sget-object v1, Lorg/jf/Penroser/PenroserLiveWallpaper;->engines:Ljava/util/LinkedList;

                monitor-enter v1

                .line 90
                :try_start_8
                sget-object v2, Lorg/jf/Penroser/PenroserLiveWallpaper;->engines:Ljava/util/LinkedList;

                new-instance v3, Ljava/lang/ref/WeakReference;

                invoke-direct {v3, v0}, Ljava/lang/ref/WeakReference;-><init>(Ljava/lang/Object;)V

                invoke-virtual {v2, v3}, Ljava/util/LinkedList;->addLast(Ljava/lang/Object;)V

                .line 91
                monitor-exit v1

                .line 92
                return-object v0

                .line 91
                :catchall_14
                move-exception v2

                monitor-exit v1
                :try_end_16
                .catch Ljava/lang/RuntimeException; {:try_start_8 .. :try_end_16} :newcatch
                .catchall {:try_start_8 .. :try_end_16} :catchall_14

                throw v2

                :newcatch
                move-exception v2
                throw v2
            .end method
        """.trimIndent()

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        val smaliMethod = smaliClass.methods[0]

        val method = SmalideaMethod(smaliMethod)

        val impl = checkNotNull(method.implementation)

        val tryBlocks = impl.tryBlocks
        Assert.assertEquals(2, tryBlocks.size.toLong())

        var tryBlock: TryBlock<out ExceptionHandler?> = tryBlocks[0]
        Assert.assertEquals(8, tryBlock.startCodeAddress.toLong())
        Assert.assertEquals(14, tryBlock.codeUnitCount.toLong())
        Assert.assertEquals(1, tryBlock.exceptionHandlers.size.toLong())
        Assert.assertEquals("Ljava/lang/RuntimeException;", tryBlock.exceptionHandlers[0].exceptionType)
        Assert.assertEquals(23, tryBlock.exceptionHandlers[0].handlerCodeAddress.toLong())

        tryBlock = tryBlocks[1]
        Assert.assertEquals(8, tryBlock.startCodeAddress.toLong())
        Assert.assertEquals(14, tryBlock.codeUnitCount.toLong())
        Assert.assertEquals(1, tryBlock.exceptionHandlers.size.toLong())
        Assert.assertEquals(null, tryBlock.exceptionHandlers[0].exceptionType)
        Assert.assertEquals(20, tryBlock.exceptionHandlers[0].handlerCodeAddress.toLong())
    }

    fun testPackedSwitch() {
        val text = """
            .class public LFormat31t;
            .super Ljava/lang/Object;
            .source "Format31t.smali"
            .method public test_packed-switch()V
                .registers 1
                .annotation runtime Lorg/junit/Test;
                .end annotation

                const v0, 12

            :switch
                packed-switch v0, :PackedSwitch

            :Label10
                invoke-static {}, Lorg/junit/Assert;->fail()V
                return-void

            :Label11
                invoke-static {}, Lorg/junit/Assert;->fail()V
                return-void

            :Label12
                return-void

            :Label13
                invoke-static {}, Lorg/junit/Assert;->fail()V
                return-void

            :PackedSwitch
                .packed-switch 10
                    :Label10
                    :Label11
                    :Label12
                    :Label13
                .end packed-switch
            .end method
        """.trimIndent()

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        val smaliMethod = smaliClass.methods[0]

        val method = SmalideaMethod(smaliMethod)

        val impl = checkNotNull(method.implementation)

        val instructions = impl.instructions.toList()

        val packedSwitchPayload = instructions[9] as PackedSwitchPayload
        val switchElements: List<SwitchElement> = packedSwitchPayload.switchElements
        Assert.assertEquals(4, switchElements.size.toLong())

        checkSwitchElement(switchElements[0], 10, 3)
        checkSwitchElement(switchElements[1], 11, 7)
        checkSwitchElement(switchElements[2], 12, 11)
        checkSwitchElement(switchElements[3], 13, 12)
    }

    fun testSparseSwitch() {
        val text = """
            .class public LFormat31t;
            .super Ljava/lang/Object;
            .source "Format31t.smali"
            .method public test_sparse-switch()V
                .registers 1
                .annotation runtime Lorg/junit/Test;
                .end annotation

                const v0, 13

            :switch
                sparse-switch v0, :SparseSwitch

            :Label10
                invoke-static {}, Lorg/junit/Assert;->fail()V
                return-void

            :Label20
                invoke-static {}, Lorg/junit/Assert;->fail()V
                return-void

            :Label15
                invoke-static {}, Lorg/junit/Assert;->fail()V
                return-void

            :Label13
                return-void

            :Label99
                invoke-static {}, Lorg/junit/Assert;->fail()V
                return-void

            :SparseSwitch
                .sparse-switch
                    10 -> :Label10
                    13 -> :Label13
                    15 -> :Label15
                    20 -> :Label20
                    99 -> :Label99
                .end sparse-switch
            .end method
        """.trimIndent()

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        val smaliMethod = smaliClass.methods[0]

        val method = SmalideaMethod(smaliMethod)

        val impl = checkNotNull(method.implementation)

        val instructions = impl.instructions.toList()

        val sparseSwitchPayload = instructions[11] as SparseSwitchPayload
        val switchElements: List<SwitchElement> = sparseSwitchPayload.switchElements
        Assert.assertEquals(5, switchElements.size.toLong())

        checkSwitchElement(switchElements[0], 10, 3)
        checkSwitchElement(switchElements[1], 13, 15)
        checkSwitchElement(switchElements[2], 15, 11)
        checkSwitchElement(switchElements[3], 20, 7)
        checkSwitchElement(switchElements[4], 99, 16)
    }

    fun testArrayData() {
        val text = """
            .class public LFormat31t;
            .super Ljava/lang/Object;
            .source "Format31t.smali"
            .method public test_fill-array-data()V
                .registers 3
                .annotation runtime Lorg/junit/Test;
                .end annotation

                const v0, 6
                new-array v0, v0, [I
                fill-array-data v0, :ArrayData

                const v1, 0
                aget v2, v0, v1
                const v1, 1
                invoke-static {v1, v2}, LAssert;->assertEquals(II)V

                const v1, 1
                aget v2, v0, v1
                const v1, 2
                invoke-static {v1, v2}, LAssert;->assertEquals(II)V

                const v1, 2
                aget v2, v0, v1
                const v1, 3
                invoke-static {v1, v2}, LAssert;->assertEquals(II)V

                const v1, 3
                aget v2, v0, v1
                const v1, 4
                invoke-static {v1, v2}, LAssert;->assertEquals(II)V

                const v1, 4
                aget v2, v0, v1
                const v1, 5
                invoke-static {v1, v2}, LAssert;->assertEquals(II)V

                const v1, 5
                aget v2, v0, v1
                const v1, 6
                invoke-static {v1, v2}, LAssert;->assertEquals(II)V

                return-void

            :ArrayData
                .array-data 4
                    1 2 128 -256 65536 0x7fffffff
                .end array-data
            .end method
        """.trimIndent()

        val file = myFixture.addFileToProject("my/pkg/blah.smali", text) as SmaliFile
        val smaliClass = checkNotNull(file.psiClass)
        val smaliMethod = smaliClass.methods[0]

        val method = SmalideaMethod(smaliMethod)

        val impl = checkNotNull(method.implementation)

        val instructions = impl.instructions.toList()

        val arrayPayload = instructions[28] as ArrayPayload
        Assert.assertEquals(4, arrayPayload.elementWidth.toLong())
        val elements = arrayPayload.arrayElements
        Assert.assertEquals(6, elements.size.toLong())

        Assert.assertEquals(1L, checkNotNull(elements[0]).toLong())
        Assert.assertEquals(2L, checkNotNull(elements[1]).toLong())
        Assert.assertEquals(128L, elements[2])
        Assert.assertEquals(-256L, elements[3])
        Assert.assertEquals(65536L, elements[4])
        Assert.assertEquals(0x7fffffffL, elements[5])
    }

    companion object {
        private fun checkSwitchElement(element: SwitchElement, key: Int, offset: Int) {
            Assert.assertEquals(key.toLong(), element.key.toLong())
            Assert.assertEquals(offset.toLong(), element.offset.toLong())
        }
    }
}
