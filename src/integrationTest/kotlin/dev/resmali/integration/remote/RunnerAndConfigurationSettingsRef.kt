package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.execution.RunnerAndConfigurationSettings")
interface RunnerAndConfigurationSettingsRef {
    fun getName(): String
    fun getConfiguration(): RunConfigurationRef
    fun setTemporary(temporary: Boolean)
}
