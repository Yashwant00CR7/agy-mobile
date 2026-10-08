---
phase: 07-mobile-ai-device-workspace
plan: 01
type: execute
tracer_first: true
---

# Plan: Mobile AI Device Storage & Interactive Folder Navigator

## Objective
Transform agy-mobile into a true mobile AI assistant by enabling direct interaction with real phone files, persisting active device folders across app restarts, providing an in-app interactive folder navigator (`DeviceFolderPickerModal`), and adding dual access points (drawer + chat top bar).

## Requirements Addressed
- REQ-32, REQ-33, REQ-34, REQ-35, REQ-36

## Execution Waves

### Wave 1: Tracer Slice (Persistent Device Workspace Storage)
#### Task 1.1: Persistent Workspace Saving & Startup Initialization
- **Type:** tdd
- **Objective:** Save selected workspace paths to `settings.json` so the app remembers the chosen device folder across restarts, defaulting to phone storage when available.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/WorkspaceManager.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/core/WorkspaceManagerTest.kt`
- **Acceptance Criteria:**
  - `WorkspaceManager` saves and retrieves `lastWorkspacePath` in `settings.json`.
  - On launch, `AgyViewModel` restores `lastWorkspacePath` if valid.
  - Unit tests verify saving, loading, and fallback behavior.
- **Verification Command:** `./gradlew test --tests com.yashwant.agymobile.core.WorkspaceManagerTest`

### Wave 2: In-App Folder Navigator & Dual Access Points
#### Task 2.1: Build `DeviceFolderPickerModal`
- **Type:** auto
- **Objective:** Create an interactive Compose modal bottom sheet for browsing device storage (`/storage/emulated/0`), with breadcrumbs, quick shortcuts (`Downloads`, `Documents`, `Storage Root`), folder listing with subfolder item counts, `..` navigation, and folder creation.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/DeviceFolderPickerModal.kt`
- **Acceptance Criteria:**
  - Modal lists directories from current path.
  - Allows drilling into subfolders and navigating up.
  - "Select This Folder" button selects the active folder.
  - Shows permission banner if `MANAGE_EXTERNAL_STORAGE` is not yet granted.

#### Task 2.2: Dual Access Wiring (Drawer + Chat Top Bar)
- **Type:** auto
- **Objective:** Connect the folder picker modal to both the Navigation Drawer ("Choose Device Folder" button) and an interactive folder switcher pill in the Chat Top Bar.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ChatGptTopBar.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/ChatGptDrawer.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/screens/ChatScreen.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/MainActivity.kt`
- **Acceptance Criteria:**
  - Tapping "Choose Device Folder" in drawer opens the modal.
  - Tapping the workspace pill in the chat top bar opens the modal.
  - Selecting a folder immediately updates `_workspaceInfo` and persists the selection.

### Wave 3: Integration, Verification & Release Build
#### Task 3.1: Full Test Suite, Build APK & Atomic Commit
- **Type:** auto
- **Objective:** Run all unit tests, compile APK, verify size, and commit atomically with personal GitHub identity.
- **Target Files:**
  - All modified files
- **Acceptance Criteria:**
  - `./gradlew test` passes 100%.
  - `./gradlew assembleDebug` succeeds.
  - Git commit under `Yashwant00CR7 <10a19yashwant@gmail.com>`.
- **Verification Command:** `./gradlew test && ./gradlew assembleDebug`
