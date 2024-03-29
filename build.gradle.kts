/*
 * Copyright (c) 2022-2023 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import io.github.themrmilchmann.gradle.toolchainswitches.ExperimentalToolchainSwitchesApi
import io.github.themrmilchmann.gradle.toolchainswitches.inferLauncher
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.gradle.plugin.functional.test)
    alias(libs.plugins.gradle.toolchain.switches)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.samwithreceiver)
    alias(libs.plugins.plugin.publish)
    id("io.github.themrmilchmann.maven-publish-conventions")
}

// Workaround for https://github.com/gradle/gradle/issues/10921
group = "io.github.themrmilchmann.gradle.toolchainswitches"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }

    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()

    target {
        compilations.configureEach {
            compilerOptions.configure {
                apiVersion = KotlinVersion.KOTLIN_1_8
                languageVersion = KotlinVersion.KOTLIN_1_8
            }
        }

        compilations.named("main").configure {
            compilerOptions.configure {
                @Suppress("DEPRECATION")
                apiVersion = KotlinVersion.KOTLIN_1_4
            }
        }
    }
}

gradlePlugin {
    compatibility {
        minimumGradleVersion = "7.4"
    }

    website = "https://github.com/TheMrMilchmann/gradle-toolchain-switches"
    vcsUrl = "https://github.com/TheMrMilchmann/gradle-toolchain-switches.git"

    plugins {
        register("toolchainswitches") {
            id = "io.github.themrmilchmann.toolchain-switches"
            displayName = "Gradle Toolchain Switches Plugin"
            description = "A Gradle plugin that enables dynamic configuration of toolchains for specific tasks via project properties."
            tags.addAll("cli", "configuration", "java", "toolchains")

            implementationClass = "io.github.themrmilchmann.gradle.toolchainswitches.plugins.ToolchainSwitchesPlugin"
        }
    }
}

samWithReceiver {
    annotation("org.gradle.api.HasImplicitReceiver")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 8
    }

    withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()

        @OptIn(ExperimentalToolchainSwitchesApi::class)
        javaLauncher.set(inferLauncher(default = project.javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(8))
        }))

        systemProperty("junit.jupiter.execution.parallel.enabled", true)
        systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")

        environment("junitVersion", libs.versions.junit.get())
    }

    withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true

        includeEmptyDirs = false
    }

    validatePlugins {
        enableStricterValidation = true
    }
}

val emptyJar = tasks.register<Jar>("emptyJar") {
    destinationDirectory = layout.buildDirectory.dir("emptyJar")
    archiveBaseName = "io.github.themrmilchmann.toolchainswitches.gradle.plugin"
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name == "toolchainswitchesPluginMarkerMaven") {
            artifact(emptyJar)
            artifact(emptyJar) { classifier = "javadoc" }
            artifact(emptyJar) { classifier = "sources" }
        }

        pom {
            name = "Gradle Toolchain Switches Plugin"
            description = "A Gradle plugin that enables dynamic configuration of toolchains for specific tasks via project properties."
        }
    }
}

dependencies {
    compileOnlyApi(kotlin("stdlib"))

    functionalTestImplementation(kotlin("stdlib"))
    functionalTestImplementation(platform(libs.junit.bom))
    functionalTestImplementation(libs.junit.jupiter.api)
    functionalTestImplementation(libs.junit.jupiter.params)
    functionalTestRuntimeOnly(libs.junit.jupiter.engine)
}