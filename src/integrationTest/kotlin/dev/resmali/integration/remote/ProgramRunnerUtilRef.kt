package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.execution.ProgramRunnerUtil")
interface ProgramRunnerUtilRef {
    fun executeConfiguration(configuration: RunnerAndConfigurationSettingsRef, executor: ExecutorRef)
}
