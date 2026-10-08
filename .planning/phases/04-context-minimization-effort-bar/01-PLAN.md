# Phase 04: Context Minimization & In-App Reasoning Effort Control

## Plan Overview
- **Goal:** Slash prompt context size by ~15,000+ tokens through a Lean Skills Profile and build an interactive in-app reasoning effort bar (`Low ⚡`, `Medium ⚖️`, `High 🧠`) allowing the user to test and adjust thinking effort dynamically.
- **Requirements:** REQ-20, REQ-21, REQ-22, REQ-23.

---

## Waves of Execution

### Wave 1: Lean Skills Profile Implementation
- **Files:** `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`
- **Actions:**
  - Define essential skills set: `listOf("ralph-loop", "gsd-quick", "gsd-fast")`.
  - In `extractBundledAssets`: extract only essential skills into `~/.gemini/config/skills/`, and extract all other skills into `~/.gemini/skills-library/`.
  - In `initializeDirectories()`: if `~/.gemini/config/skills/` contains old bloat skills from prior extractions, prune them so only the lean set remains in active config.

### Wave 2: In-App Reasoning Effort Bar & ViewModel Wiring
- **Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ModelSelectorBottomSheet.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ChatGptTopBar.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/core/AgyProcessEngine.kt`
- **Actions:**
  - In `AgyViewModel`: add `selectedEffort: StateFlow<String>` (values: `"low"`, `"medium"`, `"high"`), defaulting to `"low"` for instant responses.
  - In `ModelSelectorBottomSheet.kt`: add an interactive segmented Effort Control Bar:
    - `⚡ Low (Instant)`: `--effort low`, zero thinking delay.
    - `⚖️ Medium (Balanced)`: `--effort medium`, balanced reasoning.
    - `🧠 High (Deep)`: `--effort high`, deep chain-of-thought.
  - In `ChatGptTopBar.kt`: display the active effort badge on the model selector chip (e.g., `Gemini 3.8 Flash • ⚡ Instant`).
  - In `AgyProcessEngine.kt`: pass `request.effort` directly as `--effort <level>`.

### Wave 3: Testing & Build Verification
- **Files:**
  - `android/app/src/test/java/com/yashwant/agymobile/core/BootstrapManagerTest.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/ui/ChatComponentTest.kt`
- **Actions:**
  - Run `./gradlew test` and verify all tests pass.
  - Run `./gradlew assembleDebug` and refresh `android/agy-mobile-debug.apk`.
  - Write `VERIFICATION.md` and commit cleanly.
