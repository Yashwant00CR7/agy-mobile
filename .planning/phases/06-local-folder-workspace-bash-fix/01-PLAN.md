---
phase: 06-local-folder-workspace-bash-fix
plan: 01
type: execute
tracer_first: true
---

# Plan: Local Device Folder Workspaces & Native Bash Execution Hardening

## Objective
Enable opening chat sessions directly scoped to any local mobile device folder via Android SAF and quick drawer shortcuts, and eliminate the "bash is not callable" error by hardening shell resolution in `BootstrapManager` and deploying model tool rules.

## Requirements Addressed
- REQ-28, REQ-29, REQ-30, REQ-31

## Execution Waves

### Wave 1: Tracer Slice (Bash Execution Hardening)
#### Task 1.1: Native Bash Execution & Environment Hardening
- **Type:** tdd
- **Objective:** Ensure any call to `bash` within the agent shell process cleanly resolves to `libbash.so` without hitting Android 10+ W^X errno 13, and provide agent rules directing models to use `run_command`.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`
  - `android/app/src/main/assets/rules/system-android.md`
  - `android/app/src/test/java/com/yashwant/agymobile/core/BootstrapManagerTest.kt`
- **Acceptance Criteria:**
  - `BootstrapManager` generates a persistent `shell_env.sh` script in `geminiDir` defining `bash() { "$SHELL" "$@"; }`.
  - `BASH_ENV` and `ENV` point to this script in `getEnvironmentVariables()`.
  - Android rule instructs agent to use `run_command` rather than uncallable `bash` tool.
  - Tests in `BootstrapManagerTest.kt` verify `BASH_ENV`, `ENV`, and shell script creation.
- **Verification Command:** `./gradlew test --tests com.yashwant.agymobile.core.BootstrapManagerTest`
- **Reversibility:** two-way

### Wave 2: Local Device Folder Workspaces & Chat Binding
#### Task 2.1: SAF Folder Picker & Workspace Drawer Shortcuts
- **Type:** auto
- **Objective:** Support picking any device directory via `ActivityResultContracts.OpenDocumentTree()` in `MainActivity`, provide quick drawer shortcuts (Downloads, Documents, Storage root), and bind chat sessions directly to the active folder in `WorkspaceManager` and `AgyViewModel`.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/MainActivity.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/core/WorkspaceManager.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/ChatGptDrawer.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/core/WorkspaceManagerTest.kt`
- **Acceptance Criteria:**
  - "Open Device Folder" launches SAF folder picker and converts tree URI to device path.
  - Quick shortcuts for `/storage/emulated/0/Download`, `/storage/emulated/0/Documents`, and `/storage/emulated/0` are accessible in the drawer.
  - Resuming chat restores its bound folder, and starting a new chat in a chosen folder scopes turns to that directory.
- **Verification Command:** `./gradlew test --tests com.yashwant.agymobile.core.WorkspaceManagerTest`

### Wave 3: Integration, Polish & Build Hardening
#### Task 3.1: Full Suite Test, Build APK & Atomic Commit
- **Type:** auto
- **Objective:** Run all unit tests, compile APK, verify size, and commit atomically with personal GitHub identity.
- **Target Files:**
  - All modified files
- **Acceptance Criteria:**
  - `./gradlew test` passes 100%.
  - `./gradlew assembleDebug` builds `android/agy-mobile-debug.apk`.
  - Git commit under `Yashwant00CR7 <10a19yashwant@gmail.com>`.
- **Verification Command:** `./gradlew test && ./gradlew assembleDebug`
