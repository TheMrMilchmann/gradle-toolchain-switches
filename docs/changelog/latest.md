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