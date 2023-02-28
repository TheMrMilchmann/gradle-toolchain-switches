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

import io.github.themrmilchmann.gradle.toolchainswitches.internal.utils.*
import org.gradle.api.*
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.*
import org.gradle.jvm.toolchain.JavaCompiler
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JavadocTool

public class ToolchainSwitchesPlugin : Plugin<Project> {

    public companion object {

        public const val PROPERTY_PREFIX: String = "toolchain"
        public const val PROPERTY_VERSION_SUFFIX: String = "version"

        public const val ENVIRONMENT_TOOLCHAIN_SELECTOR: String = "env"

    }

    override fun apply(target: Project): Unit = applyTo(target) {
        plugins.withType(JavaBasePlugin::class.java) {
            val java = extensions.getByType(JavaPluginExtension::class.java)
            val javaToolchains = extensions.getByType(JavaToolchainService::class.java)

            applyTo(tasks) {
                withType(JavaCompile::class.java).configureEach {
                    javaCompiler.set(inferCompiler(taskName = name, java, javaToolchains))
                }

                withType(JavaExec::class.java).configureEach {
                    javaLauncher.set(inferLauncher(name, java, javaToolchains))
                }

                withType(Javadoc::class.java).configureEach {
                    javadocTool.set(inferJavadocTool(name, java, javaToolchains))
                }

                withType(Test::class.java).configureEach {
                    javaLauncher.set(inferLauncher(name, java, javaToolchains))
                }
            }
        }
    }

    private fun <T> Project.infer(
        taskName: String,
        java: JavaPluginExtension,
        javaToolchains: JavaToolchainService,
        factory: JavaToolchainService.(Action<JavaToolchainSpec>) -> Provider<T>,
        getter: JavaToolchainService.(JavaToolchainSpec) -> Provider<T>
    ): Provider<T> {
        val versionProvider = providers.gradleProperty("$PROPERTY_PREFIX.$taskName.$PROPERTY_VERSION_SUFFIX")

        return versionProvider.flatMap { version ->
            when (version) {
                ENVIRONMENT_TOOLCHAIN_SELECTOR -> javaToolchains.factory {}
                else -> javaToolchains.factory {
                    languageVersion.set(JavaLanguageVersion.of(version))
                }
            }
        }.orElse(provider { java.toolchain }.flatMap { javaToolchains.getter(it) })
            .orElse(javaToolchains.factory {})
    }

    private fun Project.inferCompiler(
        taskName: String,
        java: JavaPluginExtension,
        javaToolchains: JavaToolchainService
    ): Provider<JavaCompiler> {
        return infer(
            taskName,
            java,
            javaToolchains,
            factory = JavaToolchainService::compilerFor,
            getter = JavaToolchainService::compilerFor
        )
    }

    private fun Project.inferJavadocTool(
        taskName: String,
        java: JavaPluginExtension,
        javaToolchains: JavaToolchainService
    ): Provider<JavadocTool> {
        return infer(
            taskName,
            java,
            javaToolchains,
            factory = JavaToolchainService::javadocToolFor,
            getter = JavaToolchainService::javadocToolFor
        )
    }

    private fun Project.inferLauncher(
        taskName: String,
        java: JavaPluginExtension,
        javaToolchains: JavaToolchainService
    ): Provider<JavaLauncher> {
        return infer(
            taskName,
            java,
            javaToolchains,
            factory = JavaToolchainService::launcherFor,
            getter = JavaToolchainService::launcherFor
        )
    }

}