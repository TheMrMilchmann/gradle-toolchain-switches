/*
 * Copyright 2018-2022 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.themrmilchmann.gradle.toolchainswitches.plugins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

class ToolchainSwitchesPluginTest extends Specification {

    private static def GRADLE_VERSIONS = [
        "7.4",
        "7.4.1",
        "7.4.2",
        "7.5",
        "7.5.1"
    ]

    private static def TOOLCHAIN_VERSIONS = [
        "11",
        "17",
        ToolchainSwitchesPlugin.ENVIRONMENT_TOOLCHAIN_SELECTOR
    ]

    @TempDir
    File projectDir
    File buildFile
    File settingsFile

    String junitVersion

    def setup() {
        buildFile = new File(projectDir, "build.gradle")
        settingsFile = new File(projectDir, "settings.gradle")

        junitVersion = System.getenv("junitVersion")
    }

    @Unroll
    def "run test with override (Gradle #gradleVersion; Toolchain override '#toolchainVersion')"() {
        given:
        writeTest(toolchainVersion)
        buildFile << """\
            plugins {
                id 'java-library'
                id 'io.github.themrmilchmann.toolchain-switches'
            }
            
            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(8)
                }
            }
            
            tasks {
                test {
                    useJUnitPlatform()
                    
                    testLogging.showStandardStreams = true
                }
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testImplementation 'org.junit.jupiter:junit-jupiter-api:$junitVersion' 
                testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:$junitVersion'
            }
        """.stripIndent()

        when:
        def result = runGradle(gradleVersion, "test", "--info", "-Ptoolchain.test.version=$toolchainVersion")

        then:
        result.task(":test").outcome == TaskOutcome.SUCCESS

        where:
        [gradleVersion, toolchainVersion] << [GRADLE_VERSIONS, TOOLCHAIN_VERSIONS].combinations()
    }

    @Unroll
    def "run test with no override (Gradle #gradleVersion)"() {
        given:
        writeTest("8")
        buildFile << """\
            plugins {
                id 'java-library'
                id 'io.github.themrmilchmann.toolchain-switches'
            }
            
            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(8)
                }
            }
            
            tasks {
                test {
                    useJUnitPlatform()
                    
                    testLogging.showStandardStreams = true
                }
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testImplementation 'org.junit.jupiter:junit-jupiter-api:$junitVersion' 
                testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:$junitVersion'
            }
        """.stripIndent()

        when:
        def result = runGradle(gradleVersion, "test", "--info")

        then:
        result.task(":test").outcome == TaskOutcome.SUCCESS

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    @Unroll
    def "run test with unspecified toolchain (Gradle #gradleVersion)"() {
        given:
        writeTest(getLanguageVersion())
        buildFile << """\
            plugins {
                id 'java-library'
                id 'io.github.themrmilchmann.toolchain-switches'
            }
            
            tasks {
                test {
                    useJUnitPlatform()
                    
                    testLogging.showStandardStreams = true
                }
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testImplementation 'org.junit.jupiter:junit-jupiter-api:$junitVersion' 
                testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:$junitVersion'
            }
        """.stripIndent()

        when:
        def result = runGradle(gradleVersion, "test", "--info")

        then:
        result.task(":test").outcome == TaskOutcome.SUCCESS

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    private runGradle(String version, String... args) {
        def arguments = []
        arguments.addAll(args)
        arguments.add("-s")

        GradleRunner.create()
        .withGradleVersion(version)
        .withProjectDir(projectDir)
        .withArguments(args)
        .withPluginClasspath()
        .build()
    }

    private String getLanguageVersion() {
        String javaVersion = System.getProperty("java.version")
        if (javaVersion.startsWith("1.")) javaVersion = javaVersion.substring(2)

        return javaVersion.substring(0, javaVersion.indexOf("."))
    }

    private void writeTest(String version, File baseDir = projectDir) {
        File outputFile = new File(baseDir, "src/test/java/com/example/Main.java")
        outputFile.parentFile.mkdirs()
        outputFile.createNewFile()

        def expectedVersion

        if (version == "env") {
            expectedVersion = getLanguageVersion()
        } else {
            expectedVersion = version
        }

        outputFile << """\
            package com.example;
            
            import org.junit.jupiter.api.Test;
            
            import static org.junit.jupiter.api.Assertions.*;
            
            public class Main {
            
                @Test
                public void test() {
                    String javaVersion = System.getProperty("java.version");
                    if (javaVersion.startsWith("1.")) javaVersion = javaVersion.substring(2);

                    javaVersion = javaVersion.substring(0, javaVersion.indexOf("."));
                    assertEquals("$expectedVersion", javaVersion);
                }
            
            }
            """.stripIndent()
    }

}