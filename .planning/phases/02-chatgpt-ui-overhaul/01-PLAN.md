---
phase: 02-chatgpt-ui-overhaul
plan: 01
type: execute
tracer_first: true
---

# Plan: ChatGPT-Standard Mobile UI & UX Architecture Overhaul

## Objective
Completely refactor and restructure the mobile UI from scratch to match ChatGPT mobile's fluid UX, navigation drawer, floating composer, and clean message canvas, styled with authentic Antigravity design.

## Requirements Addressed
- REQ-07: Authentic Antigravity Dark palette and theme
- REQ-08: Left sliding `ModalNavigationDrawer` with grouped history and workspaces
- REQ-09: Centered model dropdown top bar with new chat button
- REQ-10: Fluid ChatGPT message canvas with collapsible thought/tool pills
- REQ-11: Floating capsule composer with '+' action sheet, auto-expanding text, and dynamic circular send/mic button
- REQ-12: Minimal ChatGPT-style welcome screen with clean suggestion pills
- REQ-13: Full integration with ViewModel, service bindings, unit tests, and APK build

## Execution Waves

### Wave 1: Design System & Palette Foundation (Tracer Slice)
#### Task 1.1: Refactor Color and Theme
- **Type:** auto
- **Objective:** Replace neon obsidian colors with authentic Antigravity Dark palette (`#131314` canvas, `#1E1F22` surface, `#2B2D31` bubbles, refined blue/violet accents).
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/theme/Color.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/theme/Theme.kt`
- **Acceptance Criteria:** Clean dark theme variables defined without neon glows or gaudy cyberpunk tokens.
- **Verification Command:** `./gradlew test`

#### Task 1.2: Build ChatGPT TopBar with Centered Model Dropdown
- **Type:** auto
- **Objective:** Create `ChatGptTopBar` with hamburger icon, centered interactive model pill (`Gemini 2.5 Flash ▾`), and new chat icon.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ChatGptTopBar.kt`
- **Acceptance Criteria:** Component renders centered model pill, menu button, and new chat action with clean ChatGPT styling.
- **Verification Command:** `./gradlew test`

### Wave 2: Left Navigation Drawer & History
#### Task 2.1: Build Left Navigation Drawer
- **Type:** auto
- **Objective:** Create `ChatGptDrawer` using `ModalDrawerSheet` with "+ New Chat" button, chronological history grouping (Today, Yesterday, Previous 7 Days, Older), workspace switcher, and user account status.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/ChatGptDrawer.kt`
- **Acceptance Criteria:** Full-height drawer renders all sections and dispatches callbacks for conversation selection, new chat, and workspace changes.
- **Verification Command:** `./gradlew test`

#### Task 2.2: Integrate ModalNavigationDrawer in MainActivity
- **Type:** auto
- **Objective:** Wrap `ChatScreen` in `ModalNavigationDrawer` in `MainActivity.kt` with smooth swipe gesture and open/close state.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/MainActivity.kt`
- **Acceptance Criteria:** Swiping from left or tapping hamburger menu opens the drawer smoothly.
- **Verification Command:** `./gradlew test`

### Wave 3: Message Flow & Fluid Canvas
#### Task 3.1: Collapsible Thought & Tool Accordion Pills
- **Type:** auto
- **Objective:** Restyle `ThoughtCard.kt` and `ToolCallCard.kt` into sleek collapsible accordion pills (e.g. `Thought for 2s ▾`, `Ran bash: git status ▾`) with compact headers and expandable details.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ThoughtCard.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ToolCallCard.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ApprovalCard.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/DiffCard.kt`
- **Acceptance Criteria:** Thought and tool cards display as compact pills that expand/collapse on tap with subtle styling.
- **Verification Command:** `./gradlew test`

#### Task 3.2: Fluid ChatGPT Chat Screen
- **Type:** auto
- **Objective:** Overhaul `ChatScreen.kt` message list: right-aligned clean user bubbles (`#2B2D31`), seamless full-width assistant messages directly on canvas, integrated storage banner, and new top bar.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/screens/ChatScreen.kt`
- **Acceptance Criteria:** Chat messages render with clean ChatGPT fluidity, avatars, and no boxy border clutter.
- **Verification Command:** `./gradlew test`

### Wave 4: Floating Capsule Composer & Minimal Welcome Screen
#### Task 4.1: Floating Capsule Composer (`ChatGptComposer.kt`)
- **Type:** auto
- **Objective:** Build floating rounded capsule input bar (`26.dp` radius) with `+` action button (quick tools menu), auto-expanding text field, and circular button flipping between Mic (empty), Up-arrow Send (typed), and Stop (running).
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ChatGptComposer.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/InputBar.kt`
- **Acceptance Criteria:** Capsule floats cleanly over canvas; dynamic send/mic/stop button functions accurately with haptics.
- **Verification Command:** `./gradlew test`

#### Task 4.2: Minimal Welcome Screen (`ChatGptWelcome.kt`)
- **Type:** auto
- **Objective:** Build minimal empty state with centered Antigravity logo, warm greeting, and 4 clean suggestion prompt pills.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/WelcomeHero.kt`
- **Acceptance Criteria:** Clean, clutter-free empty state matching ChatGPT mobile aesthetic.
- **Verification Command:** `./gradlew test`

### Wave 5: Integration, Verification & APK Packaging
#### Task 5.1: Full Verification & Assembly
- **Type:** auto
- **Objective:** Run full test suite, assemble debug APK, verify APK integrity, and write `VERIFICATION.md`.
- **Target Files:**
  - `android/agy-mobile-debug.apk`
  - `.planning/phases/02-chatgpt-ui-overhaul/VERIFICATION.md`
- **Acceptance Criteria:** `./gradlew test` passes all tests, `./gradlew assembleDebug` builds successfully, fresh APK exported to root.
- **Verification Command:** `./gradlew test && ./gradlew assembleDebug`
