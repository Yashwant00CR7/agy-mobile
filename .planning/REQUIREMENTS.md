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

---

## Milestone v1.4.0: Context Minimization & In-App Reasoning Effort Control
- [x] **REQ-20**: **Lean Skills Profile**: Prune active skills in `~/.gemini/config/skills/` down to core essentials (`ralph-loop`, `gsd-quick`, `gsd-fast`), moving remaining 70+ skills to `~/.gemini/skills-library/` on-demand storage.
- [x] **REQ-21**: **In-App Reasoning Effort Bar**: Build an interactive, in-app reasoning effort selector bar (`Low ⚡`, `Medium ⚖️`, `High 🧠`).
- [x] **REQ-22**: **Effort Integration in Engine & TopBar**: Wire the selected effort state (`low`, `medium`, `high`) through `AgyViewModel`.
- [x] **REQ-23**: **Verification & Release Build**: Ensure all unit tests pass (`./gradlew test`) and compile updated debug APK (`./gradlew assembleDebug`).

---

## Milestone v1.5.0: Model Conflict Resolution & Persistent Stream-JSON Daemon

- [x] **REQ-24**: **Fixed-Tier Model Mapping & Conflict Elimination**: Enforce strict base-model mapping in `BootstrapManager` and `AgyProcessEngine`. If a model has a fixed tier (e.g. `Gemini 3.1 Pro (High)`, `Gemini 3.1 Pro (Low)`, `Claude Sonnet 4.6`, `Claude Opus 4.6`, `GPT-OSS 120B`), omit the `--effort` CLI argument entirely to prevent `invalid model selection conflicts with --effort` crashes. For dynamic models (`Gemini 3.8 Flash`, `3.7 Flash`, `3.6 Flash`), supply the clean base slug with `--effort <level>`.
- [x] **REQ-25**: **Persistent Stream-JSON Daemon in AgyProcessEngine**: Refactor `AgyProcessEngine` to keep the native `libagy.so` process alive across turns using `--input-format stream-json --output-format stream-json`. Send prompts over stdin via `{"event":"user","message":{"content":prompt}}`. Cold start (10-14s) occurs once; all subsequent turns execute in ~1-2 seconds.
- [x] **REQ-26**: **UI Synchronization & Effort Locking**: In `ModelSelectorBottomSheet` and `ChatGptTopBar`, automatically lock and reflect the model's fixed tier when selected (e.g. showing "High (Locked)" or disabling conflicting effort buttons for Gemini 3.1 Pro / Claude), preventing invalid user selections.
- [x] **REQ-27**: **Comprehensive Verification & Build**: Update unit tests in `AgyProcessEngineTest` and `BootstrapManagerTest`, run `./gradlew test`, and build updated release APK `android/agy-mobile-debug.apk`.
