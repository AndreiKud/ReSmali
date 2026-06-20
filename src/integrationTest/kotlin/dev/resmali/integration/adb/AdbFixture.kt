package dev.resmali.integration.adb

import dev.resmali.integration.config.IntegrationTestConfig
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.createDirectories
import org.w3c.dom.Element
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilderFactory

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

    fun tapBreakpointTrigger(): Boolean {
        val bounds = findViewBounds(adb.dumpWindowHierarchy(), config.breakpointTriggerResourceId)
            ?: return false
        adb.tap(bounds.centerX, bounds.centerY)
        return true
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

    private fun findViewBounds(windowHierarchy: String, resourceId: String): UiBounds? {
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(windowHierarchy)))
        val nodes = document.getElementsByTagName("node")
        for (i in 0 until nodes.length) {
            val node = nodes.item(i) as? Element ?: continue
            if (node.getAttribute("resource-id") != resourceId) {
                continue
            }
            return parseBounds(node.getAttribute("bounds"))
        }
        return null
    }

    private fun parseBounds(rawBounds: String): UiBounds? {
        val match = BOUNDS_REGEX.matchEntire(rawBounds) ?: return null
        val (left, top, right, bottom) = match.destructured
        return UiBounds(
            left = left.toInt(),
            top = top.toInt(),
            right = right.toInt(),
            bottom = bottom.toInt(),
        )
    }

    private data class UiBounds(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
    ) {
        val centerX: Int get() = left + (right - left) / 2
        val centerY: Int get() = top + (bottom - top) / 2
    }

    companion object {
        private val BOUNDS_REGEX = Regex("""\[(\d+),(\d+)]\[(\d+),(\d+)]""")
    }
}
