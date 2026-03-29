package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("com.intellij.execution.configurations.ConfigurationTypeUtil")
interface ConfigurationTypeUtilRef {
    fun findConfigurationType(id: String): ConfigurationTypeRef
}
