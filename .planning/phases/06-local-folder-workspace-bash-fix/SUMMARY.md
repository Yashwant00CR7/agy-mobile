# Phase 06 Summary: Local Device Folder Workspaces & Native Bash Execution Hardening

## Overview
Successfully implemented local mobile device folder workspace selection, chat-to-folder binding, and hardened native bash execution inside Android ARM64 runtime.

## Outcomes Delivered
1. **Native Android SAF Folder Picker & Quick Shortcuts (REQ-28)**:
   - Added `ActivityResultContracts.OpenDocumentTree` in `MainActivity.kt` to allow users to select any device directory via the native Android Files picker.
   - Encapsulated SAF Document ID parsing in `WorkspaceManager.resolveTreeDocIdToPath` to cleanly extract `/storage/emulated/0/...` paths.
   - Provided quick shortcuts in `ChatGptDrawer` for standard device directories (Downloads, Documents, Storage Root).
   - Ensured directory selection validates `MANAGE_APP_ALL_FILES_ACCESS_PERMISSION` before opening.
2. **Chat-to-Folder Binding & Persistence (REQ-29)**:
   - Preserved workspace folder paths in `ConversationSession` across turns and app restarts.
   - Resuming chats automatically restores the bound device workspace.
3. **Native Bash Callable Hardening in Android Runtime (REQ-30)**:
   - `BootstrapManager` generates `shell_env.sh` and wires `BASH_ENV` and `ENV` to ensure subshells and scripts running `bash` cleanly resolve to `libbash.so` without hitting Android 10+ W^X `errno 13` (Permission Denied).
   - Deployed `system-android.md` and `android-runtime.md` in `~/.gemini/rules/` and APK assets instructing LLM agents to always use `run_command` rather than uncallable `bash` function calls.
   - Extracted bundled rules in `BootstrapManager.extractBundledAssets`.
4. **Comprehensive Testing & Build Verification (REQ-31)**:
   - Added unit test suites in `BootstrapManagerTest.kt`, `WorkspaceManagerTest.kt`, and `AgyProcessEngineTest.kt`.
   - Verified 100% test pass on both debug and release configurations (`./gradlew test`).
   - Compiled updated release APK `android/agy-mobile-debug.apk` (74 MB).

## Commits
- `0db31da`: feat(shell): harden native bash callable environment and inject android runtime rules
- `a86678c`: feat(workspace): support opening local mobile folders via SAF picker and quick drawer shortcuts
- `[Pending]`: fix(review): address standards & spec review findings, update tests, and finalize release
