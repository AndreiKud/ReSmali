package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.xdebugger.XDebugSession")
interface XDebugSessionRef {
    fun getSessionName(): String
    fun isSuspended(): Boolean
    fun getCurrentPosition(): XSourcePositionRef?
    fun getCurrentStackFrame(): XStackFrameRef?
    fun resume()
    fun stop()
}
