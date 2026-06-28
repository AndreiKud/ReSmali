package dev.resmali.debugging

import com.intellij.debugger.engine.JavaDebugAware
import com.intellij.psi.PsiFile
import dev.resmali.SmaliLanguage

internal class SmaliDebugAware : JavaDebugAware() {
    override fun isBreakpointAware(psiFile: PsiFile): Boolean {
        return psiFile.language === SmaliLanguage.INSTANCE
    }
}
