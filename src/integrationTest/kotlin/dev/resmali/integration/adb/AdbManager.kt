package dev.resmali.integration.adb

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.concurrent.thread

class AdbManager(
    private val adbExecutable: String = "adb",
) {
    fun verifyAdb() {
        runAdb(listOf("version"), timeout = Duration.ofSeconds(10))
    }

    fun waitForDevice() {
        runAdb(listOf("wait-for-device"), timeout = Duration.ofMinutes(2))
    }

    fun clearLogcat() {
        runAdb(listOf("logcat", "-c"))
    }

    fun uninstall(packageName: String) {
        if (!isPackageInstalled(packageName)) {
            return
        }
        runAdb(listOf("uninstall", packageName))
    }

    fun install(apkPath: Path) {
        require(Files.exists(apkPath)) { "APK does not exist: $apkPath" }
        runAdb(listOf("install", "-r", apkPath.absolutePathString()), timeout = Duration.ofMinutes(2))
    }

    fun launch(packageName: String) {
        runAdb(
            args = listOf("shell", "monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1"),
            timeout = Duration.ofSeconds(15),
        )
    }

    fun setAsDebugApp(packageName: String) {
        runAdb(listOf("shell", "am", "set-debug-app", "-w", packageName))
    }

    fun clearDebugApp() {
        runAdb(listOf("shell", "am", "clear-debug-app"))
    }

    fun forceStop(packageName: String) {
        runAdb(listOf("shell", "am", "force-stop", packageName))
    }

    fun resolvePid(packageName: String): Int? {
        val result = runAdb(
            args = listOf("shell", "pidof", "-s", packageName),
            failOnNonZeroExit = false,
        )
        if (result.exitCode != 0) {
            return null
        }
        return result.output.trim().toIntOrNull()
    }

    fun forwardJdwp(localPort: Int, pid: Int) {
        runAdb(listOf("forward", "tcp:$localPort", "jdwp:$pid"))
    }

    fun removeForward(localPort: Int) {
        val result = runAdb(
            args = listOf("forward", "--remove", "tcp:$localPort"),
            failOnNonZeroExit = false,
        )
        if (result.exitCode == 0) {
            return
        }

        val output = result.output.lowercase()
        if ("not found" !in output && "listener" !in output) {
            throw AdbCommandException(
                "Command failed with exit code ${result.exitCode}: ${result.command.joinToString(" ")}\n${result.output}",
            )
        }
    }

    fun sendBroadcast(action: String, targetPackage: String?, stringExtras: Map<String, String> = emptyMap()) {
        val args = mutableListOf("shell", "am", "broadcast", "-a", action)
        if (!targetPackage.isNullOrBlank()) {
            args += listOf("-p", targetPackage)
        }
        for ((key, value) in stringExtras) {
            args += listOf("--es", key, value)
        }
        runAdb(args)
    }

    fun dumpLogcat(outputFile: Path) {
        outputFile.parent?.let { Files.createDirectories(it) }
        val result = runAdb(
            args = listOf("logcat", "-d", "-v", "threadtime"),
            timeout = Duration.ofSeconds(30),
        )
        Files.writeString(outputFile, result.output)
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        val result = runAdb(
            args = listOf("shell", "pm", "path", packageName),
            failOnNonZeroExit = false,
        )
        val output = result.output.lowercase()
        if ("package:" in output) {
            return true
        }
        if ("not found" in output || "unknown package" in output || "can't find package" in output) {
            return false
        }
        return result.exitCode == 0 && output.isNotBlank()
    }

    private fun runAdb(
        args: List<String>,
        timeout: Duration = Duration.ofSeconds(10),
        failOnNonZeroExit: Boolean = true,
    ): AdbCommandResult {
        val command = listOf(adbExecutable) + args
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        var output = ""
        var readFailure: Throwable? = null
        val outputReader = thread(start = true, isDaemon = true, name = "adb-output-reader") {
            runCatching {
                process.inputStream.bufferedReader().use { it.readText() }
            }.onSuccess { captured ->
                output = captured
            }.onFailure { error ->
                readFailure = error
            }
        }

        val finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)
        if (!finished) {
            process.destroyForcibly()
            process.waitFor(1, TimeUnit.SECONDS)
            outputReader.join(1_000)
            throw AdbCommandException("Command timed out after $timeout: ${command.joinToString(" ")}")
        }
        outputReader.join(1_000)
        readFailure?.let { error ->
            throw AdbCommandException(
                "Failed to read command output for: ${command.joinToString(" ")}\n${error.message}",
            )
        }

        val exitCode = process.exitValue()
        val result = AdbCommandResult(command = command, exitCode = exitCode, output = output)

        if (failOnNonZeroExit && exitCode != 0) {
            throw AdbCommandException(
                "Command failed with exit code $exitCode: ${command.joinToString(" ")}\n$output",
            )
        }

        return result
    }
}
