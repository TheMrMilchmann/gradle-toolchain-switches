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

import io.github.themrmilchmann.gradle.toolchainswitches.inferredToolImpl
import io.github.themrmilchmann.gradle.toolchainswitches.internal.utils.*
import org.gradle.api.*
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.*
import org.gradle.jvm.toolchain.JavaToolchainService

public class ToolchainSwitchesPlugin : Plugin<Project> {

    public companion object {

        public const val PROPERTY_PREFIX: String = "toolchain"
        public const val PROPERTY_VERSION_SUFFIX: String = "version"

        public const val ENVIRONMENT_TOOLCHAIN_SELECTOR: String = "env"

    }

    override fun apply(target: Project): Unit = applyTo(target) {
        pluginManager.withPlugin("org.gradle.java") {
            val java = extensions.getByType(JavaPluginExtension::class.java)
            val javaToolchains = extensions.getByType(JavaToolchainService::class.java)

            applyTo(tasks) {
                withType(JavaCompile::class.java).configureEach {
                    javaCompiler.set(inferredToolImpl(
                        javaToolchains,
                        factory = JavaToolchainService::compilerFor,
                        default = project.provider { java.toolchain }.flatMap { javaToolchains.compilerFor(it) }
                    ))
                }

                withType(JavaExec::class.java).configureEach {
                    javaLauncher.set(inferredToolImpl(
                        javaToolchains,
                        factory = JavaToolchainService::launcherFor,
                        default = project.provider { java.toolchain }.flatMap { javaToolchains.launcherFor(it) }
                    ))
                }

                withType(Javadoc::class.java).configureEach {
                    javadocTool.set(inferredToolImpl(
                        javaToolchains,
                        factory = JavaToolchainService::javadocToolFor,
                        default = project.provider { java.toolchain }.flatMap { javaToolchains.javadocToolFor(it) }
                    ))
                }

                withType(Test::class.java).configureEach {
                    javaLauncher.set(inferredToolImpl(
                        javaToolchains,
                        factory = JavaToolchainService::launcherFor,
                        default = project.provider { java.toolchain }.flatMap { javaToolchains.launcherFor(it) }
                    ))
                }
            }
        }
    }

}