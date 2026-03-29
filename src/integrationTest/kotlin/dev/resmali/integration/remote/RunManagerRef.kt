package dev.resmali.integration.remote

import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.Project

@Remote("com.intellij.execution.RunManager")
interface RunManagerRef {
    fun getInstance(project: Project): RunManagerRef
    fun createConfiguration(name: String, factory: ConfigurationFactoryRef): RunnerAndConfigurationSettingsRef
    fun addConfiguration(settings: RunnerAndConfigurationSettingsRef)
    fun setTemporaryConfiguration(settings: RunnerAndConfigurationSettingsRef?)
    fun removeConfiguration(settings: RunnerAndConfigurationSettingsRef?)
}
