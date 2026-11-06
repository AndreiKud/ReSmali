package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.xdebugger.breakpoints.XBreakpoint", plugin = "dev.resmali")
interface XBreakpointRef {
    fun isEnabled(): Boolean
    fun getType(): XBreakpointTypeRef
    fun getSourcePosition(): XSourcePositionRef?
    fun getSuspendPolicy(): XSuspendPolicyRef
}
