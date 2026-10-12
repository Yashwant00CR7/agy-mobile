---
phase: 12-git-runtime-markdown-live-hud-remote-control
plan: 01
type: execute
tracer_first: true
---

# Plan 01: Git Runtime, Markdown Typography, Live Command HUD, Usage Credits & Remote Accessibility

## Objective
Deliver Milestone v2.2.0 addressing the user's reported screenshot issues (`aa9ab502a77fbf3b.png`, `ec454bc77e7b66a5.png`) and core mobile requirements:
1. Fix raw heading hashes with rich Markdown typography (H1/H2/H3, bullet points).
2. Bundle native standalone ARM64 Git runtime with dynamic account identity switching.
3. Build real-time streaming Live Command Monitoring HUD with auto-scroll and cancellation.
4. Implement Usage Credits & Token Quota Tracker in drawer and settings.
5. Implement native Android Accessibility Service for device automation with safety guardrails.
6. Build verified release APK and push directly to the `agy-mobile` Google Drive folder.

## Requirements Addressed
- REQ-57, REQ-58, REQ-59, REQ-60, REQ-61, REQ-62

## Execution Waves

### Wave 1: Tracer Slice (Markdown Typography & Git Runtime)
#### Task 1.1: Markdown Heading & Bullet Typography Engine (REQ-57)
- **Type:** tdd
- **Objective:** Overhaul Markdown rendering to parse headings (`#`, `##`, `###`), format bullet lists (`*`, `-`), strip raw `#` characters, and apply distinct typography hierarchies while preserving interactive FileProvider links.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/util/MarkdownLinkParser.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/screens/ChatScreen.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/util/MarkdownLinkParserTest.kt`
- **Acceptance Criteria:** Headings `#`, `##`, `###` render as styled H1, H2, H3 without `#` prefixes; bullet lists render as indented `• ` items; unit tests pass.

#### Task 1.2: Native Git Runtime & Dynamic Identity Management (REQ-58)
- **Type:** auto
- **Objective:** Bundle standalone ARM64 Git executable/wrapper in `BootstrapManager.kt` so commands like `git status`, `git log`, `git diff` execute in Bionic shell without 'inaccessible or not found'; auto-configure personal git identity (`Yashwant00CR7` / `10a19yashwant@gmail.com`) with dynamic switcher in Settings.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/ChatGptDrawer.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/core/BootstrapManagerTest.kt`
- **Acceptance Criteria:** `binDir/git` exists and is executable in environment PATH; `git status` command executes without 'inaccessible or not found'; git identity settings persist.

### Wave 2: Live Command Monitoring HUD & Usage Credits
#### Task 2.1: Interactive Live Command Monitoring HUD (REQ-59)
- **Type:** auto
- **Objective:** Enhance `ToolCallCard.kt`, `AgyProcessEngine.kt`, and `AgyViewModel.kt` to stream live stdout/stderr lines in real-time, show a live ticking execution timer (`Running 3.4s...`), auto-scroll in a bounded terminal box (~220dp max-height), and provide an immediate stop/cancel button to terminate stuck processes.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ToolCallCard.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/core/AgyProcessEngine.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
- **Acceptance Criteria:** Running commands display ticking duration timer, live streaming terminal with auto-scroll, and a working stop/cancel button.

#### Task 2.2: Usage Credits & Token Quota Tracker (REQ-60)
- **Type:** auto
- **Objective:** Implement `UsageCreditsManager.kt` to track prompt/completion tokens and message turns; display sleek "Credits & Token Usage" card in `ChatGptDrawer.kt` with live percentage gauge and tier badge; provide breakdown dialog in Settings.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/UsageCreditsManager.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/ChatGptDrawer.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
- **Acceptance Criteria:** Tokens tracked persistently per session and day; drawer displays visual quota gauge; settings dialog displays detailed breakdown.

### Wave 3: Remote Accessibility Service & Google Drive Deployment
#### Task 3.1: Remote Control Accessibility Service (REQ-61)
- **Type:** auto
- **Objective:** Create `AgyAccessibilityService.kt` registered in AndroidManifest with `accessibility_service_config.xml`; expose automation actions (`device_click`, `device_scroll`, `device_type_text`, `device_press_key`, `device_read_screen`) with safety guardrails and an in-app setup bridge.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/service/AgyAccessibilityService.kt`
  - `android/app/src/main/res/xml/accessibility_service_config.xml`
  - `android/app/src/main/AndroidManifest.xml`
  - `android/app/src/main/java/com/yashwant/agymobile/MainActivity.kt`
- **Acceptance Criteria:** Accessibility service declared in manifest; automation helper functions dispatch clicks and gestures; settings shows accessibility status card.

#### Task 3.2: Test Verification, Build & Google Drive Deployment (REQ-62)
- **Type:** auto
- **Objective:** Run full unit test suite, compile release debug APK, and upload the updated APK directly into the `agy-mobile` folder on Google Drive.
- **Target Files:**
  - `android/app/src/test/java/com/yashwant/agymobile/core/UsageCreditsManagerTest.kt`
  - `android/agy-mobile-debug.apk`
- **Acceptance Criteria:** Unit tests pass; APK compiles successfully; APK uploaded to Google Drive folder `agy-mobile` (ID: `1nvhmMmrcd8U6oOtMNNjUP1_uB1s0WZtT`).
