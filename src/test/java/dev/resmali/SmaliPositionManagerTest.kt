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

import com.android.tools.smali.dexlib2.Opcode
import com.intellij.debugger.NoDataException
import com.intellij.debugger.PositionManager
import com.intellij.debugger.engine.DebugProcess
import com.intellij.debugger.engine.DebugProcessListener
import com.intellij.debugger.engine.evaluation.EvaluateException
import com.intellij.debugger.engine.evaluation.EvaluationContext
import com.intellij.debugger.engine.jdi.VirtualMachineProxy
import com.intellij.debugger.engine.managerThread.DebuggerManagerThread
import com.intellij.debugger.requests.RequestManager
import com.intellij.execution.ExecutionResult
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import com.sun.jdi.ArrayReference
import com.sun.jdi.ArrayType
import com.sun.jdi.ClassLoaderReference
import com.sun.jdi.ClassObjectReference
import com.sun.jdi.ClassType
import com.sun.jdi.Field
import com.sun.jdi.Location
import com.sun.jdi.Method
import com.sun.jdi.ObjectReference
import com.sun.jdi.ReferenceType
import com.sun.jdi.Value
import com.sun.jdi.VirtualMachine
import dev.resmali.debugging.SmaliPositionManager
import dev.resmali.psi.impl.SmaliInstruction
import org.junit.Assert

class SmaliPositionManagerTest : LightJavaCodeInsightFixtureTestCase() {
    @Throws(NoDataException::class)
    fun testGetSourcePosition() {
        myFixture.addFileToProject("my/pkg/blah.smali", TEST_CLASS)
        val positionManager = SmaliPositionManager(MockDebugProcess())

        var sourcePosition = checkNotNull(
            positionManager.getSourcePosition("my.pkg.blah", "getRandomParentType", "(I)I", 0),
        )
        Assert.assertEquals(Opcode.CONST_4, (sourcePosition.elementAt as SmaliInstruction).opcode)
        Assert.assertEquals(0, (sourcePosition.elementAt as SmaliInstruction).offset)

        sourcePosition = checkNotNull(
            positionManager.getSourcePosition("my.pkg.blah", "getRandomParentType", "(I)I", 10),
        )
        Assert.assertEquals(Opcode.INVOKE_VIRTUAL, (sourcePosition.elementAt as SmaliInstruction).opcode)
        Assert.assertEquals(20, (sourcePosition.elementAt as SmaliInstruction).offset)
    }

    @Throws(NoDataException::class)
    fun testGetAllClasses() {
        myFixture.addFileToProject("my/pkg/blah.smali", TEST_CLASS)
        val positionManager = SmaliPositionManager(MockDebugProcess())
        val sourcePosition = checkNotNull(
            positionManager.getSourcePosition("my.pkg.blah", "getRandomParentType", "(I)I", 0),
        )

        val classes = positionManager.getAllClasses(sourcePosition)
        Assert.assertEquals(1, classes.size)
        Assert.assertEquals("my.pkg.blah", classes.single().name())
    }

    @Suppress("NonExtendableApiUsage")
    private inner class MockDebugProcess : DebugProcess {
        override fun getProject(): Project = this@SmaliPositionManagerTest.project

        override fun getVirtualMachineProxy(): VirtualMachineProxy = object : VirtualMachineProxy {
            override fun classesByName(name: String): MutableList<ReferenceType> = mutableListOf(MockReferenceType(name))
            override fun allClasses(): MutableList<ReferenceType> = mutableListOf()
            override fun canGetBytecodes(): Boolean = false
            override fun versionHigher(version: String): Boolean = false
            override fun canWatchFieldModification(): Boolean = false
            override fun canWatchFieldAccess(): Boolean = false
            override fun canInvokeMethods(): Boolean = false
            override fun getDebugProcess(): DebugProcess = this@MockDebugProcess
            override fun nestedTypes(refType: ReferenceType): MutableList<ReferenceType> = mutableListOf()
        }

        override fun addDebugProcessListener(listener: DebugProcessListener) = Unit
        override fun <T : Any> getUserData(key: Key<T>): T? = null
        override fun <T : Any> putUserData(key: Key<T>, value: T?) = Unit
        override fun getRequestsManager(): RequestManager = unsupported()
        override fun getPositionManager(): PositionManager = unsupported()
        override fun removeDebugProcessListener(listener: DebugProcessListener) = Unit
        override fun appendPositionManager(positionManager: PositionManager) = Unit
        override fun waitFor() = Unit
        override fun waitFor(timeout: Long) = Unit
        override fun stop(forceTerminate: Boolean) = Unit
        override fun getExecutionResult(): ExecutionResult = unsupported()
        override fun getManagerThread(): DebuggerManagerThread = unsupported()

        @Throws(EvaluateException::class)
        override fun invokeMethod(
            evaluationContext: EvaluationContext,
            objRef: ObjectReference,
            method: Method,
            args: MutableList<out Value>,
        ): Value = unsupported()

        @Throws(EvaluateException::class)
        override fun invokeMethod(
            evaluationContext: EvaluationContext,
            classType: ClassType,
            method: Method,
            args: MutableList<out Value>,
        ): Value = unsupported()

        @Throws(EvaluateException::class)
        override fun invokeInstanceMethod(
            evaluationContext: EvaluationContext,
            objRef: ObjectReference,
            method: Method,
            args: MutableList<out Value>,
            invocationOptions: Int,
        ): Value = unsupported()

        @Throws(EvaluateException::class)
        override fun findClass(
            evaluationContext: EvaluationContext?,
            name: String,
            classLoader: ClassLoaderReference,
        ): ReferenceType = unsupported()

        @Throws(EvaluateException::class)
        override fun newInstance(arrayType: ArrayType, dimension: Int): ArrayReference = unsupported()

        @Throws(EvaluateException::class)
        override fun newInstance(
            evaluationContext: EvaluationContext,
            classType: ClassType,
            constructor: Method,
            paramList: MutableList<out Value>,
        ): ObjectReference = unsupported()

        override fun isAttached(): Boolean = false
        override fun isDetached(): Boolean = false
        override fun isDetaching(): Boolean = false
        override fun getSearchScope(): GlobalSearchScope = unsupported()
        override fun printToConsole(text: String) = Unit
        override fun getProcessHandler(): ProcessHandler = unsupported()
        override fun addDebugProcessListener(listener: DebugProcessListener, disposable: Disposable) = Unit
    }

    private class MockReferenceType(private val typeName: String) : ReferenceType {
        override fun name(): String = typeName
        override fun allFields(): MutableList<Field> = mutableListOf()
        override fun genericSignature(): String = unsupported()
        override fun classLoader(): ClassLoaderReference = unsupported()
        override fun sourceName(): String = unsupported()
        override fun sourceNames(stratum: String): MutableList<String> = mutableListOf()
        override fun sourcePaths(stratum: String): MutableList<String> = mutableListOf()
        override fun sourceDebugExtension(): String = unsupported()
        override fun isStatic(): Boolean = false
        override fun isAbstract(): Boolean = false
        override fun isFinal(): Boolean = false
        override fun isPrepared(): Boolean = false
        override fun isVerified(): Boolean = false
        override fun isInitialized(): Boolean = false
        override fun failedToInitialize(): Boolean = false
        override fun fields(): MutableList<Field> = mutableListOf()
        override fun visibleFields(): MutableList<Field> = mutableListOf()
        override fun fieldByName(name: String): Field = unsupported()
        override fun methods(): MutableList<Method> = mutableListOf()
        override fun visibleMethods(): MutableList<Method> = mutableListOf()
        override fun allMethods(): MutableList<Method> = mutableListOf()
        override fun methodsByName(name: String): MutableList<Method> = mutableListOf()
        override fun methodsByName(name: String, signature: String): MutableList<Method> = mutableListOf()
        override fun nestedTypes(): MutableList<ReferenceType> = mutableListOf()
        override fun getValue(field: Field): Value = unsupported()
        override fun getValues(fields: MutableList<out Field>): MutableMap<Field, Value> = mutableMapOf()
        override fun classObject(): ClassObjectReference = unsupported()
        override fun allLineLocations(): MutableList<Location> = mutableListOf()
        override fun allLineLocations(stratum: String, sourceName: String): MutableList<Location> = mutableListOf()
        override fun locationsOfLine(lineNumber: Int): MutableList<Location> = mutableListOf()
        override fun locationsOfLine(stratum: String, sourceName: String, lineNumber: Int): MutableList<Location> = mutableListOf()

        override fun availableStrata(): MutableList<String> = mutableListOf()
        override fun defaultStratum(): String = "Java"
        override fun instances(maxInstances: Long): MutableList<ObjectReference> = mutableListOf()
        override fun majorVersion(): Int = 0
        override fun minorVersion(): Int = 0
        override fun constantPoolCount(): Int = 0
        override fun constantPool(): ByteArray = byteArrayOf()
        override fun modifiers(): Int = 0
        override fun isPrivate(): Boolean = false
        override fun isPackagePrivate(): Boolean = false
        override fun isProtected(): Boolean = false
        override fun isPublic(): Boolean = false
        override fun compareTo(other: ReferenceType): Int = typeName.compareTo(other.name())
        override fun signature(): String = "L${typeName.replace('.', '/')};"
        override fun virtualMachine(): VirtualMachine = unsupported()
    }

    companion object {
        private fun unsupported(): Nothing = throw UnsupportedOperationException("Not used by this test")

        private val TEST_CLASS = """
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

                goto :goto_4
            .end method
            """.trimIndent()
    }
}
