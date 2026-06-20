package dev.resmali.integration.remote

import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.Project

@Remote("com.intellij.xdebugger.XDebuggerManager")
interface XDebuggerManagerRef {
    fun getInstance(project: Project): XDebuggerManagerRef
    fun getDebugSessions(): Array<XDebugSessionRef>
    fun getBreakpointManager(): XBreakpointManagerRef
}
