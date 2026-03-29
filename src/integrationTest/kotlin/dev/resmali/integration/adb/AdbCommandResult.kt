package dev.resmali.integration.adb

data class AdbCommandResult(
    val command: List<String>,
    val exitCode: Int,
    val output: String,
)
