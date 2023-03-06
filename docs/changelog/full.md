### 0.3.0

_Released 2023 Mar 06_

#### Improvements

- Introduced an experimental API that can be used to infer toolchains for
  arbitrary tasks.
- Toolchain tools are now configured to fallback to use the build JVM as last
  fallback.
  - This fixes a few edge-cases that could break compatibility with Gradle 8.

#### Fixes

- Fixed an issue that could cause the plugin configuration to be applied
  multiple times under rare circumstances.


---

### 0.2.0

_Released 2022 Sep 26_

#### Improvements

- The plugin now configures the tasks after the `JavaBasePlugin` has been
  applied. If the `JavaBasePlugin` is not applied, this plugin does nothing.

#### Fixes

- Fixed a crash that could occur if this plugin was applied before the
  `JavaBasePlugin`.


---

### 0.1.0

_Released 2022 Sep 02_

#### Overview

A Gradle plugin that adds command line parameters that may be used to
dynamically switch between toolchains for specific tasks.