---
phase: 08-mobile-shell-diagnostics-conversation-deletion
plan: 01
type: execute
tracer_first: true
---

# Plan: Shell Path Hardening, Cold-Start Optimization, Conversation Deletion & Command Inspector

## Objective
Fix native bash execution errors on Android device storage folders with self-healing symlinks and system sh fallback, suppress daemon stream close errors, support conversation deletion with confirmation in the navigation drawer, and provide in-chat collapsible command execution cards and an engine debug log viewer in settings.

## Requirements Addressed
- REQ-37, REQ-38, REQ-39, REQ-40, REQ-41, REQ-42

## Execution Waves

### Wave 1: Tracer Slice (Shell Hardening & Daemon Stream Resiliency)
#### Task 1.1: Self-Healing Bash Links & Fallback Wrapper
- **Type:** tdd
- **Objective:** Fix `exec: "bash": executable file not found in $PATH` by deleting broken/dangling symlinks with `Files.deleteIfExists()` before recreating them on every bootstrap, and generating an executable shell script wrapper fallback at `binDir/bash` that delegates to `/system/bin/sh "$@"`.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/core/BootstrapManagerTest.kt`
- **Acceptance Criteria:**
  - `BootstrapManager` recreates valid symlinks even if target moved or link was dangling.
  - Generates executable fallback script if symlinking fails.
  - Adds tests for symlink replacement and fallback creation.
- **Verification Command:** `cd android && ./gradlew test --tests com.yashwant.agymobile.core.BootstrapManagerTest`

#### Task 1.2: Daemon Stream Resiliency & Warm Turn Optimization
- **Type:** auto
- **Objective:** Prevent `IOException: Read interrupted by close() on another thread` from firing as an error banner, and keep the daemon process alive and warm across turns in the same conversation and device folder.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/AgyProcessEngine.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
- **Acceptance Criteria:**
  - Stream closures during planned teardown or turn completion do not emit `AgyEvent.Error`.
  - Background daemon stays warm across turns within the same conversation and workspace.

### Wave 2: Conversation Deletion with Confirmation Dialog
#### Task 2.1: Deletion Logic in Repositories & ViewModel
- **Type:** tdd
- **Objective:** Add `deleteConversation(id)` to `ConversationRepository`, delete corresponding brain session directory in `WorkspaceManager` (`.gemini/brain/<id>`), and wire to `AgyViewModel` with active chat reset if deleted.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/ConversationRepository.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/core/WorkspaceManager.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/core/ConversationRepositoryTest.kt`
- **Acceptance Criteria:**
  - `deleteConversation(id)` removes JSON and brain files.
  - If deleted conversation is active, `AgyViewModel` resets state.
  - Unit tests verify deletion and active reset.
- **Verification Command:** `cd android && ./gradlew test --tests com.yashwant.agymobile.core.ConversationRepositoryTest`

#### Task 2.2: Drawer Deletion Action & AlertDialog UX
- **Type:** auto
- **Objective:** Add trash icon action to conversation list items in `ChatGptDrawer.kt`, triggering a clean Material 3 confirmation dialog before invoking ViewModel deletion.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/ChatGptDrawer.kt`
- **Acceptance Criteria:**
  - Each conversation item has a delete action.
  - Confirmation dialog warns the user and provides Cancel/Delete buttons.
  - Deletion removes item immediately and refreshes the list.

### Wave 3: In-Chat Command Execution Cards & Settings Debug Logs
#### Task 3.1: In-Chat Collapsible Command Cards
- **Type:** auto
- **Objective:** Render `MessageRole.TOOL` messages as interactive collapsible command cards showing command line, working directory, status, duration, and formatted stdout/stderr output.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/screens/ChatScreen.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/MessageItem.kt` (or within ChatScreen)
- **Acceptance Criteria:**
  - Tool calls for `run_command` render as styled cards.
  - Collapsible accordion shows stdout/stderr output.
  - Shows execution status (Running, Success, Error).

#### Task 3.2: Engine Debug Logs & Process Inspector in Settings
- **Type:** auto
- **Objective:** Add an "Engine Logs & Shell Commands" viewer in the Settings dialog displaying recent process execution history, daemon status, environment variables, and stdout/stderr output lines for real-time mobile debugging.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/AgyProcessEngine.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/ChatGptDrawer.kt`
- **Acceptance Criteria:**
  - `AgyProcessEngine` maintains a bounded circular buffer of recent engine logs and command executions.
  - Settings dialog displays expandable log inspector sheet with copy and refresh actions.

### Wave 4: Verification, Review & Release Build
#### Task 4.1: Full Test Suite, Review & APK Build
- **Type:** auto
- **Objective:** Run all unit tests, parallel code review subagents (Standards & Spec), build debug APK, and commit atomically with personal GitHub identity.
- **Target Files:**
  - All modified files
- **Acceptance Criteria:**
  - All unit tests pass (`./gradlew test`).
  - Code review reports no critical issues.
  - `android/agy-mobile-debug.apk` is compiled.
- **Verification Command:** `cd android && ./gradlew test && ./gradlew assembleDebug`
