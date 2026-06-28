package dev.resmali.debugging

import com.intellij.debugger.engine.DebuggerManagerThreadImpl
import com.intellij.debugger.engine.DebuggerUtils
import com.intellij.debugger.engine.evaluation.EvaluationContext
import com.intellij.debugger.engine.events.DebuggerCommandImpl
import com.intellij.debugger.ui.tree.ExtraDebugNodesProvider
import com.intellij.xdebugger.frame.XCompositeNode
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.frame.XValueGroup
import dev.resmali.debugging.utils.RegistersContext
import dev.resmali.debugging.utils.SmaliRegisterValue
import dev.resmali.debugging.value.LazyValue
import dev.resmali.util.SmaliLogger

internal class SmaliExtraDebugNodesProvider : ExtraDebugNodesProvider {
    override fun addExtraNodes(evaluationContext: EvaluationContext, children: XValueChildrenList) {
        val contextElement = DebuggerUtils.getInstance().getContextElement(evaluationContext) ?: return

        val registersContext = RegistersContext.wrap(evaluationContext.project, contextElement) ?: return
        val values = registersContext.getUserData(RegistersContext.SMALI_LAZY_VALUES_KEY) ?: return

        var totalRegistersCount = 0
        var paramRegistersCount = 0
        for (lv in values) {
            if (lv.registerName.startsWith("p")) {
                paramRegistersCount++
            } else {
                totalRegistersCount = Integer.max(totalRegistersCount, lv.registerNumber + 1)
            }
        }
        val firstParamRegister = totalRegistersCount - paramRegistersCount
        val allLazyRegisters = values.filter { it.registerName.startsWith("v") }

        children.addTopGroup(
            createGroup(
                ".params", allLazyRegisters, evaluationContext,
            ) { lazyRegister ->
                if (lazyRegister.registerNumber >= firstParamRegister) {
                    "p${lazyRegister.registerNumber - firstParamRegister} (${lazyRegister.registerName})"
                } else {
                    null
                }
            },
        )
        children.addTopGroup(
            createGroup(
                ".locals", allLazyRegisters, evaluationContext,
            ) { lazyRegister ->
                if (lazyRegister.registerNumber < firstParamRegister) {
                    lazyRegister.registerName
                } else {
                    null
                }
            },
        )
    }

    private fun createGroup(
        name: String,
        allLazyRegisters: List<LazyValue<*>>,
        evaluationContext: EvaluationContext,
        calcName: (LazyValue<*>) -> String?,
    ): XValueGroup {
        val managerThread = evaluationContext.debugProcess.managerThread as DebuggerManagerThreadImpl
        return object : XValueGroup(name) {
            override fun computeChildren(node: XCompositeNode) {
                node.addChildren(XValueChildrenList.EMPTY, false)
                managerThread.schedule(
                    object : DebuggerCommandImpl() {
                        @Throws(Exception::class)
                        override fun action() {
                            if (node.isObsolete) {
                                return
                            }
                            val paramRegisters = collectRegisters(
                                allLazyRegisters,
                                evaluationContext,
                                calcName,
                            )
                            if (paramRegisters != null) {
                                node.addChildren(paramRegisters, true)
                            }
                        }
                    },
                )
            }

            override fun isAutoExpand(): Boolean {
                return true
            }
        }
    }

    private fun collectRegisters(
        allLazyRegisters: List<LazyValue<*>>,
        evaluationContext: EvaluationContext,
        calcName: (LazyValue<*>) -> String?,
    ): XValueChildrenList? {
        val result = XValueChildrenList()
        try {
            for (lazyRegister in allLazyRegisters) {
                lazyRegister.setEvaluationContext(evaluationContext)
                val name = calcName(lazyRegister) ?: continue

                val registerValue = SmaliRegisterValue(name, lazyRegister, evaluationContext)
                result.add(registerValue)
            }
        } catch (ex: Exception) {
            SmaliLogger.INSTANCE.error(ex)
            return null
        }
        return result
    }
}
