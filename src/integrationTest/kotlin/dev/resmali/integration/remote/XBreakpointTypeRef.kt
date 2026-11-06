package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.xdebugger.breakpoints.XBreakpointType", plugin = "dev.resmali")
interface XBreakpointTypeRef {
    fun getId(): String
    fun getTitle(): String
}
