# Gradle Toolchain Switches Plugin

[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat-square&label=License)](https://github.com/TheMrMilchmann/gradle-toolchain-switches/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.themrmilchmann.gradle.toolchainswitches/gradle-toolchain-switches.svg?style=flat-square&label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/io.github.themrmilchmann.gradle.toolchainswitches/gradle-toolchain-switches)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v.svg?style=flat-square&&label=Gradle%20Plugin%20Portal&logo=Gradle&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fio%2Fgithub%2Fthemrmilchmann%2Ftoolchain-switches%2Fio.github.themrmilchmann.toolchain-switches.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/io.github.themrmilchmann.toolchain-switches)
![Gradle](https://img.shields.io/badge/Gradle-7.4-green.svg?style=flat-square&color=1ba8cb&logo=Gradle)
![Java](https://img.shields.io/badge/Java-8-green.svg?style=flat-square&color=b07219&logo=Java)

A Gradle plugin that enables dynamic configuration of toolchains for specific
tasks via project properties.


# Usage

The plugin provides the `TaskExt` class which defines several methods that can
be used to infer a tool for a specific task. Gradle properties can be used to
configure the selected tool. The algorithm currently considers the following
properties:

- `toolchain.${taskName}.version` – the language version for the toolchain

The plugin automatically sets up `JavaCompile`, `JavaExec`, `Javadoc`, and
`Test` tasks to respect the properties by default. A Property can, for example,
be set by specifying a `-P${propertyName}` command line parameter.

    gradlew build -Ptoolchain.test.version=17

This example configures the `test` task to use a Java 17 toolchain. Usually, the
value of the property is parsed using [`JavaLanguageVersion.of(String)`](https://docs.gradle.org/current/javadoc/org/gradle/jvm/toolchain/JavaLanguageVersion.html#of-java.lang.String-).
Additionally, it is possible to set the property to `env` to use the tools from
the runtime that is used to execute the build.

> **Note** While making toolchains configurable from the command-line was the
> primary motivation for this plugin, it is not the only way to set a property.
> [Learn more about Gradle's properties.](https://docs.gradle.org/current/userguide/build_environment.html)


## Compatibility Map

| Gradle | Minimal plugin version |
|--------|------------------------|
| 8.0    | 0.3.0                  |
| 7.4    | 0.1.0                  |


## Building from source

### Setup

This project uses [Gradle's toolchain support](https://docs.gradle.org/8.0.2/userguide/toolchains.html)
to detect and select the JDKs required to run the build. Please refer to the
build scripts to find out which toolchains are requested.

An installed JDK 1.8 (or later) is required to use Gradle.

### Building

Once the setup is complete, invoke the respective Gradle tasks using the
following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the Java library
- `publishToMavenLocal`     - build and install all public artifacts to the
                              local maven repository

Additionally `tasks` may be used to print a list of all available tasks.


## License

```
Copyright (c) 2022-2023 Leon Linhart

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```