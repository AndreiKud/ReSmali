package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote(value = "com.intellij.execution.remote.RemoteConfiguration", plugin = "com.intellij.java")
interface RemoteConfigurationRef : RunConfigurationRef {
    fun getClass(): JavaClassRef
}
