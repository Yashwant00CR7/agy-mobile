# Phase Context: Local Mobile Device Folder Workspaces & Native Bash Execution Hardening

## Problem & User Intent
1. **Local Mobile Folder Workspaces:** The user wants to directly open and run chats scoped to any local mobile device folder (e.g. in `/storage/emulated/0`, `/storage/emulated/0/Download`, `/storage/emulated/0/Documents`, or selected via Android's file picker). Chats and file operations must bind directly to that folder as the active workspace.
2. **"bash is not callable" Error:** In some cases on Android, attempts to execute bash commands fail with "bash is not callable" or command resolution errors. This stems from two sources:
   - Android 10+ (API 29+) SELinux W^X restrictions preventing execution of binaries or symlinks located in app data storage (`binDir/bash` fails with errno 13 Permission Denied), while `libbash.so` resides in `nativeLibraryDir` without a standalone file named `bash`.
   - LLMs or skills attempting to call a non-existent tool named `bash` rather than the official Antigravity tool `run_command`.

## Mental Model & Core Abstractions
- **Workspace as Any Accessible Path:** Workspaces are no longer restricted to internal `workspaces/` folders. Any valid folder on internal storage (`/storage/emulated/0/...`) can be an active workspace if storage permission (`MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`) is granted.
- **SAF Directory Selection:** The user can tap "Open Device Folder" in the navigation drawer, which launches the Android System Folder Picker (`ACTION_OPEN_DOCUMENT_TREE`) or taps quick shortcuts (Downloads, Documents, Storage Root). The resolved path is passed to `WorkspaceManager` and `AgyViewModel`.
- **Session-Workspace Binding:** Each chat session records `workspacePath`. Resuming a chat immediately activates its bound folder; switching folders creates/switches the context for future turns.
- **Bulletproof Bash Resolution:**
  - `BootstrapManager` initializes an environment profile (`~/.gemini/shell_env.sh`) configured via `BASH_ENV` and `ENV`. It exports a shell function `bash() { "$SHELL" "$@"; }` so any command running `bash ...` resolves directly to `libbash.so` inside the shell without needing a forbidden binary in `binDir`.
  - System rule in `~/.gemini/rules/system-android.md` informs the model that `run_command` is the tool to use for shell operations on Android, preventing hallucinatory calls to an uncallable `bash` tool.

## Verification & Backpressure Seams
- Unit Tests: `./gradlew test`
- Build: `./gradlew assembleDebug`
- Verification of test suites for `WorkspaceManagerTest`, `BootstrapManagerTest`, and `AgyProcessEngineTest`.
