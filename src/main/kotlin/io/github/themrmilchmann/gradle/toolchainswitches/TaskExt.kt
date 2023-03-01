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
package io.github.themrmilchmann.gradle.toolchainswitches

import io.github.themrmilchmann.gradle.toolchainswitches.plugins.ToolchainSwitchesPlugin
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.*

internal fun <T> Task.inferredToolImpl(
    javaToolchains: JavaToolchainService = project.extensions.getByType(JavaToolchainService::class.java),
    factory: JavaToolchainService.(Action<JavaToolchainSpec>) -> Provider<T>,
    default: Provider<T>?
): Provider<T> {
    val versionProvider = project.providers.gradleProperty("${ToolchainSwitchesPlugin.PROPERTY_PREFIX}.$name.${ToolchainSwitchesPlugin.PROPERTY_VERSION_SUFFIX}")

    @Suppress("ObjectLiteralToLambda")
    return versionProvider.flatMap { version ->
        javaToolchains.factory(object : Action<JavaToolchainSpec> {
            override fun execute(spec: JavaToolchainSpec) {
                if (version != ToolchainSwitchesPlugin.ENVIRONMENT_TOOLCHAIN_SELECTOR) {
                    spec.languageVersion.set(JavaLanguageVersion.of(version))
                }
            }
        })
    }
        .orElse(default ?: javaToolchains.factory(object : Action<JavaToolchainSpec> {
            override fun execute(spec: JavaToolchainSpec) {}
        }))
}

/**
 * Infers a toolchain tool of type [T] for the receiver task.
 *
 * Gradle properties can be used to configure the selected tool. The algorithm
 * currently considers the following properties:
 *
 * - `toolchain.${taskName}.version` – the language version for the toolchain
 *
 * The values of these properties are then used to select a toolchain. The
 * algorithm selects the first available tool from the following candidates:
 *
 * 1. If any property is configured, a tool is selected based on the configured
 *    values
 * 2. The [default] provider is considered:
 *     - If, it is not `null` and has a value, its value is used
 *     - If it is `null`, the project toolchain is used (if available)
 * 3. Otherwise, the tool from the JVM running the build is used
 *
 * @param T         the type of the tool
 * @param factory   the function to select the tool
 * @param default   a provider that provides the tool to default to, or `null`
 *                  to default to the project toolchain. The provider may have
 *                  no value.
 *
 * @since   0.3.0
 */
@ExperimentalToolchainSwitchesApi
public fun <T> Task.inferredTool(
    factory: JavaToolchainService.(Action<JavaToolchainSpec>) -> Provider<T>,
    default: Provider<T>? = null
): Provider<T> = inferredToolImpl(
    factory = factory,
    default = default
)

/**
 * Infers the [JavaCompiler] for the receiver task.
 *
 * See [inferredTool] for the strategy used to select the tool.
 *
 * @param default   a provider that provides the tool to default to, or `null`
 *                  to default to the project toolchain
 *
 * @return  a provider providing the inferred tool
 *
 * @since   0.3.0
 */
@ExperimentalToolchainSwitchesApi
public fun Task.inferredCompiler(default: Provider<JavaCompiler>? = null): Provider<JavaCompiler> =
    inferredToolImpl(
        factory = JavaToolchainService::compilerFor,
        default = default
    )

/**
 * Infers the [JavadocTool] for the receiver task.
 *
 * See [inferredTool] for the strategy used to select the tool.
 *
 * @param default   a provider that provides the tool to default to, or `null`
 *                  to default to the project toolchain
 *
 * @return  a provider providing the inferred tool
 *
 * @since   0.3.0
 */
@ExperimentalToolchainSwitchesApi
public fun Task.inferredJavadocTool(default: Provider<JavadocTool>? = null): Provider<JavadocTool> =
    inferredToolImpl(
        factory = JavaToolchainService::javadocToolFor,
        default = default
    )

/**
 * Infers the [JavaLauncher] for the receiver task.
 *
 * See [inferredTool] for the strategy used to select the tool.
 *
 * @param default   a provider that provides the tool to default to, or `null`
 *                  to default to the project toolchain
 *
 * @return  a provider providing the inferred tool
 *
 * @since   0.3.0
 */
@ExperimentalToolchainSwitchesApi
public fun Task.inferLauncher(default: Provider<JavaLauncher>? = null): Provider<JavaLauncher> =
    inferredToolImpl(
        factory = JavaToolchainService::launcherFor,
        default = default
    )