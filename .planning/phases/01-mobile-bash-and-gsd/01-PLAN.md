---
phase: 01-mobile-bash-and-gsd
plan: 01
type: execute
tracer_first: true
---

# Plan 01: Mobile Bash Runtime, Storage Access & GSD Bundle

## Objective
Enable native bash command execution in `agy-mobile` using a bundled ARM64 static binary, unlock Android device storage searches via `MANAGE_EXTERNAL_STORAGE`, and package the complete GSD Core skill suite for mobile spec-driven development.

## Requirements Addressed
- REQ-01, REQ-02, REQ-03, REQ-04, REQ-05, REQ-06

---

## Execution Waves

### Wave 1: Tracer Slice (Native Bash & Shell Environment)
*The tracer slice proves end-to-end shell resolution and environment export.*

#### Task 1.1: Bundle Static ARM64 Bash as Native Library
- **Type:** auto
- **Objective:** Copy the verified static ARM64 bash binary into `android/app/src/main/jniLibs/arm64-v8a/libbash.so`.
- **Target Files:**
  - `android/app/src/main/jniLibs/arm64-v8a/libbash.so`
- **Acceptance Criteria:** `libbash.so` exists, is an ELF 64-bit aarch64 executable, and is packaged into the APK native library directory.
- **Verification Command:** `file android/app/src/main/jniLibs/arm64-v8a/libbash.so`
- **Reversibility:** two-way

#### Task 1.2: Upgrade BootstrapManager for Shell Resolution & Symlinks
- **Type:** tdd
- **Objective:** Add `getShellExecutable()` in `BootstrapManager.kt`, export `SHELL` in `getEnvironmentVariables()`, add nativeLibDir to PATH, and create executable symlinks in `binDir`.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`
- **Acceptance Criteria:** `getShellExecutable()` returns `libbash.so` if available, or `/system/bin/sh`; `getEnvironmentVariables()["SHELL"]` points to the resolved shell.
- **Verification Command:** `cd android && ./gradlew test --tests "*BootstrapManagerTest*"`

#### Task 1.3: Add Shell Resolution Tests
- **Type:** tdd
- **Objective:** Add comprehensive tests in `BootstrapManagerTest.kt` verifying shell selection, fallback to `/system/bin/sh`, and environment variables.
- **Target Files:**
  - `android/app/src/test/java/com/yashwant/agymobile/BootstrapManagerTest.kt`
- **Acceptance Criteria:** Tests pass covering native shell resolution, fallback, and environment configuration.
- **Verification Command:** `cd android && ./gradlew test`

---

### Wave 2: Storage Access & Just-in-Time Permission
*Unlocks full device storage searches across /sdcard, Downloads, and Documents.*

#### Task 2.1: Add Storage Permissions to Manifest
- **Type:** auto
- **Objective:** Declare `MANAGE_EXTERNAL_STORAGE` and `READ_EXTERNAL_STORAGE` in `AndroidManifest.xml`.
- **Target Files:**
  - `android/app/src/main/AndroidManifest.xml`
- **Acceptance Criteria:** Manifest includes permissions with Android 11+ storage management settings.
- **Verification Command:** `git diff android/app/src/main/AndroidManifest.xml`

#### Task 2.2: Implement Just-in-Time All Files Access Dialog
- **Type:** auto
- **Objective:** Add helper in `MainActivity.kt` and `ChatScreen.kt` to check `Environment.isExternalStorageManager()` and show an unobtrusive permission banner/card when storage access is needed, linking directly to `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/MainActivity.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/screens/ChatScreen.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
- **Acceptance Criteria:** App detects missing storage permission without crashing and provides a 1-tap intent to system settings.
- **Verification Command:** `cd android && ./gradlew compileDebugKotlin`

---

### Wave 3: GSD Core Plugin Bundle & Mobile Extraction
*Integrates the complete GSD Core framework into the mobile app.*

#### Task 3.1: Bundle Full GSD Skills into Android Assets
- **Type:** auto
- **Objective:** Copy all 72 GSD Core skills from `~/.gemini/config/skills/` into `android/app/src/main/assets/skills/`.
- **Target Files:**
  - `android/app/src/main/assets/skills/*`
- **Acceptance Criteria:** All GSD skills (`gsd-plan-phase`, `gsd-execute-phase`, `gsd-verify-work`, `gsd-ship`, etc.) exist in the assets directory.
- **Verification Command:** `ls -1 android/app/src/main/assets/skills | wc -l` (>= 70 skills)

#### Task 3.2: Implement Asset Extraction in BootstrapManager
- **Type:** tdd
- **Objective:** Add `extractBundledSkills(context: Context)` in `BootstrapManager.kt` to extract bundled assets into `homeDir/.gemini/config/skills/` on app startup.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/MainActivity.kt`
- **Acceptance Criteria:** On first launch or update, GSD skills are unpacked to `.gemini/config/skills/`.
- **Verification Command:** `cd android && ./gradlew test`

---

### Wave 4: Integration Build & End-to-End Verification
*Verifies compilation, unit tests, and production APK packaging.*

#### Task 4.1: Run Full Test Suite
- **Type:** auto
- **Objective:** Run all unit tests across the Android project.
- **Verification Command:** `cd android && ./gradlew test`

#### Task 4.2: Build and Verify Debug APK
- **Type:** auto
- **Objective:** Assemble `android/app/build/outputs/apk/debug/app-debug.apk`, copy to `android/agy-mobile-debug.apk`, and verify native libraries and assets.
- **Verification Command:** `cd android && ./gradlew assembleDebug`
