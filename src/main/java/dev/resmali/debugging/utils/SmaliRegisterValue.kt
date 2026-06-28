package dev.resmali.debugging.utils

import com.intellij.debugger.engine.ContextUtil
import com.intellij.debugger.engine.DebugProcessImpl
import com.intellij.debugger.engine.DebuggerUtils
import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.debugger.engine.evaluation.CodeFragmentKind
import com.intellij.debugger.engine.evaluation.EvaluationContext
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.engine.evaluation.TextWithImports
import com.intellij.debugger.engine.evaluation.TextWithImportsImpl
import com.intellij.debugger.engine.events.DebuggerCommandImpl
import com.intellij.debugger.engine.events.SuspendContextCommandImpl
import com.intellij.debugger.jdi.LocalVariablesUtil
import com.intellij.debugger.jdi.StackFrameProxyImpl
import com.intellij.openapi.util.NlsSafe
import com.intellij.xdebugger.XExpression
import com.intellij.xdebugger.frame.XCompositeNode
import com.intellij.xdebugger.frame.XNamedValue
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.frame.XValueModifier
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.jetbrains.jdi.SlotLocalVariable
import com.sun.jdi.CharValue
import com.sun.jdi.ObjectReference
import com.sun.jdi.PrimitiveValue
import com.sun.jdi.StringReference
import com.sun.jdi.Value
import dev.resmali.SmaliFileType
import dev.resmali.debugging.SmaliCodeFragmentFactory
import dev.resmali.debugging.value.LazyValue

private const val DEBUGGER_DEFAULT_IMPORTS = "java.lang.Boolean,java.lang.Byte,java.lang.Character,java.lang.Double,java.lang.Float," +
        "java.lang.Integer,java.lang.Long,java.lang.Short,java.lang.String"

class SmaliRegisterValue(
    @NlsSafe name: @NlsSafe String,
    private val lazyValue: LazyValue<*>,
    private val evaluationContext: EvaluationContext,
) : XNamedValue(name) {
    private var value: Value? = null

    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        val managerThread = (evaluationContext.debugProcess as DebugProcessImpl).managerThread
        managerThread.schedule(
            object : DebuggerCommandImpl() {
                @Throws(Exception::class)
                override fun action() {
                    if (node.isObsolete) {
                        return
                    }
                    lazyValue.setEvaluationContext(evaluationContext)
                    value = lazyValue.getNullableValue(true)

                    val currentValue = value
                    if (currentValue == null) {
                        node.setPresentation(null, null, "null", false)
                        return
                    }

                    val type = currentValue.type()
                    val hasChildren = currentValue is ObjectReference
                    node.setPresentation(null, type.name(), currentValue.toString(), hasChildren)
                }
            },
        )
    }

    override fun computeChildren(node: XCompositeNode) {
        val managerThread = (evaluationContext.debugProcess as DebugProcessImpl).managerThread
        managerThread.schedule(
            object : DebuggerCommandImpl() {
                override fun action() {
                    if (node.isObsolete) {
                        return
                    }
                    val objectValue = value as? ObjectReference
                    if (objectValue == null) {
                        node.addChildren(XValueChildrenList.EMPTY, true)
                        return
                    }

                    val debugProcess = evaluationContext.debugProcess as DebugProcessImpl
                    val nodeManager = checkNotNull(debugProcess.xdebugProcess).nodeManager
                    val descriptor = SmaliValueDescriptorImpl(
                        evaluationContext.project,
                        lazyValue.registerName,
                        objectValue,
                    )

                    // TODO: SmaliRegisterModifier for children to use registers in evaluations
                    val javaValue = JavaValue.create(
                        null,
                        descriptor,
                        evaluationContext as EvaluationContextImpl,
                        nodeManager,
                        true,
                    )
                    javaValue.computeChildren(node)
                }
            },
        )
    }

    override fun getModifier(): XValueModifier? {
        // TODO: Signature without an actual value?
        if (value == null) {
            return null
        }
        return SmaliRegisterModifier()
    }

    internal inner class SmaliRegisterModifier : XValueModifier() {
        override fun calculateInitialValueEditorText(callback: XInitialValueCallback) {
            when (val currentValue = value) {
                null -> callback.setValue("null")
                is CharValue -> callback.setValue("'${currentValue.value()}'")
                is PrimitiveValue -> callback.setValue(currentValue.toString())
                is StringReference -> callback.setValue("\"${currentValue.value()}\"")
                else -> callback.setValue(null)
            }
        }

        override fun setValue(expression: XExpression, callback: XModificationCallback) {
            val managerThread = (evaluationContext.debugProcess as DebugProcessImpl).managerThread
            managerThread.schedule(
                object : SuspendContextCommandImpl(evaluationContext.suspendContext as SuspendContextImpl) {
                    override fun contextAction(suspendContext: SuspendContextImpl) {
                        try {
                            val expr = expression.expression
                            val textWithImports: TextWithImports = TextWithImportsImpl(
                                CodeFragmentKind.CODE_BLOCK,
                                expr,
                                DEBUGGER_DEFAULT_IMPORTS,
                                SmaliFileType,
                            )
                            val project = evaluationContext.project
                            val codeFragmentFactory = SmaliCodeFragmentFactory()
                            val contextElement = DebuggerUtils.getInstance().getContextElement(evaluationContext)
                            val ctx = RegistersContext.wrap(project, contextElement)
                            val codeFragment = codeFragmentFactory.createPsiCodeFragmentImpl(textWithImports, ctx, project)

                            val evaluator = codeFragmentFactory.evaluatorBuilder.build(
                                codeFragment,
                                ContextUtil.getSourcePosition(evaluationContext),
                            )

                            var newValue = evaluator.evaluate(evaluationContext)
                            val frameProxy = checkNotNull(evaluationContext.frameProxy as? StackFrameProxyImpl)
                            val frame = frameProxy.stackFrame

                            val slot = RegisterSlotUtils.mapForVirtualMachine(
                                frame.virtualMachine(),
                                lazyValue.method,
                                lazyValue.registerNumber,
                            )
                            val slotVariable: SlotLocalVariable = object : SlotLocalVariable {
                                override fun slot(): Int {
                                    return slot
                                }

                                override fun signature(): String? {
                                    return lazyValue.type().signature()
                                }
                            }

                            if (newValue is LazyValue<*>) {
                                newValue = newValue.getNullableValue(true)
                            }

                            LocalVariablesUtil.setValue(frame, slotVariable, newValue)
                            callback.valueModified()
                        } catch (e: Exception) {
                            callback.errorOccurred(e.message ?: e.javaClass.simpleName)
                        }
                    }
                },
            )
        }
    }
}
