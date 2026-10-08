# Phase Context: ChatGPT-Standard Mobile UI & UX Overhaul

## Problem & User Intent
The previous user interface used an aggressive cyberpunk/neon obsidian aesthetic with heavy glowing borders, diagnostic status matrix cards, and cluttered bottom sheets. The user explicitly disliked this look and requested a complete overhaul on par with ChatGPT mobile's user experience, fluidity, and UX flows, combined with authentic Antigravity design aesthetics.

## Mental Model & Core Abstractions
1. **Clean, Distraction-Free Canvas:**
   - Dark, sophisticated slate/graphite neutral tones (`#131314` background, `#212124` elevated surfaces, `#2D3139` user bubbles) reminiscent of Google Antigravity & modern ChatGPT mobile dark mode.
   - Refined Antigravity accents (delicate blue-violet `#4E80EE` / `#7C3AED` and cyan accents for active icons and states; no gaudy neon borders or matrix boxes).

2. **Navigation & Flow:**
   - **Top Navigation Bar:**
     - Left: Hamburger menu icon that slides open the navigation drawer.
     - Center: Centered model dropdown pill (`Gemini 2.5 Flash ▾`) providing seamless model selection via bottom sheet.
     - Right: New Chat icon (`SquarePen` / Edit icon) allowing instant conversation reset.
   - **Left Navigation Drawer (`ModalNavigationDrawer`):**
     - Full-height drawer sliding from left edge.
     - "+ New Chat" primary button at top.
     - Conversation history list chronologically grouped (Today, Yesterday, Previous 7 Days, Older).
     - Workspace / Project section with quick switcher and project creation.
     - Account / Auth status row at the bottom with sign in / sign out and settings access.

3. **Message Presentation & Fluid Stream:**
   - **User Message:** Crisp right-aligned rounded bubble (`RoundedCornerShape(18.dp)`) in elevated slate (`#2D3139`), white clean text.
   - **Assistant Message:** Directly rendered on the canvas (no heavy bounding box), clean typography, crisp markdown formatting, subtle Antigravity avatar at the start.
   - **Collapsible Agent Activities:** Sleek accordion pills for internal agent thoughts (`Thought for 3s ▾`) and tool runs (`Ran bash: git status ▾`) that keep the transcript readable and expand on demand.
   - **Approval & Diff Cards:** Modern, compact cards with native rounded borders and high-contrast actions.

4. **Floating Capsule Composer:**
   - Floating pill (`RoundedCornerShape(26.dp)`) elevated over the canvas.
   - Left: `+` Action button opening quick skills/tools menu (git status, run tests, review diffs).
   - Center: Auto-expanding multi-line text input ("Message Antigravity...").
   - Right: Circular action button:
     - Mic icon when input is empty.
     - Solid circular Up-Arrow Send button when text is entered.
     - Solid circular Stop button when agent is executing.

5. **Minimal Empty State (Welcome Screen):**
   - Centered subtle Antigravity emblem.
   - Warm greeting: "What can I help you build?"
   - 3-4 clean suggestion prompt pills (e.g., "Run git status & diffs", "Analyze project structure", "Search phone storage").

## Locked Architectural Decisions
- Architecture: Compose Material3 `ModalNavigationDrawer` wrapping `Scaffold` with custom TopAppBar and floating InputBar.
- Styling: Authentic Antigravity dark theme (`#131314`, `#1E1F22`, `#2B2D31`) with refined typography.
- Backwards Compatibility: Preserve all existing ViewModel hooks, speech recognition, foreground service bindings, approval flows, tool calls, and storage permissions.

## Verification & Backpressure Seams
- Unit Tests: `./gradlew test` passes all tests in `BootstrapManagerTest`, `AgyViewModelTest`, and UI component tests.
- Build & Assembly: `./gradlew assembleDebug` successfully produces `android/app/build/outputs/apk/debug/app-debug.apk`.
- Packaging: Export updated `android/agy-mobile-debug.apk`.
