package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.xdebugger.breakpoints.XBreakpointManager")
interface XBreakpointManagerRef {
    fun getAllBreakpoints(): Array<XBreakpointRef>
    fun removeBreakpoint(breakpoint: XBreakpointRef)
}
