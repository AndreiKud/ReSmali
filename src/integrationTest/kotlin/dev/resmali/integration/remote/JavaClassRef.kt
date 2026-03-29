package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("java.lang.Class")
interface JavaClassRef {
    fun getField(name: String): JavaFieldRef
}
