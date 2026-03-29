package dev.resmali.integration.remote

import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.VirtualFile

@Remote("com.intellij.xdebugger.XSourcePosition")
interface XSourcePositionRef {
    fun getLine(): Int
    fun getFile(): VirtualFile
}
