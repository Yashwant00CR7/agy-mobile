# Verification Report: Phase 14 - Compiler Background IPC & In-Chat Intent Auto-Trigger

## Goal
Fix on-device APK compilation issue reported in Bug 1 (where the agent claimed compilation is impossible or output raw JSON without running the on-device compiler), ensuring APK builds always run in the background, update the UI with progress, and notify the user with an interactive `ApkInstallCard` on success or structured diagnostics with an instant "Ask AI to Fix & Recompile" button on failure.

## Verification Checklist

### 1. In-Chat Intent Auto-Trigger
- [x] Detected compile intents (`compile the apk for me`, `build apk`, etc.) directly in `AgyViewModel.sendPrompt()`.
- [x] Compiles asynchronously in the background via `triggerBuildApk()` without blocking chat.
- [x] Does not intercept fix/error explanation prompts (allowing AI agent debugging when errors occur).

### 2. Synchronous Bidirectional CLI IPC Bridge
- [x] Upgraded `BootstrapManager.kt` `setupCompilerLinks()`: `bin/build-apk` sends requests to `$HOME/.compiler_ipc/req_*.json` and waits for `$RES_FILE`.
- [x] Added `startCompilerIpcWatcher()` in `AgyViewModel.kt` polling `.compiler_ipc` to process requests, invoke `BuildApkBridge.executeBuild()`, write the response file, and post the interactive `ChatMessage(apkResult = result)`.
- [x] Upgraded `handleEvent` for `event.name.endsWith("run_command")` to also execute `BuildApkBridge.executeBuild()` and write response files for waiting shell scripts.

### 3. Agent System Rules & Hallucination Prevention
- [x] `BootstrapManager.kt` persists `rules/on-device-compiler.md` and copies `GEMINI.md` to `homeDir/.gemini/GEMINI.md` and `homeDir/GEMINI.md`.
- [x] Explicit instructions forbid the agent from claiming JDK/d8/aapt2 are missing on mobile.

### 4. Interactive Install & Self-Healing Diagnostics Card
- [x] `ApkInstallCard.kt` updated to display stage name, error bullets, and a prominent "Ask AI to Fix & Recompile" button when `!result.success`.
- [x] `ChatScreen.kt` wired `onFixWithAi` callback to `ApkInstallCard`.
- [x] `MainActivity.kt` connects `onFixWithAi` to `viewModel.submitPrompt(fixPrompt, boundService)`.
- [x] `AgyViewModel.kt` sets `apkResult = result` on both success and error, ensuring error cards render properly.

### 5. Build & Tests
- [x] `./gradlew testDebugUnitTest`: Passed with 0 errors.
- [x] `./gradlew assembleDebug`: Successfully assembled `app-debug.apk` and copied to `agy-mobile-debug.apk` (125MB).
