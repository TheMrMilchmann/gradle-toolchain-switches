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
import io.github.themrmilchmann.build.*
import io.github.themrmilchmann.build.BuildType
import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.samwithreceiver)
    alias(libs.plugins.gradle.plugin.functional.test)
    alias(libs.plugins.plugin.publish)
    id("io.github.themrmilchmann.maven-publish-conventions")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()
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
            description = "A Gradle plugin that adds command line parameters that may be used to dynamically switch between toolchains for specific tasks."
            tags.addAll("cli", "configuration", "java", "toolchains")

            implementationClass = "io.github.themrmilchmann.gradle.toolchainswitches.plugins.ToolchainSwitchesPlugin"
        }
    }
}

samWithReceiver {
    annotation("org.gradle.api.HasImplicitReceiver")
}

tasks {
    named<Test>("test").configure {
        useJUnitPlatform()

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
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
}