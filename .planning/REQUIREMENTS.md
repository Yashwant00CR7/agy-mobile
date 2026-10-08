# Requirements: agy-mobile

## Completed Milestones
- [x] **REQ-01**: Bundle native ARM64 bash binary with execute permissions.
- [x] **REQ-02**: Implement Terminal / Shell execution helper.
- [x] **REQ-03**: Support device-wide storage access and permission flows.
- [x] **REQ-04**: Bundle GSD Core skills in assets and extract to runtime directory.
- [x] **REQ-05**: Inject shell and GSD environment variables to agent process.
- [x] **REQ-06**: Verify shell execution and GSD workflow end-to-end.
- [x] **REQ-07**: Update color palette and theme to authentic Antigravity Dark (`#131314` canvas, `#1E1F22` surfaces, `#2B2D31` bubbles, refined blue/violet accents).
- [x] **REQ-08**: Implement full ChatGPT-style left sliding `ModalNavigationDrawer` containing "+ New Chat" button, grouped conversation history (Today, Yesterday, Older), workspace switcher, and user account status bar.
- [x] **REQ-09**: Implement ChatGPT-style top bar with hamburger menu icon, centered interactive model dropdown pill, and new chat button.
- [x] **REQ-10**: Overhaul chat message layout to fluid ChatGPT canvas: right-aligned clean user bubbles, seamless full-width assistant messages, and collapsible accordion pills for thoughts and tool calls.
- [x] **REQ-11**: Redesign bottom composer as a floating capsule pill (`RoundedCornerShape(26.dp)`) with `+` action button, auto-expanding text input, and circular send/stop button.
- [x] **REQ-12**: Redesign empty state (welcome screen) to a clean, minimal ChatGPT-style layout with centered Antigravity logo, warm greeting, and clean suggestion prompt pills.
- [x] **REQ-13**: Maintain full integration with existing `AgyViewModel`, `AgyAgentService`, speech recognition, file diffs, tool approval flows, and storage permission checks; pass unit tests and `./gradlew assembleDebug`.

---

## Milestone v1.3.0: Latency Optimization, Folder-Wise Context Scoping, Session Persistence & Keyboard IME Fix

- [x] **REQ-14**: **Turn Latency & Folder-Wise Context Scoping**: Restrict agent directory scanning and context ingestion to the explicit active workspace folder (folder-wise scoping) to prevent massive whole-device latency. Default model to fast medium-effort (`Gemini 3.8 Flash (Medium Effort)`), avoiding unneeded CoT delays.
- [x] **REQ-15**: **Instant Visual Thinking State**: Display an animated, pulsing "Antigravity is thinking..." status indicator in `ChatScreen` immediately upon sending a prompt when `isRunning == true` before stream chunks arrive, ensuring zero perceived lag.
- [x] **REQ-16**: **Conversation Session Persistence**: Implement `ConversationRepository` in `com.yashwant.agymobile.core` persisting chats to local storage (`filesDir/conversations/<id>.json`). Save complete message lists, timestamps, titles, and workspace associations automatically after every turn.
- [x] **REQ-17**: **Session Restoration & App Startup**: Launch app with a fresh clean "New Chat" on start (clean slate). Load full conversation message history and switch workspace folder when tapping any past session in `ChatGptDrawer`.
- [x] **REQ-18**: **Keyboard IME Padding & Auto-Scroll**: Apply `Modifier.imePadding()` to `ChatGptComposer` in `ChatScreen` so the input composer pins directly above the soft keyboard without obscuring the dialog box. Automatically scroll `LazyColumn` to the latest message whenever the keyboard appears.
- [x] **REQ-19**: **End-to-End Build & Verification**: Pass all unit tests (`./gradlew test`) and compile release-ready debug APK via `./gradlew assembleDebug`.
