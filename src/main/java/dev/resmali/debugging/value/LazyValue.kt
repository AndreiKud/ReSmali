/*
 * Copyright 2016, Google Inc.
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
package dev.resmali.debugging.value

import com.intellij.debugger.DebuggerManagerEx
import com.intellij.debugger.engine.evaluation.EvaluateException
import com.intellij.debugger.engine.evaluation.EvaluationContext
import com.intellij.debugger.jdi.VirtualMachineProxyImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable
import com.jetbrains.jdi.LocalVariableImpl
import com.jetbrains.jdi.SlotLocalVariable
import com.jetbrains.jdi.StackFrameImpl
import com.sun.jdi.AbsentInformationException
import com.sun.jdi.ObjectReference
import com.sun.jdi.Type
import com.sun.jdi.Value
import com.sun.jdi.VirtualMachine
import dev.resmali.debugging.utils.RegisterSlotUtils
import dev.resmali.psi.impl.SmaliMethod
import dev.resmali.util.SmaliLogger
import java.lang.reflect.Field
import javax.annotation.Nonnull

open class LazyValue<T : Value>(
    val method: SmaliMethod,
    protected val project: Project,
    val registerNumber: Int,
    val registerName: String,
    protected val type: String,
) : Value {
    private var evaluationContext: EvaluationContext? = null
    private var value: Value? = null

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun getNullableValue(allowNull: Boolean): T? {
        if (value == null) {
            try {
                val context = evaluationContext ?: DebuggerManagerEx.getInstanceEx(project).context.createEvaluationContext()
                if (context == null) {
                    check(allowNull) { "Can't create evaluation context for $this" }
                    return null
                }
                evaluationContext = context
                value = evaluateRegister(context, method, registerNumber)
            } catch (ex: EvaluateException) {
                SmaliLogger.INSTANCE.debug("Failed to evaluate register $registerName", ex)
            } finally {
                evaluationContext = null
            }
        }
        check(value != null || allowNull) { "Null value is not allowed for $this" }
        return value as? T
    }

    protected fun getValue(): T {
        return checkNotNull(getNullableValue(false))
    }

    override fun type(): Type {
        return checkNotNull(getValue().type())
    }

    override fun virtualMachine(): VirtualMachine? {
        val debugProcess = evaluationContext?.debugProcess ?: DebuggerManagerEx.getInstanceEx(project).context.debugProcess
        return (debugProcess?.virtualMachineProxy as? VirtualMachineProxyImpl)?.virtualMachine
    }

    fun setEvaluationContext(@Nonnull evaluationContext: EvaluationContext) {
        this.evaluationContext = evaluationContext
    }

    override fun equals(other: Any?): Boolean {
        return getNullableValue(true)?.equals(other) ?: super.equals(other)
    }

    override fun hashCode(): Int {
        return getNullableValue(true)?.hashCode() ?: super.hashCode()
    }

    override fun toString(): String {
        return getNullableValue(true)?.toString() ?: "null"
    }

    @Throws(EvaluateException::class)
    fun evaluateRegister(context: EvaluationContext, smaliMethod: SmaliMethod, registerNum: Int): Value? {
        val registerCount = ApplicationManager.getApplication().runReadAction(
            ThrowableComputable<Int, EvaluateException> { smaliMethod.registerCount },
        )
        if (registerNum >= registerCount) {
            return null
        }

        val frameProxy = context.frameProxy ?: return null
        val currentLocation = frameProxy.location() ?: return null

        val method = currentLocation.method()

        val methodSize = smaliMethod.instructions.sumOf { it.instructionSize }
        if (((methodSize / 2) - 1 downTo 0).firstNotNullOfOrNull { method.locationOfCodeIndex(it.toLong()) } == null) {
            SmaliLogger.INSTANCE.debug("Null location for register $registerName in method ${method.name()}")
            return null
        }

        val frameImpl = frameProxy.stackFrame as StackFrameImpl
        val slotNumber = RegisterSlotUtils.mapForVirtualMachine(
            frameImpl.virtualMachine(),
            smaliMethod,
            registerNum,
        )

        val strategies = listOf(
            { tryGetValueByMatchingVisibleVariable(frameImpl, slotNumber) },
            { tryGetValueByDexlibType(frameImpl, slotNumber) },
            { tryGetValueByGuessingPrimitive(frameImpl, slotNumber) },
        )
        for (strategy in strategies) {
            try {
                return strategy()
            } catch (e: Exception) {
                if (e !is ValueNotFoundException) {
                    SmaliLogger.INSTANCE.debug(e)
                }
            }
        }
        return null
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class, AbsentInformationException::class, ValueNotFoundException::class)
    private fun tryGetValueByMatchingVisibleVariable(frameImpl: StackFrameImpl, slotNumber: Int): Value? {
        try {
            val signature = frameImpl.visibleVariables().firstOrNull { slotField.get(it) as Int == slotNumber }?.signature()
                ?: throw ValueNotFoundException()
            return getSlotValue(frameImpl, slotNumber, signature)
        } catch (e: NoSuchFieldException) {
            SmaliLogger.INSTANCE.error(e)
            throw e
        } catch (e: IllegalAccessException) {
            SmaliLogger.INSTANCE.error(e)
            throw e
        }
    }

    private fun tryGetValueByDexlibType(frameImpl: StackFrameImpl, slotNumber: Int): Value? {
        return getSlotValue(frameImpl, slotNumber, type)
    }

    // Primitive types from the DEX library are guessed by literal value, which is not always accurate.
    private fun tryGetValueByGuessingPrimitive(frameImpl: StackFrameImpl, slotNumber: Int): Value? {
        val guessedType = when (type) {
            "I" -> "F"
            "J" -> "D"
            else -> "I"
        }
        return getSlotValue(frameImpl, slotNumber, guessedType)
    }

    private fun getSlotValue(frameImpl: StackFrameImpl, slotNumber: Int, signature: String): Value? {
        val slotLocalVariable = object : SlotLocalVariable {
            override fun slot(): Int = slotNumber
            override fun signature(): String = signature
        }
        return frameImpl.getSlotsValues(listOf(slotLocalVariable)).first()
    }

    private class ValueNotFoundException : Exception()

    companion object {
        private val slotField: Field by lazy {
            LocalVariableImpl::class.java.getDeclaredField("slot").apply {
                isAccessible = true
            }
        }

        fun create(
            @Nonnull method: SmaliMethod,
            @Nonnull project: Project,
            registerNumber: Int,
            registerName: String,
            @Nonnull type: String,
        ): LazyValue<*> = when {
            type == "B" -> LazyByteValue(method, project, registerNumber, registerName, type)
            type == "S" -> LazyShortValue(method, project, registerNumber, registerName, type)
            type == "J" -> LazyLongValue(method, project, registerNumber, registerName, type)
            type == "I" -> LazyIntegerValue(method, project, registerNumber, registerName, type)
            type == "F" -> LazyFloatValue(method, project, registerNumber, registerName, type)
            type == "D" -> LazyDoubleValue(method, project, registerNumber, registerName, type)
            type == "Z" -> LazyBooleanValue(method, project, registerNumber, registerName, type)
            type == "C" -> LazyCharValue(method, project, registerNumber, registerName, type)
            type == "V" -> LazyVoidValue(method, project, registerNumber, registerName, type)
            type.startsWith("[") -> LazyArrayReference(method, project, registerNumber, registerName, type)
            type == "Ljava/lang/String;" -> LazyStringReference(method, project, registerNumber, registerName, type)
            type == "Ljava/lang/Class;" -> LazyClassObjectReference(method, project, registerNumber, registerName, type)
            type == "Ljava/lang/ThreadGroup;" -> LazyThreadGroupReference(method, project, registerNumber, registerName, type)
            type == "Ljava/lang/Thread;" -> LazyThreadReference(method, project, registerNumber, registerName, type)
            type == "Ljava/lang/ClassLoader;" -> LazyClassLoaderReference(method, project, registerNumber, registerName, type)
            type.startsWith("L") -> LazyObjectReference<ObjectReference>(method, project, registerNumber, registerName, type)
            else -> LazyValue<Value>(method, project, registerNumber, registerName, type)
        }
    }
}
