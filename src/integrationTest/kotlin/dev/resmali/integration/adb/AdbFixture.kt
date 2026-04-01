package dev.resmali.integration.adb

import dev.resmali.integration.config.IntegrationTestConfig
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.createDirectories

class AdbFixture(
    private val config: IntegrationTestConfig,
    private val adb: AdbManager = AdbManager(config.adbExecutable),
) {
    private var jdwpForwardActive: Boolean = false
    private val testArtifactsDir: Path = config.artifactsDir.resolve(config.testName)

    fun prepareInstall() {
        testArtifactsDir.createDirectories()
        adb.verifyAdb()
        adb.waitForDevice()
        adb.clearDebugApp()
        adb.removeForward(config.jdwpLocalPort)
        adb.clearLogcat()
        adb.uninstall(config.appPackage)
        adb.install(config.apkPath)
        adb.forceStop(config.appPackage)
    }

    fun forwardJdwpToRunningApp() {
        val pid = waitForPid(
            config.appPackage,
            Duration.ofSeconds(config.attachTimeoutSeconds)
        )
        if (pid == null) {
            error("Could not resolve pid for package ${config.appPackage} before timeout.")
        }

        adb.removeForward(config.jdwpLocalPort)
        adb.forwardJdwp(config.jdwpLocalPort, pid)
        jdwpForwardActive = true
    }

    fun launchApp() {
        adb.launch(config.appPackage)
    }

    fun setAsDebugApp() {
        adb.setAsDebugApp(config.appPackage)
    }

    fun collectFailureArtifacts(error: Throwable) {
        val adbLogPath = testArtifactsDir.resolve("adb-logcat.txt")
        adb.dumpLogcat(adbLogPath)
        val stackTrace = StringWriter().also { writer ->
            error.printStackTrace(PrintWriter(writer))
        }.toString()
        Files.writeString(testArtifactsDir.resolve("failure.txt"), stackTrace)
    }

    fun cleanup() {
        if (jdwpForwardActive) {
            adb.removeForward(config.jdwpLocalPort)
            jdwpForwardActive = false
        }
        adb.forceStop(config.appPackage)
        adb.clearDebugApp()
    }

    private fun waitForPid(packageName: String, timeout: Duration): Int? {
        val deadlineMs = System.currentTimeMillis() + timeout.toMillis()
        while (System.currentTimeMillis() < deadlineMs) {
            val pid = adb.resolvePid(packageName)
            if (pid != null) {
                return pid
            }
            Thread.sleep(250)
        }
        return null
    }
}
