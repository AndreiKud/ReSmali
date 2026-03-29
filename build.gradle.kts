@file:Suppress("UnstableApiUsage")

import org.gradle.kotlin.dsl.testImplementation
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.TestIdeUiTask
import java.util.Properties

plugins {
    id("java")
    id("antlr")
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("org.jetbrains.intellij.platform") version "2.12.0"
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val integrationTestSourceSet = sourceSets.create("integrationTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}
val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

kotlin {
    jvmToolchain(21)
}
tasks {
    withType<JavaCompile>().configureEach {
        options.release.set(17)
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjdk-release=17")
        }
    }
}

intellijPlatform {
    buildSearchableOptions = false
    pluginVerification {
        ides {
            create {
                type = IntelliJPlatformType.IntellijIdea
                version = "261.22158.46" // 2026.1 eap, March 5, 2026
            }
            create {
                type = IntelliJPlatformType.IntellijIdeaCommunity
                version = "2025.1" // April 14, 2025
            }
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.2.5")
        // androidStudio("2025.2.3.9")
        bundledPlugin("com.intellij.java")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
        testFramework(TestFrameworkType.Starter, configurationName = "integrationTestImplementation")
    }

    implementation("com.android.tools.smali:smali:3.0.9")
    implementation("com.android.tools.smali:smali-util:3.0.9")

    implementation("org.antlr:antlr-runtime:3.5.2")
    implementation("com.google.code.gson:gson:2.8.9")
    antlr("org.antlr:antlr:3.5.3")

    testImplementation("junit:junit:4.13.2")
    integrationTestImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    integrationTestImplementation("org.assertj:assertj-core:3.27.3")
    integrationTestImplementation("org.kodein.di:kodein-di-jvm:7.20.2")
    integrationTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.1")
    integrationTestRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // TODO: move from javax.annotations to org.jetbrains.annotations
    implementation("com.github.spotbugs:spotbugs-annotations:4.9.7")
}

fun JavaExec.setupIdeExec() {
    // For testing purposes
    val smaliProjectDirPath: String? = localProperties.getProperty("smali.project.dir")
    if (!smaliProjectDirPath.isNullOrBlank()) {
        argumentProviders += CommandLineArgumentProvider { listOf(
            smaliProjectDirPath,
        ) }
    }
}

val currentVersion = "0.09"
version = currentVersion

tasks {
    patchPluginXml {
        pluginId = "dev.resmali"
        pluginName = "ReSmali"
        pluginVersion = currentVersion
        pluginDescription = """
            Adds support for Smali language:
            <br>
            <ul>
                <li>Highlighting</li>
                <li>Debugging (registers listing, examining objects state, conditional breakpoints, setting values at runtime, etc.)</li>
                <li>Basic refactoring</li>
                <li>Navigation, search, structure view</li>
            </ul>
            <br>
            ReSmali is a fork of the original <a href="https://github.com/JesusFreke/smalidea">smalidea</a> plugin by Ben Gruver (JesusFreke).
        """.trimIndent()

        changeNotes = null

        vendorName = "Andrei Kudryavtsev"
        vendorEmail = "andreikudrya1995@gmail.com"
        vendorUrl = "https://github.com/AndreiKud"

        sinceBuild = "251.23774.435"
    }

    runIde {
        setupIdeExec()
    }

    val integrationTest by intellijPlatformTesting.testIdeUi.registering {
        task {
            description = "Runs integration tests with Starter/Driver."
            group = "verification"

            testClassesDirs = integrationTestSourceSet.output.classesDirs
            classpath = integrationTestSourceSet.runtimeClasspath
            useJUnitPlatform()

            dependsOn(buildPlugin)
        }
    }
    val cleanAllureResults by registering(Delete::class) {
        delete("allure-results")
    }
    withType<TestIdeUiTask>().configureEach {
        dependsOn(cleanAllureResults)
    }

    // https://youtrack.jetbrains.com/articles/IDEA-A-21/IDEA-Latest-Builds-And-Release-Notes
    intellijPlatformTesting.runIde.register("runIntellijIdea") {
        task {
            setupIdeExec()
            jvmArgumentProviders += CommandLineArgumentProvider { listOf(
                "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8706",
            ) }
        }
        // type = IntelliJPlatformType.IntellijIdea
        // version = "261.22158.46" // 2026.1 eap, March 5, 2026
        type = IntelliJPlatformType.IntellijIdeaCommunity
        version = "2025.1" // 15 Apr 2025
    }

    // https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html
    intellijPlatformTesting.runIde.register("runAndroidStudio") {
        task {
            setupIdeExec()
            jvmArgumentProviders += CommandLineArgumentProvider { listOf(
                "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8706",
            ) }
        }
        type = IntelliJPlatformType.AndroidStudio
        // version = "2025.3.3.2" // Panda 3 Canary 2, Feb 26, 2026
        version = "2025.1.2.11" // Narwhal Feature Drop, Jul 31, 2025
    }

    val extractTokensTask = register<Copy>("extractTokens") {
        val smaliZip = providers.provider {
            val artifacts = configurations.runtimeClasspath.flatMap {
                it.incoming.artifacts.resolvedArtifacts
            }.get()
            artifacts.find { it.id.displayName.contains(":smali") }?.file
                ?: throw GradleException("Could not find 'smali' artifact in 'default' configuration")
        }

        from(smaliZip.map { zipTree(it) }) {
            include("**/*.tokens")
        }
        into(layout.buildDirectory.dir("tokens"))
    }

    generateGrammarSource.configure {
        val tokensDir = layout.buildDirectory.dir("tokens/com/android/tools/smali/smali/")
        arguments = arguments + listOf("-lib", tokensDir.get().asFile.path)
        outputDirectory = layout.buildDirectory.dir("generated-src/antlr/main/dev/resmali/").get().asFile
        dependsOn(extractTokensTask)
    }
}
