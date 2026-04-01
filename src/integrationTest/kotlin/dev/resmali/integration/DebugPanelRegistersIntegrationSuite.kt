package dev.resmali.integration

import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import dev.resmali.integration.adb.AdbFixture
import dev.resmali.integration.config.IdeBuildType
import dev.resmali.integration.config.IntegrationTestConfig
import dev.resmali.integration.debug.executeDebugScenario
import org.assertj.core.util.Preconditions
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths

class DebugPanelRegistersIntegrationSuite {

    @Test
    fun oldAndroidStudio() {
        val config = IntegrationTestConfig(
            ideProductCode = "AI",
            ideBuildType = IdeBuildType.RELEASE,
            ideBuildNumber = "2025.1.2.11",
        )
        registersAreVisibleWhenBreakpointHit(config)
    }

    @Test
    fun newAndroidStudio() {
        val config = IntegrationTestConfig(
            ideProductCode = "AI",
            ideBuildType = IdeBuildType.RELEASE,
            ideBuildNumber = "2025.2.3.9",
        )
        registersAreVisibleWhenBreakpointHit(config)
    }

    @Test
    fun oldIntelliJIdea() {
        val config = IntegrationTestConfig(
            ideProductCode = "IC",
            ideBuildType = IdeBuildType.RELEASE,
            ideBuildNumber = "252.23892.409",
        )
        registersAreVisibleWhenBreakpointHit(config)
    }

    @Test
    fun newIntelliJIdea() {
        val config = IntegrationTestConfig(
            ideProductCode = "IU",
            ideBuildType = IdeBuildType.EAP,
            ideBuildNumber = "261.22158.46",
        )
        registersAreVisibleWhenBreakpointHit(config)
    }

    fun registersAreVisibleWhenBreakpointHit(config: IntegrationTestConfig) {
        cleanupFixtureIdeaDir(config.smaliProjectPath.toFile())
        val fixture = AdbFixture(config)
        var primaryFailure: Throwable? = null

        try {
            fixture.prepareInstall()
            val ideInfo = resolveIdeInfo(
                productCode = config.ideProductCode,
                buildType = config.ideBuildType,
                buildNumber = config.ideBuildNumber,
            )
            val context = Starter.newContext(
                config.testName,
                TestCase(
                    ideInfo = ideInfo,
                    projectInfo = LocalProjectInfo(config.smaliProjectPath),
                ),
            ).apply {
                PluginConfigurator(this).installPluginFromPath(config.pluginPath)
            }
            patchAndroidStudioDriverClasspath(
                context = context,
                productCode = config.ideProductCode,
            )

            context
                .runIdeWithDriver()
                .executeDebugScenario(config, fixture)
        } catch (error: Throwable) {
            primaryFailure = error
            runCatching { fixture.collectFailureArtifacts(error) }
                .onFailure(error::addSuppressed)
            throw error
        } finally {
            val cleanupErrors = mutableListOf<Throwable>()
            runCatching { fixture.cleanup() }.onFailure(cleanupErrors::add)
            runCatching { cleanupFixtureIdeaDir(config.smaliProjectPath.toFile()) }.onFailure(cleanupErrors::add)

            val failure = primaryFailure
            if (cleanupErrors.isNotEmpty()) {
                if (failure != null) {
                    cleanupErrors.forEach(failure::addSuppressed)
                } else {
                    val rootCleanupError = cleanupErrors.first()
                    cleanupErrors.drop(1).forEach(rootCleanupError::addSuppressed)
                    throw rootCleanupError
                }
            }
        }
    }

    private fun resolveIdeInfo(
        productCode: String,
        buildType: IdeBuildType,
        buildNumber: String?,
    ): IdeInfo {
        val baseIdeInfo = when (productCode.uppercase()) {
            "AI" -> IdeProductProvider.AI
            "IC" -> IdeProductProvider.IC
            "IU" -> IdeProductProvider.IU
            else -> error(
                "Unsupported IDE product code '$productCode'. Supported values: AI, IC, IU",
            )
        }

        val withBuildType = baseIdeInfo.copy(buildType = buildType.starterValue)
        return if (buildNumber.isNullOrBlank()) {
            withBuildType
        } else {
            withBuildType.copy(buildNumber = buildNumber)
        }
    }

    private fun patchAndroidStudioDriverClasspath(context: IDETestContext, productCode: String) {
        if (productCode.uppercase() != "AI") {
            return
        }

        val assertjCoreJar = runCatching {
            Paths.get(Preconditions::class.java.protectionDomain.codeSource.location.toURI())
        }.getOrNull() ?: return

        context.applyVMOptionsPatch {
            addLine("-Xbootclasspath/a:${assertjCoreJar.toAbsolutePath()}")
            addLine("-Dcom.android.adblib.tools.process.properties.collector.delay.default=PT30S")
            addLine("-Dcom.android.adblib.tools.process.properties.collector.delay.short=PT30S")
            addLine("-Dcom.android.adblib.tools.process.properties.collector.delay.use.short=true")
        }
    }

    private fun cleanupFixtureIdeaDir(smaliProjectDir: File) {
        val ideaDir = smaliProjectDir.resolve(".idea")
        if (!ideaDir.exists()) {
            return
        }
        check(ideaDir.deleteRecursively()) {
            "Failed to delete fixture IDE metadata: ${ideaDir.absolutePath}"
        }
    }
}
