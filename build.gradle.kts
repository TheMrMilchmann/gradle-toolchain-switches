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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.*

@Suppress("DSL_SCOPE_VIOLATION") // See https://github.com/gradle/gradle/issues/22797
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.samwithreceiver)
    alias(libs.plugins.gradle.plugin.functional.test)
    alias(libs.plugins.gradle.toolchain.switches)
    alias(libs.plugins.plugin.publish)
    id("io.github.themrmilchmann.maven-publish-conventions")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }

    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()

    target {
        compilations.all {
            compilerOptions.configure {
                apiVersion.set(KotlinVersion.KOTLIN_1_8)
                languageVersion.set(KotlinVersion.KOTLIN_1_8)
            }
        }

        compilations.named("main").configure {
            compilerOptions.configure {
                apiVersion.set(KotlinVersion.KOTLIN_1_4)
            }
        }
    }
}

gradlePlugin {
    compatibility {
        minimumGradleVersion.set("7.4")
    }

    website.set("https://github.com/TheMrMilchmann/gradle-toolchain-switches")
    vcsUrl.set("https://github.com/TheMrMilchmann/gradle-toolchain-switches.git")

    plugins {
        create("toolchainswitches") {
            id = "io.github.themrmilchmann.toolchain-switches"
            displayName = "Gradle Toolchain Switches Plugin"
            description = "A Gradle plugin that enables dynamic configuration of toolchains for specific tasks via Gradle properties."
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
        options.release.set(8)
    }

    withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()

        /*
         * This does not work because there is no way for the plugin to respect
         * this convention while keeping the configuration lazy. In practice,
         * this means that the task defaults to the project-wide toolchain while
         * it should default to a toolchain for Java 8.
         *
         * We work around this by exposing a few experimental functions that can
         * be used to configure tools (taking into account properties) starting
         * in 0.3.0.
         *
         * TODO Adapt this task to use 0.3.0's toolchain configuration
         *      functions.
         *
         * See https://github.com/gradle/gradle/issues/14768
         */
//        javaLauncher.convention(project.javaToolchains.launcherFor {
//            languageVersion.set(JavaLanguageVersion.of(8))
//        })

        systemProperty("junit.jupiter.execution.parallel.enabled", true)
        systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")

        environment("junitVersion", libs.versions.junit.get())
    }
}

val emptyJar = tasks.register<Jar>("emptyJar") {
    destinationDirectory.set(layout.buildDirectory.dir("emptyJar"))
    archiveBaseName.set("io.github.themrmilchmann.toolchainswitches.gradle.plugin")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name == "toolchainswitchesPluginMarkerMaven") {
            artifact(emptyJar)
            artifact(emptyJar) { classifier = "javadoc" }
            artifact(emptyJar) { classifier = "sources" }
        }

        pom {
            name.set("Gradle Toolchain Switches Plugin")
            description.set("A Gradle plugin that adds command line parameters that may be used to dynamically switch between toolchains for specific tasks.")
        }
    }
}

dependencies {
    functionalTestImplementation(platform(libs.junit.bom))
    functionalTestImplementation(libs.junit.jupiter.api)
    functionalTestImplementation(libs.junit.jupiter.params)
    functionalTestRuntimeOnly(libs.junit.jupiter.engine)
}