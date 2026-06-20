package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.xdebugger.breakpoints.XBreakpointType")
interface XBreakpointTypeRef {
    fun getId(): String
    fun getTitle(): String
}
