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
package io.github.themrmilchmann.gradle.toolchainswitches.plugins

import org.gradle.api.JavaVersion
import org.gradle.internal.jvm.Jvm
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Suppress("FunctionName")
class ToolchainSwitchesPluginTest {

    private companion object {

        lateinit var junitVersion: String

        @BeforeAll
        @JvmStatic
        fun setup() {
            junitVersion = System.getenv("junitVersion")
        }

        @JvmStatic
        private fun provideGradleVersions(): List<String> = buildList {
            // See https://docs.gradle.org/current/userguide/compatibility.html
            val javaVersion = JavaVersion.current()

            add("8.0.2")
            add("8.0.1")
            add("8.0")
            add("7.6.1")
            add("7.6")

            @Suppress("UnstableApiUsage")
            if (javaVersion >= JavaVersion.VERSION_19) return@buildList

            add("7.5.1")
            add("7.5")

            @Suppress("UnstableApiUsage")
            if (javaVersion >= JavaVersion.VERSION_18) return@buildList

            add("7.4.2")
            add("7.4.1")
            add("7.4")
        }

    }

    @field:TempDir
    lateinit var projectDir: Path

    private val buildFile: Path get() = projectDir.resolve("build.gradle")
    private val settingsFile: Path get() = projectDir.resolve("settings.gradle")

    @ParameterizedTest
    @MethodSource("provideGradleVersions")
    fun `Run tests with project toolchain`(gradleVersion: String) {
        writeSettingsFile(gradleVersion)
        writeTest(expectedVersion = "8")

        buildFile.writeText(
            """
            plugins {
                id 'io.github.themrmilchmann.toolchain-switches'
                id 'java-library'                
            }
            
            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(8)
                }
            }

            test {
                useJUnitPlatform()
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                testImplementation 'org.junit.jupiter:junit-jupiter-api:$junitVersion'
                testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:$junitVersion'
            }
            """.trimIndent()
        )

        GradleRunner.create()
            .withArguments("test", "--info")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .withProjectDir(projectDir.toFile())
            .build()
    }

    @ParameterizedTest
    @MethodSource("provideGradleVersions")
    fun `Run tests with toolchain override`(gradleVersion: String) {
        writeSettingsFile(gradleVersion)
        writeTest(expectedVersion = "17")

        buildFile.writeText(
            """
            plugins {
                id 'io.github.themrmilchmann.toolchain-switches'
                id 'java-library'                
            }
            
            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(8)
                }
            }

            test {
                useJUnitPlatform()
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                testImplementation 'org.junit.jupiter:junit-jupiter-api:$junitVersion'
                testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:$junitVersion'
            }
            """.trimIndent()
        )

        GradleRunner.create()
            .withArguments("test", "--info", "-Ptoolchain.test.version=17")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .withProjectDir(projectDir.toFile())
            .build()
    }
    @ParameterizedTest
    @MethodSource("provideGradleVersions")
    fun `Run tests with build toolchain`(gradleVersion: String) {
        writeSettingsFile(gradleVersion)
        writeTest(expectedVersion = Jvm.current().javaVersion?.majorVersion!!)

        buildFile.writeText(
            """
            plugins {
                id 'io.github.themrmilchmann.toolchain-switches'
                id 'java-library'                
            }

            test {
                useJUnitPlatform()
            }
            
            repositories {
                mavenCentral()
            }

            dependencies {
                testImplementation 'org.junit.jupiter:junit-jupiter-api:$junitVersion'
                testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:$junitVersion'
            }
            """.trimIndent()
        )

        GradleRunner.create()
            .withArguments("test", "--info")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .withProjectDir(projectDir.toFile())
            .build()
    }

    private fun writeSettingsFile(gradleVersion: String) {
        if (gradleVersion < "8.0") return

        settingsFile.writeText(
            """
            pluginManagement {
                plugins {
                    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.4.0'
                }
            }
            
            plugins {
                id 'org.gradle.toolchains.foojay-resolver-convention'
            }
            """.trimIndent()
        )
    }

    private fun writeTest(expectedVersion: String) {
        projectDir.resolve("src/test/java/com/example")
            .createDirectories()
            .resolve("VersionTest.java")
            .writeText(
                """
                package com.example;
                
                import org.junit.jupiter.api.Test;
                
                import static org.junit.jupiter.api.Assertions.*;
                
                public class VersionTest {
                
                    @Test
                    public void test() {
                        String javaVersion = System.getProperty("java.version");
                        if (javaVersion.startsWith("1.")) javaVersion = javaVersion.substring(2);
    
                        javaVersion = javaVersion.substring(0, javaVersion.indexOf("."));
                        assertEquals("$expectedVersion", javaVersion);
                    }
                
                }
                """.trimIndent()
            )
    }

}