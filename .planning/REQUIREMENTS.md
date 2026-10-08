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

- [x] **REQ-20**: **Lean Skills Profile**: Prune active skills in `~/.gemini/config/skills/` down to core essentials (`ralph-loop`, `gsd-quick`, `gsd-fast`), moving remaining 70+ skills to `~/.gemini/skills-library/` on-demand storage. Immediately eliminates ~15,000 to 20,000 input tokens from every turn.
- [x] **REQ-21**: **In-App Reasoning Effort Bar**: Build an interactive, in-app reasoning effort selector bar (`Low ⚡`, `Medium ⚖️`, `High 🧠`) allowing the user to seamlessly test and control thinking effort in real time.
- [x] **REQ-22**: **Effort Integration in Engine & TopBar**: Wire the selected effort state (`low`, `medium`, `high`) through `AgyViewModel`, display the active effort status badge in the UI, and pass `--effort <level>` directly to `libagy.so`. Default to `low` (instant) for testing.
- [x] **REQ-23**: **Verification & Release Build**: Ensure all unit tests pass (`./gradlew test`) and compile updated debug APK (`./gradlew assembleDebug`).
