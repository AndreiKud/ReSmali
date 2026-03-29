package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.execution.configurations.ConfigurationType")
interface ConfigurationTypeRef {
    fun getId(): String
    fun getConfigurationFactories(): Array<ConfigurationFactoryRef>
}
