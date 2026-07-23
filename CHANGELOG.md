# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased] - 2026-04-02

### Changed
- Restricted the mod to the client environment and moved client-only join initialization out of the main entrypoint.
- Reworked module state application so config loads no longer rely on `toggle()` side effects.
- Replaced ad-hoc console printing with structured logging in client bootstrap, config, friends, and FakeLag paths.
- Stabilized ClickGUI layout handling so the settings panel uses explicit layout context instead of temporary coordinate spoofing.

### Fixed
- Restored player and keybind state correctly when disabling modules such as Freecam, Fly, Sprint, InvMove, Scaffold, and Blink.
- Moved AutoClicker off a background thread and back onto the client tick path to avoid unsafe client-state access.
- Implemented Timer speed scaling through a render tick mixin instead of leaving the module as a placeholder.
- Fixed `Utils.inFov()` so it performs a real yaw-based FOV check.
- Added safer packet handling for NoRotate and Blink flush behavior.

### Added
- Wired STap into both KillAura attacks and manual attacks.
- Implemented `weaponOnly` handling in KillAura.
- Added functional Reach handling through interaction reach hooks.
- Added smarter AutoSwap weapon selection based on actual attack damage modifiers.
- Added BreakProgress rendering for the current block being mined.
- Added runtime loading for vanilla post-processing shaders.
- Added accessor and invoker mixins required for reach, packet rotation, block breaking, and shader loading support.
