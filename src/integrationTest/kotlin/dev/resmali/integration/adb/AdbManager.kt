package dev.resmali.integration.adb

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

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
        runAdb(listOf("uninstall", packageName))
    }

    fun install(apkPath: Path) {
        require(Files.exists(apkPath)) { "APK does not exist: $apkPath" }
        runAdb(listOf("install", "-r", apkPath.absolutePathString()), timeout = Duration.ofMinutes(2))
    }

    fun launch(packageName: String) {
        runAdb(
            args = listOf("shell", "monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1"),
            timeout = Duration.ofSeconds(30),
        )
    }

    fun setAsDebugApp(packageName: String) {
        runAdb(listOf("shell", "am", "set-debug-app", "-w", "--persistent", packageName))
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

    private fun runAdb(
        args: List<String>,
        timeout: Duration = Duration.ofSeconds(10),
        failOnNonZeroExit: Boolean = true,
    ): AdbCommandResult {
        val command = listOf(adbExecutable) + args
        val process = ProcessBuilder(command).redirectErrorStream(true).start()

        val output = process.inputStream.bufferedReader().use { it.readText() }

        val finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)
        if (!finished) {
            process.destroyForcibly()
            throw AdbCommandException("Command timed out after $timeout: ${command.joinToString(" ")}")
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
