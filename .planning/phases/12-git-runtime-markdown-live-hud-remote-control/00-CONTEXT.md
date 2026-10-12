# Phase Context: 12-git-runtime-markdown-live-hud-remote-control

## Problem & User Intent
The user reported several critical friction points and feature requests in the mobile experience:
1. Markdown headings (e.g. `### 1. Temporary and Test Files...`) render with raw `#` hashes and flat body styling (`aa9ab502a77fbf3b.png`).
2. Running `git status` on the mobile device returned `git: inaccessible or not found` (`ec454bc77e7b66a5.png`).
3. Running commands lack live visual monitoring, streaming output, ticking duration timer, and immediate cancellation control ("Live command monitoring fix everything for me").
4. No visibility into API Usage Credits, tokens, and daily quotas in the drawer and top bar.
5. Inability to remotely automate phone actions via Android Accessibility Service.
6. The compiled APK must be deployed into the `agy-mobile` Google Drive folder.

## Mental Model & Core Abstractions
- **Markdown Typography Engine**: Structured blocks (`Heading(level, text)`, `BulletItem(level, text)`, `CodeBlock(lang, code)`, `Paragraph(annotatedString)`). Heading levels determine bold weights, font sizes (22sp, 18sp, 15sp), and visual rhythm.
- **Git Mobile Runtime**: Standalone ARM64 git binary bundled and extracted via `BootstrapManager.kt` into `binDir` and PATH, with dynamic git identity profiles (defaulting to personal account `Yashwant00CR7` / `10a19yashwant@gmail.com`).
- **Live Command Monitoring HUD**: An active `ToolCallCard` that streams stdout/stderr lines in real-time, displays a pulsing running indicator and live ticking timer, bounded within a ~220dp terminal box with auto-scroll and a cancel button that triggers process termination.
- **Usage Credits & Quota Tracker**: `UsageCreditsManager` maintaining persistent token counts (prompt, completion, total), daily reset window, and quota percentage, presented as an account card in `ChatGptDrawer` and modal breakdown in Settings.
- **Remote Control Accessibility**: `AgyAccessibilityService` extending Android's `AccessibilityService` to perform automated node inspection (`findAccessibilityNodeInfosByText`), gesture clicks (`dispatchGesture`), text input, and global actions (`GLOBAL_ACTION_HOME`, `GLOBAL_ACTION_BACK`, `GLOBAL_ACTION_RECENTS`), protected by safety guardrails.

## Locked Architectural Decisions
- **Decision 1 (Git)**: Bundle a standalone ARM64 `git` executable / wrapper directly into the APK environment with zero external downloads required, and automatically configure git identity for `Yashwant00CR7` / `10a19yashwant@gmail.com` with a dynamic switcher in Settings.
- **Decision 2 (Markdown)**: Full typography hierarchy (H1: 22sp bold, H2: 18sp bold, H3: 15sp bold, bullet points `• `) while maintaining existing FileProvider clickable link annotations and code syntax blocks.
- **Decision 3 (Command HUD)**: Bounded terminal viewport (~220dp max-height) with live line auto-scrolling, elapsed timer, line counter, and one-tap process cancellation.
- **Decision 4 (Usage Credits)**: Local token counter and tier quota tracker with daily window, displayed in the drawer footer and detailed in Settings.
- **Decision 5 (Accessibility)**: Native Android `AgyAccessibilityService` with autonomous execution for standard navigation and approval prompts for sensitive actions, plus an in-app Settings shortcut card.
- **Decision 6 (Drive)**: Google Drive folder `agy-mobile` (ID: `1nvhmMmrcd8U6oOtMNNjUP1_uB1s0WZtT`) receives the final compiled APK.

## Verification & Backpressure Seams
- Unit Tests: `./gradlew test`
- Build: `./gradlew assembleDebug`
- APK Output: `android/app/build/outputs/apk/debug/app-debug.apk` and `android/agy-mobile-debug.apk`
- Drive Upload: Verify upload to `agy-mobile` via Google Workspace MCP tool.
