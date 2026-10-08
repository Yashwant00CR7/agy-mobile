# Phase 05: Model Conflict Resolution & Persistent Stream-JSON Daemon

## Plan Overview
- **Goal:** Completely eliminate the model selection & effort crash bug (e.g. Gemini 3.1 Pro + Med effort collision) and slash turn latency from 14s to ~1-2s by transitioning `AgyProcessEngine` to a persistent background `stream-json` daemon.
- **Requirements Addressed:** REQ-24, REQ-25, REQ-26, REQ-27.

---

## Waves of Execution

### Wave 1: Model & Effort Mapping Sanitization (REQ-24, REQ-26)
- **Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/core/AgyProcessEngine.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ModelSelectorBottomSheet.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
- **Actions:**
  - In `BootstrapManager.kt`:
    - Map models cleanly:
      - Dynamic models: `gemini-3.8-flash`, `gemini-3.7-flash`, `gemini-3.6-flash`.
      - Fixed-tier models: `gemini-3.1-pro-high`, `gemini-3.1-pro-low`, `claude-sonnet-4-6`, `claude-opus-4-6-thinking`, `gpt-oss-120b-medium`.
    - Provide helper `isFixedTierModel(slug: String): Boolean` and `fixedTierFor(slug: String): String?`.
  - In `AgyProcessEngine.kt`:
    - Only add `--effort <level>` if the model is dynamic and does NOT end with a fixed tier suffix (`-high`, `-medium`, `-low`), and is not a Claude model. If fixed tier, omit `--effort` CLI argument entirely to prevent CLI collision crashes.
  - In `ModelSelectorBottomSheet.kt`:
    - For fixed-tier models (Gemini 3.1 Pro High/Low, Claude, GPT-OSS), auto-lock effort to their inherent tier and disable incompatible effort segments with a clear "Locked" badge.
  - In `AgyViewModel.kt`:
    - Auto-dismiss error cards on model change and sync effort state.

### Wave 2: Persistent Stream-JSON Daemon in AgyProcessEngine (REQ-25)
- **Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/AgyProcessEngine.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
- **Actions:**
  - In `AgyProcessEngine.kt`:
    - Add persistent daemon session support using `--input-format stream-json --output-format stream-json`.
    - Cache active process while matching conversation, model, and workspace.
    - On subsequent turns in the same session: send `{"event":"user","message":{"content":prompt}}` over stdin.
    - Yields sub-second to 2s response streaming without language server or auth cold starts.
    - Support clean teardown on cancellation, session switch, or exit.
    - Provide safe fallback to single-turn `-p` if daemon fails.

### Wave 3: Testing & Build Verification (REQ-27)
- **Files:**
  - `android/app/src/test/java/com/yashwant/agymobile/core/BootstrapManagerTest.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/core/AgyProcessEngineTest.kt`
- **Actions:**
  - Verify model mapping unit tests pass.
  - Run `./gradlew test`.
  - Run `./gradlew assembleDebug` and update `android/agy-mobile-debug.apk`.
  - Create `VERIFICATION.md`.
