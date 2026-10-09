# Phase Context: Android Seccomp Resolution & Native Bionic Shell Migration

## Problem & User Intent
When asking the agent `what is in this dir` on Android, the agent attempts to run `ls` or `find` via `run_command` and fails with exit code 159 (`SIGSYS` / Bad System Call).
The root cause is that `libbash.so` was a static glibc Linux executable. When launched in an Android app sandbox, glibc startup syscalls trigger Android's kernel seccomp filter, resulting in `SIGSYS` (signal 31 = exit code 159).
The user confirmed standardizing on Android's built-in Bionic `/system/bin/sh` (mksh/toybox), purging `libbash.so`, and symlinking `binDir/bash` and `binDir/sh` to `/system/bin/sh`.

## Locked Architectural Decisions
- **Purge `libbash.so`**: Delete `android/app/src/main/jniLibs/arm64-v8a/libbash.so`. This permanently prevents seccomp violations and reduces APK size by ~2.4MB.
- **Native Bionic Shell**: Resolve shell directly to `/system/bin/sh` on Android (`/bin/sh` on Unix/macOS fallback).
- **Symlinks & Wrappers**: Self-heal `binDir/bash` and `binDir/sh` to point to `/system/bin/sh`. If symlinking is blocked, fallback script invokes `/system/bin/sh "$@"`.
- **Environment & Rules**: `SHELL=/system/bin/sh`, rules specify Android Bionic shell execution.
- **UI Diagnostics**: Display `/system/bin/sh (Android Bionic Shell)` in drawer settings and logs viewer.

## Verification Seams
- `./gradlew test --tests com.yashwant.agymobile.core.BootstrapManagerTest`
- `./gradlew test`
- `./gradlew assembleDebug`
