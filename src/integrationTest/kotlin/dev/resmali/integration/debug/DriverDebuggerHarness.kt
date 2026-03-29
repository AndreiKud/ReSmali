package dev.resmali.integration.debug

import com.intellij.driver.client.Driver
import com.intellij.driver.client.Remote
import com.intellij.driver.client.service
import com.intellij.driver.client.utility
import com.intellij.driver.model.LockSemantics
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.VirtualFile
import com.intellij.driver.sdk.findFile
import com.intellij.driver.sdk.openFile
import com.intellij.driver.sdk.openToolWindow
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.waitFor
import com.intellij.driver.sdk.waitNotNull
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.common.toolwindows.DebugToolWindowUi
import com.intellij.driver.sdk.ui.components.common.toolwindows.debugToolWindow
import com.intellij.driver.sdk.ui.components.elements.tree
import com.intellij.driver.sdk.ui.components.elements.verticalScrollBar
import com.intellij.driver.sdk.ui.xQuery
import com.intellij.ide.starter.driver.engine.BackgroundRun
import dev.resmali.integration.adb.AdbFixture
import dev.resmali.integration.config.IntegrationTestConfig
import dev.resmali.integration.remote.DefaultDebugExecutorRef
import dev.resmali.integration.remote.ProgramRunnerUtilRef
import dev.resmali.integration.remote.ConfigurationTypeUtilRef
import dev.resmali.integration.remote.RunManagerRef
import dev.resmali.integration.remote.RunnerAndConfigurationSettingsRef
import dev.resmali.integration.remote.RunConfigurationRef
import dev.resmali.integration.remote.JavaObjectRef
import dev.resmali.integration.remote.XDebugSessionRef
import dev.resmali.integration.remote.XDebuggerManagerRef
import dev.resmali.integration.remote.XDebuggerUtilRef
import java.nio.file.Files
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

private const val REMOTE_DEBUG_CONFIGURATION_NAME = "IT Remote JDWP"
private const val DEBUG_VARIABLES_TREE_CLASS = "com.intellij.xdebugger.impl.ui.tree.XDebuggerTree"
private const val MAX_ATTACH_ATTEMPTS = 3L

private const val PARAMS_GROUP = ".params"
private const val LOCALS_GROUP = ".locals"

fun BackgroundRun.executeDebugScenario(config: IntegrationTestConfig, fixture: AdbFixture) {
    useDriverAndCloseIde {
        runDebuggerFlow(config, fixture)
    }
}

private fun Driver.runDebuggerFlow(config: IntegrationTestConfig, fixture: AdbFixture) {
    val project = waitForSingleProject(config)
    val breakpointLineZeroBased = (config.breakpointLine - 1).coerceAtLeast(0)

    var settingsToCleanup: RunnerAndConfigurationSettingsRef? = null
    var breakpointSet = false
    var activeSession: XDebugSessionRef? = null

    try {
        openFile(config.breakpointFileRelativePath, project = project, waitForCodeAnalysis = false)

        toggleLineBreakpointWithReadAction(
            project = project,
            relativePath = config.breakpointFileRelativePath,
            line = breakpointLineZeroBased,
        )
        breakpointSet = true

        fixture.setAsDebugApp()
        // fixture.triggerCodePath()
        fixture.launchApp()
        fixture.forwardJdwpToRunningApp()

        val (settings, attachedSession) = attachWithRetries(project, config)
        settingsToCleanup = settings

        // Some IDE builds keep the app in "Waiting For Debugger" even when session state is not reported as suspended.
        // Best-effort resume nudges the VM to continue execution after attach.
        runCatching { attachedSession.resume() }

        val pausedSession = waitForPausedSession(project, settings.getName(), config)
        activeSession = pausedSession

        val hitLocation = assertBreakpointLocation(pausedSession, config)
        val debugTexts = captureDebugPanelTexts()

        // waitFor(timeout = 5.hours, condition = { false })

        assertVariableGroups(debugTexts)
        assertExpectedVariables(debugTexts, config.expectedVariables)

        writeDebugState(
            config = config,
            title = "success",
            content = buildString {
                appendLine("status=success")
                appendLine("session=${pausedSession.getSessionName()}")
                appendLine("breakpointFile=${config.breakpointFileRelativePath}")
                appendLine("breakpointLine=${config.breakpointLine}")
                appendLine("hitFile=${hitLocation.hitFile}")
                appendLine("hitLine=${hitLocation.hitLine}")
                appendLine("expectedVariables=${config.expectedVariables}")
                appendLine("debugTexts:")
                debugTexts.forEach { appendLine(it) }
            },
        )
    } catch (error: Throwable) {
        writeDebugState(
            config = config,
            title = "failure-debug-state",
            content = buildString {
                appendLine("status=failure")
                appendLine("breakpointFile=${config.breakpointFileRelativePath}")
                appendLine("breakpointLine=${config.breakpointLine}")
                appendLine("error=${error::class.qualifiedName}: ${error.message}")
            },
        )
        throw error
    } finally {
        runCatching { activeSession?.stop() }
        runCatching {
            settingsToCleanup?.let { utility(RunManagerRef::class).getInstance(project).removeConfiguration(it) }
        }
        if (breakpointSet) {
            runCatching {
                toggleLineBreakpointWithReadAction(
                    project = project,
                    relativePath = config.breakpointFileRelativePath,
                    line = breakpointLineZeroBased,
                )
            }
        }
    }
}

private fun Driver.waitForSingleProject(config: IntegrationTestConfig): Project {
    return waitNotNull(
        message = "wait for project to open",
        timeout = config.attachTimeoutSeconds.seconds,
    ) {
        runCatching { singleProject() }.getOrNull()
    }
}

private fun Driver.toggleLineBreakpointWithReadAction(project: Project, relativePath: String, line: Int) {
    withContext(
        dispatcher = OnDispatcher.EDT,
        semantics = LockSemantics.READ_ACTION,
    ) {
        val file = findFile(relativePath = relativePath, project = project)
            ?: error("Failed to find file for breakpoint: $relativePath")
        service<XDebuggerUtilRef>().toggleLineBreakpoint(project, file, line, false)
    }
}

private fun Driver.createAndStartRemoteDebugConfiguration(
    project: Project,
    config: IntegrationTestConfig,
): RunnerAndConfigurationSettingsRef {
    return withContext(dispatcher = OnDispatcher.EDT) {
        val runManager = utility(RunManagerRef::class).getInstance(project)
        val configurationTypeUtil = utility(ConfigurationTypeUtilRef::class)
        val remoteType = runCatching { configurationTypeUtil.findConfigurationType("Remote") }
            .getOrElse { configurationTypeUtil.findConfigurationType("javaRemote") }
        val factory = remoteType.getConfigurationFactories().firstOrNull()
            ?: error("RemoteConfigurationType does not expose configuration factories")

        val uniqueName = "$REMOTE_DEBUG_CONFIGURATION_NAME ${System.currentTimeMillis()}"
        val settings = runManager.createConfiguration(uniqueName, factory)
        settings.setTemporary(true)

        val remoteConfiguration = settings.getConfiguration()
        this@createAndStartRemoteDebugConfiguration.setPublicField(remoteConfiguration, "HOST", "127.0.0.1")
        this@createAndStartRemoteDebugConfiguration.setPublicField(remoteConfiguration, "PORT", config.jdwpLocalPort.toString())
        this@createAndStartRemoteDebugConfiguration.setPublicField(remoteConfiguration, "USE_SOCKET_TRANSPORT", true)
        this@createAndStartRemoteDebugConfiguration.setPublicField(remoteConfiguration, "SERVER_MODE", false)
        this@createAndStartRemoteDebugConfiguration.setPublicField(remoteConfiguration, "AUTO_RESTART", false)

        runManager.addConfiguration(settings)
        runManager.setTemporaryConfiguration(settings)

        val executor = utility(DefaultDebugExecutorRef::class).getDebugExecutorInstance()
        utility(ProgramRunnerUtilRef::class).executeConfiguration(settings, executor)

        settings
    }
}

private fun Driver.attachWithRetries(
    project: Project,
    config: IntegrationTestConfig,
): Pair<RunnerAndConfigurationSettingsRef, XDebugSessionRef> {
    val timeoutPerAttemptSeconds = (config.attachTimeoutSeconds / MAX_ATTACH_ATTEMPTS).coerceAtLeast(10L)
    var lastError: Throwable? = null

    for (attempt in 1..MAX_ATTACH_ATTEMPTS) {
        val settings = createAndStartRemoteDebugConfiguration(project, config)

        try {
            val attachedSession = waitForAttachedSession(
                project = project,
                configurationName = settings.getName(),
                timeoutSeconds = timeoutPerAttemptSeconds,
            )
            return settings to attachedSession
        } catch (error: Throwable) {
            lastError = error
            runCatching {
                utility(RunManagerRef::class).getInstance(project).removeConfiguration(settings)
            }
            Thread.sleep(500)
        }
    }

    throw (lastError ?: error("Unable to attach debugger session after $MAX_ATTACH_ATTEMPTS attempts"))
}

private fun Driver.waitForAttachedSession(
    project: Project,
    configurationName: String,
    timeoutSeconds: Long,
): XDebugSessionRef {
    return waitNotNull(
        message = "wait for attached debug session",
        timeout = timeoutSeconds.seconds,
    ) {
        findMatchingSession(project, configurationName)
    }
}

private fun Driver.waitForPausedSession(
    project: Project,
    configurationName: String,
    config: IntegrationTestConfig,
): XDebugSessionRef {
    return waitNotNull(
        message = "wait for debug session suspended on breakpoint",
        timeout = config.hitTimeoutSeconds.seconds,
    ) {
        val session = findMatchingSession(project, configurationName) ?: return@waitNotNull null
        if (!session.isSuspended()) return@waitNotNull null
        if (session.getCurrentStackFrame() == null) return@waitNotNull null
        session
    }
}

private fun Driver.findMatchingSession(project: Project, configurationName: String): XDebugSessionRef? {
    val manager = utility(XDebuggerManagerRef::class).getInstance(project)
    return manager.getDebugSessions().firstOrNull { session ->
        val sessionName = session.getSessionName()
        sessionName == configurationName || sessionName.contains(configurationName)
    }
}

private data class HitLocation(val hitFile: String, val hitLine: Int)

private fun assertBreakpointLocation(session: XDebugSessionRef, config: IntegrationTestConfig): HitLocation {
    val position = requireNotNull(session.getCurrentPosition()) {
        "No current source position in suspended session '${session.getSessionName()}'"
    }

    val actualLine = position.getLine() + 1
    val actualFile = position.getFile().getPath().replace('\\', '/')
    val expectedFileSuffix = config.breakpointFileRelativePath.replace('\\', '/')

    check(actualFile.endsWith(expectedFileSuffix)) {
        "Breakpoint hit file mismatch. Expected suffix '$expectedFileSuffix', actual '$actualFile'"
    }
    check(actualLine == config.breakpointLine) {
        "Breakpoint hit line mismatch. Expected ${config.breakpointLine}, actual $actualLine"
    }

    return HitLocation(hitFile = actualFile, hitLine = actualLine)
}

private fun Driver.captureDebugPanelTexts(): List<String> {
    openToolWindow("Debug")
    val debugUi = ideFrame().debugToolWindow()
    return collectDebugPanelTexts(debugUi)
}

private fun collectDebugPanelTexts(debugUi: DebugToolWindowUi): List<String> {
    val snapshot = linkedSetOf<String>()

    try {
        val debugTree = debugUi.tree(xQuery { byType(DEBUG_VARIABLES_TREE_CLASS) })
        debugTree.waitForNodesLoaded(timeout = 5.seconds)
        debugTree.collectExpandedPaths()
            .filter { it.path[0] == LOCALS_GROUP || it.path[0] == PARAMS_GROUP }
            .flatMap { it.path }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { snapshot.add(it) }
    } catch (error: Throwable) {
        throw IllegalStateException("Failed to capture Debug tool window variables tree snapshot", error)
    }

    return snapshot.toList()
}

private fun hasVariableGroups(debugTexts: List<String>): Boolean {
    return debugTexts.contains(PARAMS_GROUP) && debugTexts.contains(LOCALS_GROUP)
}

private fun hasExpectedVariables(debugTexts: List<String>, expectedVariables: Map<String, String>): Boolean {
    if (expectedVariables.isEmpty()) {
        return true
    }

    return expectedVariables.all { (registerName, expectedValue) ->
        containsExpectedVariable(debugTexts, registerName, expectedValue)
    }
}

private fun assertVariableGroups(debugTexts: List<String>) {
    check(hasVariableGroups(debugTexts)) {
        "Debug panel does not contain both .params and .locals groups. Snapshot:\n${debugTexts.joinToString("\n")}"
    }
}

private fun assertExpectedVariables(debugTexts: List<String>, expectedVariables: Map<String, String>) {
    if (expectedVariables.isEmpty()) {
        return
    }

    val snapshot = debugTexts.joinToString("\n")
    expectedVariables.forEach { (registerName, expectedValue) ->
        check(containsExpectedVariable(debugTexts, registerName, expectedValue)) {
            "Expected variable '$registerName = $expectedValue' not found in debug panel snapshot:\n$snapshot"
        }
    }
}

private fun containsExpectedVariable(debugTexts: List<String>, registerName: String, expectedValue: String): Boolean {
    val matchLine = debugTexts.firstOrNull { it.contains("\\b$registerName\\b".toRegex()) } ?: return false
    return matchLine.trim().split("=")[1].contains(expectedValue)
}

private fun writeDebugState(config: IntegrationTestConfig, title: String, content: String) {
    val outputDir = config.artifactsDir.resolve(config.testName)
    Files.createDirectories(outputDir)
    Files.writeString(outputDir.resolve("$title.txt"), content)
}

private fun Driver.setPublicField(target: RunConfigurationRef, fieldName: String, value: Any) {
    val javaObject = cast(target, JavaObjectRef::class)
    val field = javaObject.getClass().getField(fieldName)
    field.set(target, value)
}
