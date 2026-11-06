package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.xdebugger.frame.XStackFrame", plugin = "dev.resmali")
interface XStackFrameRef
