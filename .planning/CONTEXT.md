# Phase Context: Mobile Bash Runtime, Storage Access & GSD Bundle

## Problem & User Intent
When running Antigravity on mobile (`agy-mobile`), the agent fails when attempting shell searches across storage because Android lacks `bash` in `/system/bin` and restricts app storage by default. Furthermore, the user wants the full GSD Core framework integrated into the mobile app to empower spec-driven development on their phone.

## Mental Model & Core Abstractions
- **Native Library Execution Vector:** On Android 10+ (API 29+), binaries in standard app directories cannot be executed (`error=13 Permission denied`). However, binaries inside `nativeLibraryDir` (packaged as `lib*.so` in `jniLibs/arm64-v8a/` with `useLegacyPackaging = true`) are granted executable permissions and `apk_data_file` SELinux context.
- **Shell Resolution Protocol:** `BootstrapManager` resolves the shell executable:
  1. Primary: `libbash.so` inside `nativeLibraryDir`.
  2. Fallback: `/system/bin/sh` (POSIX compliant mksh/toybox).
  It exports `SHELL` and `PATH` so `libagy.so` and child processes execute without "bash not found" errors.
- **Device File Access:** `MANAGE_EXTERNAL_STORAGE` unlocks `/sdcard`, allowing shell commands like `find /sdcard/Download -iname "*<query>*"` to locate files across the phone.
- **GSD Plugin Engine:** Pre-packaged GSD skills inside Android assets are extracted to the user's `~/.gemini/config/skills/` on device startup. The user triggers them via `/gsd-*` slash commands in the minimal chat UI.

## Locked Architectural Decisions
- Static ARM64 bash binary bundled as `libbash.so` in `android/app/src/main/jniLibs/arm64-v8a/`.
- `BootstrapManager.kt` exports `SHELL` pointing to `libbash.so` (or `/system/bin/sh`).
- Symlinks `binDir/bash -> libbash.so` and `binDir/sh -> /system/bin/sh`.
- `AndroidManifest.xml` declares `MANAGE_EXTERNAL_STORAGE` and `READ_EXTERNAL_STORAGE`.
- Just-in-Time prompt in `ChatScreen`/`AgyViewModel` prompts user with an interactive dialog linking directly to `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`.
- Full GSD Core skills packaged in `android/app/src/main/assets/skills/` and auto-extracted by `BootstrapManager.kt`.
- UI remains minimal: slash commands (`/gsd-*`) route directly to `libagy.so`.

## Verification & Backpressure Seams
- Unit tests: `./gradlew test` (verifying `BootstrapManagerTest`, environment variables, shell resolution).
- Build verification: `./gradlew assembleDebug` (producing verified `android/agy-mobile-debug.apk`).
- APK asset verification: inspecting APK entries for `libbash.so`, `libagy.so`, and GSD skills.
