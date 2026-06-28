package dev.resmali.integration.debug

import com.intellij.driver.client.Driver
import com.intellij.driver.client.service
import com.intellij.driver.model.LockSemantics
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.findFile
import com.intellij.driver.sdk.openFile
import com.intellij.driver.sdk.openToolWindow
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.common.toolwindows.DebugToolWindowUi
import com.intellij.driver.sdk.ui.components.common.toolwindows.debugToolWindow
import com.intellij.driver.sdk.ui.components.elements.tree
import com.intellij.driver.sdk.ui.xQuery
import com.intellij.driver.sdk.waitNotNull
import com.intellij.ide.starter.driver.engine.BackgroundRun
import dev.resmali.integration.adb.AdbFixture
import dev.resmali.integration.config.IntegrationTestConfig
import dev.resmali.integration.remote.ConfigurationTypeUtilRef
import dev.resmali.integration.remote.DefaultDebugExecutorRef
import dev.resmali.integration.remote.JavaObjectRef
import dev.resmali.integration.remote.ProgramRunnerUtilRef
import dev.resmali.integration.remote.RunConfigurationRef
import dev.resmali.integration.remote.RunManagerRef
import dev.resmali.integration.remote.RunnerAndConfigurationSettingsRef
import dev.resmali.integration.remote.XBreakpointRef
import dev.resmali.integration.remote.XDebugSessionRef
import dev.resmali.integration.remote.XDebuggerManagerRef
import dev.resmali.integration.remote.XDebuggerUtilRef
import java.nio.file.Files
import kotlin.time.Duration.Companion.seconds

private const val REMOTE_DEBUG_CONFIGURATION_NAME = "IT Remote JDWP"
private const val DEBUG_VARIABLES_TREE_CLASS = "com.intellij.xdebugger.impl.ui.tree.XDebuggerTree"
private const val MAX_ATTACH_ATTEMPTS = 3
private const val BREAKPOINT_TRIGGER_INTERVAL_MS = 1_000L

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
        openFile(config.breakpointFileRelativePath, project = project, waitForCodeAnalysis = true)

        setLineBreakpointWithReadAction(
            project = project,
            config = config,
            line = breakpointLineZeroBased,
        )
        breakpointSet = true

        fixture.setAsDebugApp()
        fixture.launchApp()
        fixture.forwardJdwpToRunningApp()

        val (settings, attachedSession) = attachWithRetries(project, config)
        settingsToCleanup = settings

        // Some IDE builds keep the app in "Waiting For Debugger" even when session state is not reported as suspended.
        // Best-effort resume nudges the VM to continue execution after attach.
        resumeIfNoFrame(attachedSession)

        val pausedSession = waitForPausedSession(
            project = project,
            configurationName = settings.getName(),
            config = config,
            triggerBreakpoint = fixture::tapBreakpointTrigger,
        )
        activeSession = pausedSession

        val hitLocation = assertBreakpointLocation(pausedSession, config)
        val debugTexts = captureDebugPanelTexts()

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
                appendLine("breakpointTriggerResourceId=${config.breakpointTriggerResourceId}")
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
                appendLine("breakpointTriggerResourceId=${config.breakpointTriggerResourceId}")
                appendLine("error=${error::class.qualifiedName}: ${error.message}")
                appendLine("debugSessions:")
                debugSessionSnapshot(project).forEach { appendLine(it) }
                appendLine("breakpoints:")
                breakpointSnapshot(project).forEach { appendLine(it) }
            },
        )
        throw error
    } finally {
        activeSession?.stop()
        settingsToCleanup?.let {
            utility(RunManagerRef::class).getInstance(project).removeConfiguration(it)
        }
        if (breakpointSet) {
            removeLineBreakpoints(
                project = project,
                config = config,
                line = breakpointLineZeroBased,
            )
        }
    }
}

private fun Driver.waitForSingleProject(config: IntegrationTestConfig): Project {
    return waitNotNull(
        message = "Wait for project to open",
        timeout = config.attachTimeoutSeconds.seconds,
    ) {
        singleProjectOrNull()
    }
}

private fun Driver.singleProjectOrNull(): Project? {
    return try {
        singleProject()
    } catch (error: IllegalStateException) {
        if (error.message == "No projects are opened") {
            null
        } else {
            throw error
        }
    }
}

private fun Driver.setLineBreakpointWithReadAction(project: Project, config: IntegrationTestConfig, line: Int) {
    removeLineBreakpoints(project, config, line)

    withContext(
        dispatcher = OnDispatcher.EDT,
        semantics = LockSemantics.READ_ACTION,
    ) {
        val file = findFile(relativePath = config.breakpointFileRelativePath, project = project)
            ?: error("Failed to find file for breakpoint: ${config.breakpointFileRelativePath}")
        val debuggerUtil = service<XDebuggerUtilRef>()
        check(debuggerUtil.canPutBreakpointAt(project, file, line)) {
            "Cannot put breakpoint at ${config.breakpointFileRelativePath}:${config.breakpointLine}"
        }
        debuggerUtil.toggleLineBreakpoint(project, file, line, temporary = false)
    }

    waitNotNull(
        message = "Wait for breakpoint to be registered",
        timeout = config.attachTimeoutSeconds.seconds,
    ) {
        findLineBreakpoint(project, config, line)?.takeIf { it.isEnabled() }
    }
}

private fun Driver.removeLineBreakpoints(project: Project, config: IntegrationTestConfig, line: Int) {
    val breakpointManager = utility(XDebuggerManagerRef::class).getInstance(project).getBreakpointManager()
    breakpointManager.getAllBreakpoints()
        .filter { it.matchesLine(config.breakpointFileRelativePath, line) }
        .forEach { breakpointManager.removeBreakpoint(it) }
}

private fun Driver.findLineBreakpoint(
    project: Project,
    config: IntegrationTestConfig,
    line: Int,
): XBreakpointRef? {
    val breakpointManager = utility(XDebuggerManagerRef::class).getInstance(project).getBreakpointManager()
    return breakpointManager.getAllBreakpoints().firstOrNull { it.matchesLine(config.breakpointFileRelativePath, line) }
}

private fun XBreakpointRef.matchesLine(relativePath: String, line: Int): Boolean {
    val expectedFileSuffix = relativePath.replace('\\', '/')
    return runCatching {
        val position = getSourcePosition() ?: return false
        val actualFile = position.getFile().getPath().replace('\\', '/')
        actualFile.endsWith(expectedFileSuffix) && position.getLine() == line
    }.getOrDefault(false)
}

private fun Driver.breakpointSnapshot(project: Project): List<String> {
    return runCatching {
        val breakpointManager = utility(XDebuggerManagerRef::class).getInstance(project).getBreakpointManager()
        breakpointManager.getAllBreakpoints().map { breakpoint ->
            val type = runCatching { "${breakpoint.getType().getId()} (${breakpoint.getType().getTitle()})" }
                .getOrDefault("<unknown type>")
            val enabled = runCatching { breakpoint.isEnabled() }.getOrDefault(false)
            val suspendPolicy = runCatching { breakpoint.getSuspendPolicy().name() }.getOrDefault("<unknown>")
            val position = runCatching { breakpoint.getSourcePosition() }.getOrNull()
            val file = runCatching { position?.getFile()?.getPath() }.getOrNull()
            val line = runCatching { position?.getLine()?.plus(1) }.getOrNull()
            "type=$type enabled=$enabled suspendPolicy=$suspendPolicy file=$file line=$line"
        }
    }.getOrElse { error ->
        listOf("<failed to collect breakpoint snapshot: ${error::class.qualifiedName}: ${error.message}>")
    }
}

private fun Driver.createAndStartRemoteDebugConfiguration(
    project: Project,
    config: IntegrationTestConfig,
): RunnerAndConfigurationSettingsRef {
    return withContext(dispatcher = OnDispatcher.EDT) {
        val runManager = utility(RunManagerRef::class).getInstance(project)
        val configurationTypeUtil = utility(ConfigurationTypeUtilRef::class)

        val remoteType = configurationTypeUtil.findConfigurationType("Remote")
            ?: configurationTypeUtil.findConfigurationType("javaRemote")
            ?: error("Unable to find \"javaRemote\" or \"Remote\" configuration")

        val factory = remoteType.getConfigurationFactories().firstOrNull()
            ?: error("RemoteConfigurationType does not expose configuration factories")

        val uniqueName = "$REMOTE_DEBUG_CONFIGURATION_NAME ${System.currentTimeMillis()}"
        val settings = runManager.createConfiguration(uniqueName, factory)
        settings.setTemporary(true)

        val remoteConfiguration = settings.getConfiguration()
        setPublicField(remoteConfiguration, "HOST", "127.0.0.1")
        setPublicField(remoteConfiguration, "PORT", config.jdwpLocalPort.toString())
        setPublicField(remoteConfiguration, "USE_SOCKET_TRANSPORT", true)
        setPublicField(remoteConfiguration, "SERVER_MODE", false)
        setPublicField(remoteConfiguration, "AUTO_RESTART", false)

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
    val perAttemptTimeoutSeconds = (config.attachTimeoutSeconds / MAX_ATTACH_ATTEMPTS).coerceAtLeast(10L)
    var lastError: Throwable? = null
    val settings = createAndStartRemoteDebugConfiguration(project, config)
    repeat(MAX_ATTACH_ATTEMPTS) {
        try {
            val attachedSession: XDebugSessionRef = waitNotNull(
                message = "Wait for attached debug session",
                timeout = perAttemptTimeoutSeconds.seconds,
            ) {
                findMatchingSession(project, settings.getName())
            }
            return settings to attachedSession
        } catch (error: Throwable) {
            lastError = error
            utility(RunManagerRef::class).getInstance(project).removeConfiguration(settings)
            Thread.sleep(1000)
        }
    }

    throw (lastError ?: error("Unable to attach debugger session after $MAX_ATTACH_ATTEMPTS attempts"))
}

private fun Driver.resumeIfNoFrame(attachedSession: XDebugSessionRef) {
    runCatching {
        if (attachedSession.getCurrentStackFrame() != null) {
            return
        }
        withContext(dispatcher = OnDispatcher.EDT) {
            attachedSession.resume()
        }
    }
}

private fun Driver.waitForPausedSession(
    project: Project,
    configurationName: String,
    config: IntegrationTestConfig,
    triggerBreakpoint: () -> Boolean,
): XDebugSessionRef {
    val triggerState = BreakpointTriggerState()

    try {
        return waitNotNull(
            message = "Wait for debug session suspended on breakpoint",
            timeout = config.hitTimeoutSeconds.seconds,
        ) {
            findPausedSession(project, configurationName) ?: run {
                val now = System.currentTimeMillis()
                if (now - triggerState.lastAttemptAt >= BREAKPOINT_TRIGGER_INTERVAL_MS) {
                    triggerState.lastAttemptAt = now
                    runCatching { triggerBreakpoint() }
                        .onSuccess { tapped ->
                            triggerState.result = if (tapped) {
                                "tapped ${config.breakpointTriggerResourceId}"
                            } else {
                                "view not found: ${config.breakpointTriggerResourceId}"
                            }
                        }
                        .onFailure { error ->
                            triggerState.failure = error
                            triggerState.result = "${error::class.qualifiedName}: ${error.message}"
                        }
                }
                null
            }
        }
    } catch (error: Throwable) {
        triggerState.failure?.let(error::addSuppressed)
        throw IllegalStateException(
            "Timed out waiting for breakpoint after UI trigger. Last trigger result: ${triggerState.result}",
            error,
        )
    }
}

private class BreakpointTriggerState {
    var lastAttemptAt: Long = 0
    var result: String = "not attempted"
    var failure: Throwable? = null
}

private fun Driver.findPausedSession(project: Project, configurationName: String): XDebugSessionRef? {
    val session = findMatchingSession(project, configurationName) ?: return null
    if (!session.isSuspended()) return null
    if (session.getCurrentStackFrame() == null) return null
    return session
}

private fun Driver.findMatchingSession(project: Project, configurationName: String): XDebugSessionRef? {
    val manager = utility(XDebuggerManagerRef::class).getInstance(project)
    return manager.getDebugSessions().firstOrNull { session ->
        val sessionName = session.getSessionName()
        sessionName == configurationName || sessionName.contains(configurationName)
    }
}

private fun Driver.debugSessionSnapshot(project: Project): List<String> {
    return runCatching {
        val manager = utility(XDebuggerManagerRef::class).getInstance(project)
        manager.getDebugSessions().map { session ->
            val name = runCatching { session.getSessionName() }.getOrDefault("<unknown>")
            val suspended = runCatching { session.isSuspended() }.getOrNull()
            val frame = runCatching { session.getCurrentStackFrame() != null }.getOrNull()
            val position = runCatching { session.getCurrentPosition() }.getOrNull()
            val file = runCatching { position?.getFile()?.getPath() }.getOrNull()
            val line = runCatching { position?.getLine()?.plus(1) }.getOrNull()
            "name=$name suspended=$suspended hasFrame=$frame file=$file line=$line"
        }
    }.getOrElse { error ->
        listOf("<failed to collect debug sessions: ${error::class.qualifiedName}: ${error.message}>")
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
