package dev.resmali.integration.remote

import com.intellij.driver.client.Remote

@Remote("java.lang.Object")
interface JavaObjectRef {
    fun getClass(): JavaClassRef
}
