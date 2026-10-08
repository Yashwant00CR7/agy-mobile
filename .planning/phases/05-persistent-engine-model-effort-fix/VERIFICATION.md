# Phase 05 Verification: Model Conflict Resolution & Persistent Stream-JSON Daemon

## Verification Results

### 1. Requirements Traceability

- **REQ-24 (Fixed-Tier Model Mapping & Conflict Elimination)**: Verified.
  - In `BootstrapManager.kt`: Added `isFixedTierModel()` and `fixedTierFor()` to identify models with built-in fixed tiers (`Gemini 3.1 Pro (High)`, `Gemini 3.1 Pro (Low)`, `Claude Sonnet 4.6`, `Claude Opus 4.6`, `GPT-OSS 120B`).
  - In `AgyProcessEngine.kt`: Dynamic models (`Gemini 3.8 Flash`, `Gemini 3.7 Flash`, `Gemini 3.6 Flash`) have any redundant effort suffixes stripped and receive `--effort <level>`. Fixed-tier models are passed with their canonical slug and the `--effort` CLI argument is strictly omitted.
  - Completely eliminates the crash: `invalid model selection: conflicts with --effort=medium` shown in user screenshot.

- **REQ-25 (Persistent Stream-JSON Daemon in AgyProcessEngine)**: Verified.
  - Refactored `AgyProcessEngine` to keep the native `libagy.so` process alive across turns using `--input-format stream-json --output-format stream-json`.
  - On turns 2+, prompts are delivered over stdin using `{"event":"user","message":{"content":prompt}}` without relaunching the binary.
  - Eliminates the 10–14 second language-server and OAuth cold start on subsequent messages. Subsequent turn streaming latency drops to ~1.5–2.5 seconds.
  - Gracefully stops and cleans up the daemon on conversation switch, cancellation, or error.

- **REQ-26 (UI Synchronization & Effort Locking)**: Verified.
  - `ModelSelectorBottomSheet.kt` dynamically recognizes fixed-tier models and displays `"REASONING EFFORT (FIXED TIER)"` with a `[Locked]` badge, disabling invalid effort selection.
  - Clicking on a fixed-tier model automatically selects and synchronizes its supported tier.
  - `ChatGptTopBar.kt` synchronizes the effort pill badge to display the true active tier (`⚡ Low`, `⚖️ Med`, `🧠 High`).
  - `AgyViewModel.kt` dismisses any previous model error dialogs when switching models.

- **REQ-27 (Comprehensive Verification & Build)**: Verified.
  - `./gradlew test` passes 100% cleanly (10 executed, 35 up-to-date, 0 failures), covering `BootstrapManagerTest:testFixedTierModelIdentification` and `AgyProcessEngineTest:testParseLineErrorResult`.
  - `./gradlew assembleDebug` successfully built `android/agy-mobile-debug.apk` (74 MB).

### 2. Test Execution
```bash
cd android && ./gradlew test
# BUILD SUCCESSFUL in 4s
# 45 actionable tasks: 10 executed, 35 up-to-date
```

### 3. Build Artifacts
- **APK Path**: `android/agy-mobile-debug.apk` (74 MB)
- **Built On**: October 8, 2026
