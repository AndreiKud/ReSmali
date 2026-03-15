package dev.resmali.debugging.utils;

import com.intellij.debugger.DebuggerContext;
import com.intellij.debugger.JavaDebuggerBundle;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl;
import com.intellij.debugger.impl.PositionUtil;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.util.IncorrectOperationException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

public class SmaliValueDescriptorImpl extends ValueDescriptorImpl {
    private final String name;
    private final ObjectReference objRef;

    public SmaliValueDescriptorImpl(Project project, String name, ObjectReference objRef) {
        super(project, objRef);
        this.name = name;
        this.objRef = objRef;
    }

    @Override public Value calcValue(EvaluationContextImpl ctx) {
        return objRef;
    }

    @Override public String calcValueName() {
        return name;
    }

    @Override public PsiExpression getDescriptorEvaluation(DebuggerContext context) throws EvaluateException {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(myProject);
        try {
            return elementFactory.createExpressionFromText(getName(), PositionUtil.getContextElement(context));
        } catch (IncorrectOperationException e) {
            throw new EvaluateException(JavaDebuggerBundle.message("error.invalid.local.variable.name", getName()), e);
        }
    }

    @Override public Type getType() {
        return objRef.type();
    }
}
