package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("java.lang.reflect.Field")
interface JavaFieldRef {
    fun set(instance: Any, value: Any?)
}
