# Requirements: agy-mobile

## Completed Milestones
- [x] **REQ-01**: Bundle native ARM64 bash binary with execute permissions.
- [x] **REQ-02**: Implement Terminal / Shell execution helper.
- [x] **REQ-03**: Support device-wide storage access and permission flows.
- [x] **REQ-04**: Bundle GSD Core skills in assets and extract to runtime directory.
- [x] **REQ-05**: Inject shell and GSD environment variables to agent process.
- [x] **REQ-06**: Verify shell execution and GSD workflow end-to-end.
- [x] **REQ-07**: Update color palette and theme to authentic Antigravity Dark.
- [x] **REQ-08**: Implement full ChatGPT-style left sliding `ModalNavigationDrawer`.
- [x] **REQ-09**: Implement ChatGPT-style top bar with centered model dropdown pill.
- [x] **REQ-10**: Overhaul chat message layout to fluid ChatGPT canvas.
- [x] **REQ-11**: Redesign bottom composer as a floating capsule pill.
- [x] **REQ-12**: Redesign empty state (welcome screen).
- [x] **REQ-13**: Maintain full integration with existing `AgyViewModel` and background service.
- [x] **REQ-14**: Turn Latency & Folder-Wise Context Scoping.
- [x] **REQ-15**: Instant Visual Thinking State.
- [x] **REQ-16**: Conversation Session Persistence.
- [x] **REQ-17**: Session Restoration & App Startup.
- [x] **REQ-18**: Keyboard IME Padding & Auto-Scroll.
- [x] **REQ-19**: End-to-End Build & Verification.
- [x] **REQ-20**: Lean Skills Profile.
- [x] **REQ-21**: In-App Reasoning Effort Bar.
- [x] **REQ-22**: Effort Integration in Engine & TopBar.
- [x] **REQ-23**: Verification & Release Build v1.4.0.
- [x] **REQ-24**: Fixed-Tier Model Mapping & Conflict Elimination.
- [x] **REQ-25**: Persistent Stream-JSON Daemon in AgyProcessEngine.
- [x] **REQ-26**: UI Synchronization & Effort Locking.
- [x] **REQ-27**: Comprehensive Verification & Build v1.5.0.
- [x] **REQ-28**: Local Mobile Device Folder Workspaces.
- [x] **REQ-29**: Direct Chat-to-Folder Binding & Persistence.
- [x] **REQ-30**: Native Bash Callable Hardening in Android Runtime.
- [x] **REQ-31**: Testing, Verification & Release Build v1.6.0.

---

## Milestone v1.7.0: Mobile AI Device Storage & Interactive In-App Folder Navigator

- [ ] **REQ-32**: **In-App Device Folder Navigator & Picker Modal**:
  - Build `DeviceFolderPickerModal.kt` providing an interactive directory browser navigating through device storage (`/storage/emulated/0`), with breadcrumbs, quick shortcuts (`Downloads`, `Documents`, `Storage Root`), folder listing with subfolder item counts, `.. (Parent Folder)` navigation, "+ New Folder" dialog, and a primary "Select This Folder" action button.
  - If `Environment.isExternalStorageManager()` is false, display an in-modal permission prompt banner with a one-tap action taking the user to Android Settings to enable "All Files Access".
- [ ] **REQ-33**: **Persistent Mobile Workspace Across App Launches**:
  - In `WorkspaceManager` and `AgyViewModel`, persist the last selected workspace path to `settings.json`.
  - On app launch, if a device workspace was previously chosen and still exists, automatically initialize and restore that mobile folder as the active workspace instead of reverting to the app-internal default `workspaces/` directory.
  - If storage permission is granted and no custom workspace has been set, default to a device folder (e.g. `/storage/emulated/0/Download` or `/storage/emulated/0`) rather than app private internal storage so the app behaves like a true mobile AI.
- [ ] **REQ-34**: **Dual Access Points (Drawer & Top Bar) for Mobile Folder Switching**:
  - Navigation Drawer (`ChatGptDrawer.kt`): Add a prominent "Choose Device Folder" action card and allow tapping the current workspace pill to open the modal.
  - Chat Top Bar (`ChatGptTopBar.kt` / `ChatScreen.kt`): Add an interactive folder indicator pill beside or below the model selector (e.g. `📁 Download ▾`) so the user can see and change their active device folder directly from the chat screen at any time without opening the drawer.
- [ ] **REQ-35**: **Device File Interaction & Engine Workspace Binding**:
  - When a device folder is chosen, instantly update `AgyViewModel`, notify the user with a subtle system message ("Workspace switched to: /path"), and bind all future queries, file edits, and commands to that path.
  - `AgyProcessEngine` terminates any stale background daemon if the workspace path changes and spawns the engine in the new device folder with `ANTIGRAVITY_WORKSPACE_ROOT`, `WORKSPACE_DIR`, `PWD`, and `processBuilder.directory(folder)`.
- [ ] **REQ-36**: **Unit Testing & Release Build Verification**:
  - Add unit tests for `WorkspaceManager` persistent workspace loading/saving and directory navigation.
  - Run `./gradlew test` and compile `android/agy-mobile-debug.apk`.
