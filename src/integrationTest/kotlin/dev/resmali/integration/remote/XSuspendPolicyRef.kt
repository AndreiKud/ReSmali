package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.xdebugger.breakpoints.SuspendPolicy")
interface XSuspendPolicyRef {
    fun name(): String
}
