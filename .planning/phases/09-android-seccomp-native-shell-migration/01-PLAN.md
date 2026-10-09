---
phase: 09-android-seccomp-native-shell-migration
plan: 01
type: execute
tracer_first: true
---

# Plan: Android Seccomp Resolution & Native Bionic Shell Migration

## Objective
Completely eliminate exit code 159 (`SIGSYS` / Bad System Call) during command execution (`run_command`, `ls`, `find`, etc.) by removing the static glibc `libbash.so` binary, migrating primary shell resolution to Android's built-in Bionic `/system/bin/sh`, linking `bin/bash` directly to `/system/bin/sh`, updating environment rules, and updating UI diagnostic inspectors.

## Requirements Addressed
- REQ-43: Native Bionic Shell Resolution & Purge `libbash.so`
- REQ-44: Android Environment & Rule Hardening
- REQ-45: Drawer Settings & Engine Diagnostics UI Update
- REQ-46: Test Suite, Code Review & Release APK Build

## Execution Waves

### Wave 1: Tracer Slice (Native Bionic Shell Resolution & Purge `libbash.so`)
- **Type:** tdd
- **Objective:**
  - Delete `android/app/src/main/jniLibs/arm64-v8a/libbash.so` to remove glibc binary that triggers Android seccomp filter violations.
  - Update `BootstrapManager.kt`:
    - `getShellExecutable()` prioritizes `/system/bin/sh` on Android and `/bin/sh` on Unix/macOS.
    - `setupShellLinks()` links both `binDir/bash` and `binDir/sh` to the resolved system shell.
    - `createShellWrapperScript()` invokes `/system/bin/sh "$@"`.
  - Update `BootstrapManagerTest.kt` to reflect native system shell resolution without requiring `libbash.so`.
- **Target Files:**
  - `android/app/src/main/jniLibs/arm64-v8a/libbash.so` (delete)
  - `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/core/BootstrapManagerTest.kt`
- **Verification Command:** `cd android && ./gradlew test --tests com.yashwant.agymobile.core.BootstrapManagerTest`

### Wave 2: Android Environment & Rules Hardening
- **Type:** auto
- **Objective:**
  - Update `setupShellEnvironment()` in `BootstrapManager.kt` to set `SHELL` to the system shell executable.
  - Update `rules/system-android.md` and `rules/android-runtime.md` to instruct the agent that `/system/bin/sh` (mksh/toybox) is the native shell.
  - Update `ChatGptDrawer.kt` Engine & Shell Logs dialog and About section to display `/system/bin/sh (Android Bionic Shell)`.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/ChatGptDrawer.kt`
- **Acceptance Criteria:**
  - `SHELL` environment variable points to `/system/bin/sh`.
  - Engine logs diagnostic header reflects `/system/bin/sh (Android Bionic Shell)`.

### Wave 3: Verification, Review & Release Build
- **Type:** auto
- **Objective:**
  - Run full test suite (`./gradlew test`).
  - Run parallel Standards and Spec review subagents.
  - Assemble debug APK (`./gradlew assembleDebug`) and verify APK size reduction (~71.6MB).
  - Verify git commits follow personal identity policy.
- **Verification Command:** `cd android && ./gradlew test && ./gradlew assembleDebug`
