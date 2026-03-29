package dev.resmali.integration.remote

import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.VirtualFile

@Remote("com.intellij.xdebugger.XDebuggerUtil")
interface XDebuggerUtilRef {
    fun toggleLineBreakpoint(project: Project, file: VirtualFile, line: Int, temporary: Boolean)
}
