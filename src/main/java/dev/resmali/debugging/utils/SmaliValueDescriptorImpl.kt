package dev.resmali.debugging.utils

import com.intellij.debugger.DebuggerContext
import com.intellij.debugger.JavaDebuggerBundle
import com.intellij.debugger.engine.evaluation.EvaluateException
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.impl.PositionUtil
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiExpression
import com.intellij.util.IncorrectOperationException
import com.sun.jdi.ObjectReference
import com.sun.jdi.Type
import com.sun.jdi.Value

class SmaliValueDescriptorImpl(
    project: Project?,
    private val variableName: String?,
    private val objRef: ObjectReference,
) : ValueDescriptorImpl(
    project,
    objRef,
) {
    override fun calcValue(ctx: EvaluationContextImpl?): Value {
        return objRef
    }

    override fun calcValueName(): String? {
        return variableName
    }

    @Throws(EvaluateException::class)
    override fun getDescriptorEvaluation(context: DebuggerContext?): PsiExpression {
        val elementFactory = JavaPsiFacade.getElementFactory(myProject)
        try {
            return elementFactory.createExpressionFromText(name, PositionUtil.getContextElement(context))
        } catch (e: IncorrectOperationException) {
            throw EvaluateException(JavaDebuggerBundle.message("error.invalid.local.variable.name", name), e)
        }
    }

    override fun getType(): Type? {
        return objRef.type()
    }
}
