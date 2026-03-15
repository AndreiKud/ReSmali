package dev.resmali.debugging;

import com.intellij.debugger.engine.JavaDebugAware;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import dev.resmali.SmaliLanguage;

public class SmaliDebugAware extends JavaDebugAware {
    @Override
    public boolean isBreakpointAware(@NotNull PsiFile psiFile) {
        return psiFile.getLanguage() == SmaliLanguage.INSTANCE;
    }
}
