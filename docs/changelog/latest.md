### 0.2.0

_Released 2022 Sep 26_

### Improvements

- The plugin now configures the tasks after the `JavaBasePlugin` has been
  applied. If the `JavaBasePlugin` is not applied, this plugin does nothing.

### Fixes

- Fixed a crash that could occur if this plugin was applied before the
  `JavaBasePlugin`.