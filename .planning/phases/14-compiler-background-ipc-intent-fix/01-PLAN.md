---
phase: 14-compiler-background-ipc-intent-fix
plan: 01
type: execute
tracer_first: true
---

# Plan: Fix Compiler Awareness, In-Chat Intent Auto-Trigger, and Synchronous CLI IPC Bridge

## Objective
Fix the on-device compiler bug where the AI agent claims it cannot compile APKs on mobile, fails to execute the build, and leaves the user without an installable APK. Implement dual in-chat intent trigger, background asynchronous compilation with real-time notifications, interactive error cards with self-healing, synchronous CLI `build-apk` IPC bridge, and upload the updated APK to Google Drive.

## Requirements Addressed
- **REQ-FIX-01**: Agent System Instruction — Ensure `BootstrapManager` injects `on-device-compiler.md` and updates global `GEMINI.md` on every bootstrap so the LLM explicitly knows it can compile APKs on-device.
- **REQ-FIX-02**: In-Chat Intent Auto-Trigger — Intercept user messages like "compile the apk for me", "build apk", "compile apk" in `AgyViewModel.sendPrompt` to execute background compilation and display live status pills.
- **REQ-FIX-03**: Background Asynchronous Execution — Compilation runs strictly on `Dispatchers.IO` in the background, keeping the chat interactive, notifying the user on completion with `ApkInstallCard`, or presenting structured error cards if it cannot be built.
- **REQ-FIX-04**: Robust ToolCall & ToolResult Handling — Support `default_api:run_command` in `AgyViewModel.kt`, fix `ToolResult` handler to never overwrite or erase `apkResult`.
- **REQ-FIX-05**: Interactive Error Card with Self-Healing — Display file, line, and message with an instant "Ask AI to Fix & Recompile" action.
- **REQ-FIX-06**: Verify Unit Tests, Assemble Debug APK, and Upload to Google Drive folder `agy-mobile`.

## Execution Waves

### Wave 1: System Rules & Model Awareness (Tracer Slice)
- **Task 1.1**: Update `BootstrapManager.kt` to write `on-device-compiler.md` into `geminiDir/rules/` and link to `homeDir/.gemini/GEMINI.md` on every startup.

### Wave 2: Intent Detection, Chat Quick Action, & Tool Event Handling
- **Task 2.1**: Update `AgyViewModel.kt` to detect compilation intents in `sendPrompt` and trigger background compilation.
- **Task 2.2**: Add `default_api:run_command` support and fix `ToolResult` preserving `apkResult`.
- **Task 2.3**: Update `ChatBottomBar.kt` with a "🔨 Build APK" quick action chip.

### Wave 3: Interactive Error Card & Bidirectional IPC
- **Task 3.1**: Enhance `ApkInstallCard.kt` to render an Interactive Error Card with file/line diagnostics and a 1-tap "Ask AI to Fix & Recompile" self-healing action.
- **Task 3.2**: Upgrade `build-apk` script in `BootstrapManager.kt` to write a request file and wait for `result.json` so CLI executions return real status.

### Wave 4: Testing, Assembly & Google Drive Upload
- **Task 4.1**: Run `./gradlew testDebugUnitTest` and assemble APK via `./gradlew assembleDebug`.
- **Task 4.2**: Upload the updated `agy-mobile-debug.apk` to Google Drive folder `agy-mobile` (`1nvhmMmrcd8U6oOtMNNjUP1_uB1s0WZtT`).
