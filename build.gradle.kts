/*
 * Copyright (c) 2022 Leon Linhart
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
import com.github.themrmilchmann.build.*
import com.github.themrmilchmann.build.BuildType
import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    groovy
    `java-test-fixtures`
    `kotlin-dsl`
    `maven-publish`
    signing
    alias(libs.plugins.plugin.publish)
}

group = "io.github.themrmilchmann.gradle.toolchainswitches"
val nextVersion = "0.1.0"
version = when (deployment.type) {
    BuildType.SNAPSHOT -> "$nextVersion-SNAPSHOT"
    else -> nextVersion
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
    plugins {
        create("toolchainswitches") {
            id = "io.github.themrmilchmann.toolchain-switches"
            displayName = "Gradle Toolchain Switches Plugin"
            description = "A Gradle plugin that adds command line parameters that may be used to dynamically switch between toolchains for specific tasks."

            implementationClass = "io.github.themrmilchmann.gradle.toolchainswitches.plugins.ToolchainSwitchesPlugin"
        }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    withType<Test> {
        useJUnitPlatform()

        doFirst {
            environment("junitVersion", libs.versions.junit.get())
        }
    }
}

val emptyJar = tasks.create<Jar>("emptyJar") {
    destinationDirectory.set(buildDir.resolve("emptyJar"))
    archiveBaseName.set("io.github.themrmilchmann.toolchainswitches.gradle.plugin")
}

publishing {
    repositories {
        maven {
            url = uri(deployment.repo)

            credentials {
                username = deployment.user
                password = deployment.password
            }
        }
    }
    publications.withType<MavenPublication> {
        if (name == "toolchainswitchesPluginMarkerMaven") {
            artifact(emptyJar)
            artifact(emptyJar) { classifier = "javadoc" }
            artifact(emptyJar) { classifier = "sources" }
        }

        pom {
            name.set("Gradle Toolchain Switches Plugin")
            description.set("A Gradle plugin that adds command line parameters that may be used to dynamically switch between toolchains for specific tasks.")
            packaging = "jar"
            url.set("https://github.com/TheMrMilchmann/gradle-toolchain-switches")

            licenses {
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/TheMrMilchmann/gradle-toolchain-switches/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }
            }

            developers {
                developer {
                    id.set("TheMrMilchmann")
                    name.set("Leon Linhart")
                    email.set("themrmilchmann@gmail.com")
                    url.set("https://github.com/TheMrMilchmann")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/TheMrMilchmann/gradle-toolchain-switches.git")
                developerConnection.set("scm:git:git://github.com/TheMrMilchmann/gradle-toolchain-switches.git")
                url.set("https://github.com/TheMrMilchmann/gradle-toolchain-switches.git")
            }
        }
    }
}

pluginBundle {
    website = "https://github.com/TheMrMilchmann/gradle-toolchain-switches"
    vcsUrl = "https://github.com/TheMrMilchmann/gradle-toolchain-switches.git"

    tags = listOf("cli", "configuration", "java", "toolchains")
}

signing {
    isRequired = (deployment.type === BuildType.RELEASE)
    sign(publishing.publications)
}

repositories {
    mavenCentral()
}

dependencies {
    testFixturesApi(platform(libs.spock.bom))
    testFixturesApi(libs.spock.core)
}