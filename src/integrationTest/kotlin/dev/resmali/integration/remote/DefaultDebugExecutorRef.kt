package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.execution.executors.DefaultDebugExecutor")
interface DefaultDebugExecutorRef {
    fun getDebugExecutorInstance(): ExecutorRef
}
