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

---

## Milestone v1.6.0: Local Device Folder Workspaces & Native Bash Execution Hardening

- [ ] **REQ-28**: **Local Mobile Device Folder Workspaces**: Add native Android Storage Access Framework (SAF) folder picker (`ACTION_OPEN_DOCUMENT_TREE`) + direct shortcuts (e.g. `Downloads`, `Documents`, Device Storage Root `/storage/emulated/0`) in `MainActivity` and `ChatGptDrawer`. When a user chooses or navigates to a local device folder, set the active workspace directory to that exact directory, bind chat sessions to it, and pass it to `libagy.so` as `ANTIGRAVITY_WORKSPACE_ROOT`, `WORKSPACE_DIR`, and process working directory.
- [ ] **REQ-29**: **Direct Chat-to-Folder Binding & Persistence**: In `ConversationRepository` and `WorkspaceManager`, preserve the exact external folder path associated with each chat session so resuming a chat instantly restores that folder as the active workspace, and starting a new chat in a local folder creates new chat history tied to that directory.
- [ ] **REQ-30**: **Native Bash Callable Hardening in Android Runtime**:
  1. In `BootstrapManager`, resolve `bash` command executions cleanly by injecting `BASH_ENV` and `ENV` startup scripts that alias/export `bash() { "$SHELL" "$@"; }`, ensuring any subshell, command, or script invoking `bash` routes seamlessly to `libbash.so` without failing with errno 13 (Permission denied) on Android 10+ W^X app data storage.
  2. Provide system rules/instructions in `~/.gemini/rules/system-android.md` ensuring the agent runtime and models always use `run_command` rather than uncallable tool names like `bash()`.
  3. Ensure fallback to `/system/bin/sh` if `libbash.so` is unavailable.
- [ ] **REQ-31**: **Testing, Verification & Release Build**: Update unit tests in `WorkspaceManagerTest.kt`, `BootstrapManagerTest.kt`, and `AgyProcessEngineTest.kt`. Verify with `./gradlew test` and build the updated debug APK `android/agy-mobile-debug.apk`.
