package dev.resmali.integration.config

import org.junit.jupiter.api.Assumptions
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

enum class IdeBuildType(val starterValue: String) {
    RELEASE("release"),
    RC("rc"),
    PREVIEW("preview"),
    EAP("eap"),
    NIGHTLY("nightly");
}

data class IntegrationTestConfig(
    val ideProductCode: String,
    val ideBuildNumber: String?,
    val ideBuildType: IdeBuildType = IdeBuildType.RELEASE,
    val testName: String = "smali-debug-attach",
    val pluginPath: Path = resolvePluginPath(),
    val starterCheckoutPath: Path = resolvePath(".intellijPlatform"),
    val apkPath: Path = resolvePath("src/integrationTest/resources/test-app/app-debug.apk"),
    val smaliProjectPath: Path = resolvePath("src/integrationTest/resources/smali-project"),
    val appPackage: String = "com.example.smalisample",
    val breakpointTriggerResourceId: String = "$appPackage:id/btn",
    val adbExecutable: String = "adb",
    val jdwpLocalPort: Int = 8706,
    val breakpointFileRelativePath: String = "com/example/smalisample/MainActivity.smali",
    val breakpointLine: Int = 137,
    val expectedVariables: Map<String, String> = linkedMapOf(
        "p0" to "MainActivity",
        "v0" to "MainActivity",
        "v1" to "Object[3]",
        "v2" to "Byte",
        "v3" to "Double",
        "v4" to "18",
        "v5" to "1000",
        "v6" to "2000",
        "v8" to "0.6",
        "v9" to "3.14",
        "v11" to "null",
        "v12" to "100",
        "v13" to "\"exp11\"",
        "v14" to "FrameLayout",
        "v15" to "long[3]",
        "v16" to "true",
        "v17" to "17",
        "v18" to "int[",
        "v19" to "double[",
        "v20" to "b",
    ),
    val attachTimeoutSeconds: Long = 15,
    val hitTimeoutSeconds: Long = 15,
    val artifactsDir: Path = resolvePath("build/integration-test-artifacts"),
) {

    init {
        val normalizedPluginPath = pluginPath.toAbsolutePath().normalize()
        val normalizedApkPath = apkPath.toAbsolutePath().normalize()
        val normalizedSmaliProjectPath = smaliProjectPath.toAbsolutePath().normalize()

        Assumptions.assumeTrue(Files.exists(normalizedPluginPath), "Missing plugin archive: $normalizedPluginPath")
        Assumptions.assumeTrue(Files.exists(normalizedApkPath), "Missing APK: $normalizedApkPath")
        Assumptions.assumeTrue(
            Files.isDirectory(normalizedSmaliProjectPath),
            "Missing smali project dir: $normalizedSmaliProjectPath",
        )
        require(breakpointFileRelativePath.isNotBlank()) {
            "Breakpoint file path must not be blank"
        }
        val breakpointPath = normalizedSmaliProjectPath.resolve(breakpointFileRelativePath).normalize()
        Assumptions.assumeTrue(Files.exists(breakpointPath), "Missing breakpoint file: $breakpointPath")
    }

    companion object {
        private const val PLUGIN_PATH_PROPERTY = "resmali.integration.plugin.path"

        private fun resolvePath(relativePath: String): Path {
            return Paths.get(relativePath).toAbsolutePath().normalize()
        }

        private fun resolvePluginPath(): Path {
            val explicitPluginPath: String? = System.getProperty(PLUGIN_PATH_PROPERTY)
            require(!explicitPluginPath.isNullOrBlank()) {
                "JVM property '$PLUGIN_PATH_PROPERTY' must not be blank"
            }
            return Paths.get(explicitPluginPath.trim()).toAbsolutePath().normalize()
        }
    }
}
