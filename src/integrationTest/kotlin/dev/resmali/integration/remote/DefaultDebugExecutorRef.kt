package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.execution.executors.DefaultDebugExecutor", plugin = "dev.resmali")
interface DefaultDebugExecutorRef {
    fun getDebugExecutorInstance(): ExecutorRef
}
