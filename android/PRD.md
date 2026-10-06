# PRD: agy-mobile-native — Standalone Android App for Google Antigravity

## Problem & User Intent
The user wants a 100% standalone native Android mobile application for Google Antigravity (`agy`) that runs directly on their Android phone without requiring Termux to be installed. The app must deliver an ultra-premium, high-polish visual experience ("sexier than you") using native Jetpack Compose, featuring Cyber-Minimalist Obsidian & Neon aesthetics, smooth 120Hz gesture navigation, interactive tool approval cards, syntax-highlighted diffs, voice dictation, and an interactive Foreground Service keeping the agent alive in the background.

## Mental Model & Core Abstractions
- **Self-Contained Native Android Architecture**:
  - The `agy` ARM64 binary and portable tools are bundled into the APK (`libagy.so` and `assets/bin`), extracted to `context.filesDir/bin/`, and executed with full Linux permissions inside the app's private sandbox.
- **Agent Process Coroutine Engine**:
  - `AgyProcessEngine` launches `libagy.so` using Kotlin Coroutines `ProcessBuilder`, communicating via `--input-format stream-json --output-format stream-json`.
  - Dispatches NDJSON events into reactive `StateFlow<AgyUiState>` consumed by Compose screens.
- **Interactive Foreground Service**:
  - `AgyAgentService` holds a `WakeLock` (`PARTIAL_WAKE_LOCK`) so the phone's CPU never sleeps during agent tasks.
  - Posts persistent notifications with direct notification shade actions (**[Approve]**, **[Cancel]**).
- **Cyber-Minimalist Obsidian & Neon Aesthetic**:
  - True OLED black (`#080C14`), Electric Cyan (`#00E5FF`), and Antigravity Violet (`#8B5CF6`).
  - Frosted glass cards, glowing pulsing borders during agent reasoning, and tactile haptic feedback.

## Architectural Decisions & Constraints
- **Subdirectory Location**: Built inside `agy-mobile/android/`.
- **Package Name**: `com.yashwant.agymobile`
- **Minimum SDK**: 26 (Android 8.0 Oreo), Target SDK: 35 (Android 15).
- **UI Framework**: 100% Jetpack Compose with Material 3.
- **Git & Identity Rules**:
  - Repo-local identity to `Yashwant00CR7` / `10a19yashwant@gmail.com`.
  - Push remote `git@github-personal:Yashwant00CR7/agy-mobile.git`. Never touch `--global` git config.

## Verification & Backpressure
Command to run after each task:
- Test & Compile: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew test assembleDebug`
- Final APK target: `android/agy-mobile-debug.apk`

---

## Discrete Tasks

### Task 1: Android Project Scaffolding & Gradle Configuration
- **Objective:** Initialize `android/settings.gradle.kts`, `android/build.gradle.kts`, `android/app/build.gradle.kts`, `android/local.properties`, and `android/gradle.properties`.
- **Acceptance Criteria:** `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew tasks` runs cleanly and succeeds.
- **Target Files:** `android/settings.gradle.kts`, `android/build.gradle.kts`, `android/app/build.gradle.kts`, `android/local.properties`, `android/gradle.properties`.

### Task 2: Binary Packaging & Environment Bootstrapper (`BootstrapManager.kt`)
- **Objective:** Implement `BootstrapManager.kt` to extract the `agy` ARM64 binary and portable tools to `context.filesDir/bin`, set executable permissions (`setExecutable(true)`), and configure runtime environment variables (`PATH`, `HOME`, `TMPDIR`).
- **Acceptance Criteria:** Unit tests in `BootstrapManagerTest.kt` pass, verifying extraction and file permissions.
- **Target Files:** `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`, `android/app/src/test/java/com/yashwant/agymobile/core/BootstrapManagerTest.kt`.

### Task 3: Native Agy Coroutine Process Engine (`AgyProcessEngine.kt`)
- **Objective:** Implement `AgyProcessEngine.kt` to launch `agy` with `--input-format stream-json --output-format stream-json`, stream NDJSON events via Kotlin `StateFlow<AgyEvent>`, parse tool calls, thoughts, texts, diffs, and manage autonomous vs supervised permission gates.
- **Acceptance Criteria:** Unit tests in `AgyProcessEngineTest.kt` verify event parsing and approval resolution.
- **Target Files:** `android/app/src/main/java/com/yashwant/agymobile/core/AgyProcessEngine.kt`, `android/app/src/main/java/com/yashwant/agymobile/model/AgyModels.kt`, `android/app/src/test/java/com/yashwant/agymobile/core/AgyProcessEngineTest.kt`.

### Task 4: Android Foreground Service & Notification Shade Actions (`AgyAgentService.kt`)
- **Objective:** Implement `AgyAgentService.kt` with `WakeLock`, persistent status notification (`NotificationCompat.Builder`), and interactive notification actions (**[Approve]**, **[Cancel]**) using BroadcastReceivers / PendingIntents.
- **Acceptance Criteria:** Unit tests in `AgyAgentServiceTest.kt` verify service lifecycle, intent actions, and notification channel initialization.
- **Target Files:** `android/app/src/main/java/com/yashwant/agymobile/service/AgyAgentService.kt`, `android/app/src/test/java/com/yashwant/agymobile/service/AgyAgentServiceTest.kt`.

### Task 5: Cyber-Minimalist Design System & Theme (`Theme.kt`, `Color.kt`)
- **Objective:** Build the Obsidian OLED true black (`#080C14`), Electric Cyan (`#00E5FF`), and Antigravity Violet (`#8B5CF6`) Material 3 design system, typography, card shapes, glassmorphism surface elevations, and glowing status borders.
- **Acceptance Criteria:** Theme and color resources compile and pass verification tests in `ThemeTest.kt`.
- **Target Files:** `android/app/src/main/java/com/yashwant/agymobile/ui/theme/*`, `android/app/src/test/java/com/yashwant/agymobile/ui/ThemeTest.kt`.

### Task 6: High-Polish Jetpack Compose Components (Chat, Cards, Approvals, Diffs)
- **Objective:** Build Compose UI components:
  - `ChatScreen.kt`: Smooth 120Hz message list with spring scrolling.
  - `ThoughtCard.kt`: Glowing pulsing border during reasoning.
  - `ToolCallCard.kt`: Animated expandable card with arguments & output preview.
  - `ApprovalCard.kt`: Tactile [Approve] / [Deny] buttons with haptic feedback.
  - `DiffCard.kt`: Monospaced diff viewer with syntax highlighted added/removed lines.
  - `InputBar.kt`: Animated mic dictation button, quick chips row, auto-growing text field.
- **Acceptance Criteria:** Compose components compile and pass UI state model tests.
- **Target Files:** `android/app/src/main/java/com/yashwant/agymobile/ui/components/*`, `android/app/src/main/java/com/yashwant/agymobile/ui/screens/ChatScreen.kt`.

### Task 7: Workspace Browser & Conversation History Bottom Sheet
- **Objective:** Implement `WorkspaceDrawer.kt` and `HistoryDrawer.kt`:
  - Browse app-internal projects or external storage directories.
  - Inspect git branch and dirty status.
  - Load and resume past conversation transcripts from internal brain storage.
- **Acceptance Criteria:** Unit tests in `WorkspaceManagerTest.kt` pass for directory navigation and git branch detection.
- **Target Files:** `android/app/src/main/java/com/yashwant/agymobile/core/WorkspaceManager.kt`, `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/*`, `android/app/src/test/java/com/yashwant/agymobile/core/WorkspaceManagerTest.kt`.

### Task 8: Main Activity Integration, Full Test Suite & APK Assembly
- **Objective:** Connect `MainActivity.kt`, ViewModel, and Service; run the full test suite (`./gradlew test`), compile the debug APK (`./gradlew assembleDebug`), and copy the final standalone APK to `android/agy-mobile-debug.apk`.
- **Acceptance Criteria:** `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew test assembleDebug` succeeds with 100% pass rate, producing `agy-mobile-debug.apk`.
- **Target Files:** `android/app/src/main/java/com/yashwant/agymobile/MainActivity.kt`, `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`, `android/agy-mobile-debug.apk`.

---

## Feature: In-Phone Native Google Authentication Flow

### Problem & Invariant
`libagy.so` requires Google OAuth credentials in `$HOME/.gemini/oauth_creds.json`. When unauthenticated, it generates a Google OAuth consent URL, prints it to stderr, and waits on stdin for the authorization code before timing out after 60s with `Error: authentication required. Run 'libagy.so' to log in`.
The app must provide a 100% self-contained in-phone login flow via Chrome Custom Tabs, stdin code piping, and credential persistence in `$HOME/.gemini/`.

### Task 9: Authentication Core Engine & Subprocess Stream Handler (`AuthManager.kt`, `AgyProcessEngine.kt`)
- **Objective:**
  - Enhance `BootstrapManager.kt` with credential status checks: `isLoggedIn()`, `getAuthAccount()`, `getOauthCredsFile()`.
  - Implement `AuthManager.kt`:
    - Launch `libagy.so` in auth probe/login mode and stream stderr in real-time.
    - Extract Google OAuth consent URLs using regex (`https://accounts.google.com/o/oauth2/auth[^\s]+`).
    - Pipe user authorization code + `\n` to stdin of the active process.
    - Write / import credential JSON directly into `$HOME/.gemini/oauth_creds.json` and `$HOME/.gemini/settings.json`.
    - Provide `signOut()` to safely revoke/delete local credentials.
  - Define `AuthState` in `AgyModels.kt`.
- **Acceptance Criteria:** Unit tests in `AuthManagerTest.kt` pass with 100% success rate, validating regex extraction, token persistence, and auth state transitions.
- **Target Files:** `android/app/src/main/java/com/yashwant/agymobile/core/AuthManager.kt`, `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`, `android/app/src/main/java/com/yashwant/agymobile/core/AgyProcessEngine.kt`, `android/app/src/main/java/com/yashwant/agymobile/model/AgyModels.kt`, `android/app/src/test/java/com/yashwant/agymobile/core/AuthManagerTest.kt`.

### Task 10: Chrome Custom Tabs Integration & Browser Helper (`BrowserHelper.kt`)
- **Objective:**
  - Add `androidx.browser:browser:1.8.0` to `android/app/build.gradle.kts`.
  - Create `BrowserHelper.kt` to launch Google OAuth consent URLs in Chrome Custom Tabs styled with Cyber-Obsidian OLED theme colors, falling back to system browser if unavailable.
- **Acceptance Criteria:** `BrowserHelperTest.kt` verifies Intent configuration, URL routing, and flags.
- **Target Files:** `android/app/build.gradle.kts`, `android/app/src/main/java/com/yashwant/agymobile/ui/util/BrowserHelper.kt`, `android/app/src/test/java/com/yashwant/agymobile/ui/BrowserHelperTest.kt`.

### Task 11: Cyber-Obsidian Auth UI Components (`AuthBanner.kt`, `AuthBottomSheet.kt`)
- **Objective:**
  - Implement `AuthBanner.kt`: Sleek neon top banner when unauthenticated with a prominent [Sign In] CTA button.
  - Implement `AuthBottomSheet.kt`:
    - Step 1: [Open Google Sign-In] button launching Chrome Custom Tabs.
    - Step 2: Authorization code field with auto-clipboard detection and [Paste Code] chip.
    - Step 3: [Complete Sign-In] action button with loading spinner during token exchange.
    - Step 4: Expandable fallback drawer for direct `oauth_creds.json` import/paste.
- **Acceptance Criteria:** Compose components compile cleanly and unit tests in `AuthComponentTest.kt` pass.
- **Target Files:** `android/app/src/main/java/com/yashwant/agymobile/ui/components/AuthBanner.kt`, `android/app/src/main/java/com/yashwant/agymobile/ui/components/AuthBottomSheet.kt`, `android/app/src/test/java/com/yashwant/agymobile/ui/AuthComponentTest.kt`.

### Task 12: ViewModel & Chat Screen Integration, Backpressure Verification & APK Assembly
- **Objective:**
  - Connect `AuthManager` to `AgyViewModel.kt` and `MainActivity.kt`.
  - Check auth state on startup; show `AuthBanner` when logged out; auto-open `AuthBottomSheet` if a prompt is sent while unauthenticated.
  - Add Account display and Sign Out action in `WorkspaceBottomSheet.kt`.
  - Enforce backpressure: run full test suite and assemble updated debug APK.
- **Acceptance Criteria:** `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew test assembleDebug` succeeds with 100% pass rate, producing `android/agy-mobile-debug.apk`.
- **Target Files:** `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`, `android/app/src/main/java/com/yashwant/agymobile/MainActivity.kt`, `android/app/src/main/java/com/yashwant/agymobile/ui/screens/ChatScreen.kt`, `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/WorkspaceBottomSheet.kt`, `android/agy-mobile-debug.apk`.

