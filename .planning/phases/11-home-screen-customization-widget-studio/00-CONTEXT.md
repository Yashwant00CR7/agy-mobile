# Phase Context: Home Screen Customization & Widget Studio

## Problem & User Intent
The user wants to customize their Android phone's home screen directly from `agy-mobile` — choosing which app icons to place, customizing icon styles, and creating/reshaping home screen widgets. While preserving their existing default system launcher (Pixel Launcher, Samsung One UI, etc.), the user wants an in-app "Home Screen & Widget Studio" where they can visually design shortcuts, reshape widgets, preview them, and pin them to their home screen with one tap (as well as via AI assistant prompts).

## Mental Model & Core Abstractions
- **Native Android ShortcutManager (`requestPinShortcut`)**:
  - Leverages Android 8.0+ (API 26+) `ShortcutManager` to programmatically pin custom shortcuts to the system launcher.
  - Dynamically renders custom icon shapes (Circle, Squircle, Rounded Square), colors, badges, and icons into a Canvas `Bitmap` to generate unique `Icon` objects.
  - Connects shortcuts to either:
    1. Direct Installed App Launch (with custom icon/label mask).
    2. Direct agy-mobile Actions (Open specific folder workspace, trigger voice dictation, new chat).
- **Native Android AppWidget Infrastructure (`requestPinAppWidget`)**:
  - Registers `AppWidgetProvider` implementations in `AndroidManifest.xml` with XML widget metadata (`res/xml/agy_prompt_widget_info.xml`, etc.).
  - Implements 3 high-impact widget types:
    1. **Quick AI Prompt Pill** (2x1 / 3x1): Instant floating prompt input with microphone & chat action.
    2. **Quick Actions Grid** (3x2 / 4x1): Quick toggle buttons for phone tools, apps, and AI triggers.
    3. **Workspace & Device Status Card** (4x2 / 3x2): Visual card displaying active workspace path, git branch, storage space, and quick launcher.
  - Interactive Reshaper & Preview: In-app modal lets users select widget layouts, preview them in Compose before pinning, and trigger `AppWidgetManager.requestPinAppWidget()`.
- **Dual Surface Access Points**:
  - *Navigation Drawer*: "Home Screen & Widgets" item in `ChatGptDrawer`.
  - *Composer '+' Action*: "Pin to Home Screen" quick action in `ChatGptComposer`.
  - *Assistant Prompt Trigger*: Responding to prompts like "Pin shortcut" or "Open Home Studio".

## Locked Architectural Decisions
- **Non-Invasive System Launcher Pinner**:
  - Do NOT replace the user's primary system launcher (`android.intent.category.HOME` is not required for the whole app). Instead, use `ShortcutManager.requestPinShortcut` and `AppWidgetManager.requestPinAppWidget` which work on ALL modern Android OEM launchers.
- **Dynamic Bitmap Icon Generator**:
  - `ShortcutIconGenerator`: Converts Compose/Android vector icons, emojis, or app icons into styled bitmaps with user-selected background shape (Circle, Squircle, Rounded Rect), border accents, and badges.
- **Compose Home Screen Studio Modal**:
  - `HomeScreenStudioModal`: Tabbed or segmented view between "App & Action Shortcuts" and "Home Widgets", featuring live preview, shape selector, color palette, and "Pin to Home Screen" button with confirmation feedback.

## Verification & Backpressure Seams
- Unit tests: `ShortcutIconGeneratorTest.kt`, `HomeScreenStudioStateTest.kt`.
- Test suite: `./gradlew test` (100% passing).
- Build: `./gradlew assembleDebug` (compiles `android/agy-mobile-debug.apk`).
