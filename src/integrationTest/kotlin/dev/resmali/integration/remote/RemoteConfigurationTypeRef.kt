package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote(value = "com.intellij.execution.remote.RemoteConfigurationType", plugin = "com.intellij.java")
interface RemoteConfigurationTypeRef {
    fun getInstance(): RemoteConfigurationTypeRef
    fun getConfigurationFactories(): Array<ConfigurationFactoryRef>
}
