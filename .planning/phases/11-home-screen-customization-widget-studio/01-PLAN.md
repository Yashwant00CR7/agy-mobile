---
phase: 11-home-screen-customization-widget-studio
plan: 01
type: execute
tracer_first: true
---

# Plan: Home Screen Customization & Widget Studio

## Objective
Implement an in-app Home Screen & Widget Studio empowering users to visually design and pin custom styled app & action shortcuts, as well as configure, reshape, preview, and pin 3 native Android widgets (Quick AI Prompt Pill, Quick Actions Grid, Workspace & Device Status Card) directly to their existing phone launcher via `ShortcutManager` and `AppWidgetManager`.

## Requirements Addressed
- REQ-52: Dynamic Shortcut Creator & Bitmap Icon Generator
- REQ-53: Native AppWidget Architecture & XML Providers
- REQ-54: In-App Home Screen Studio & Widget Reshaper UI
- REQ-55: Dual Entry Points & Assistant Integration
- REQ-56: Test Suite, Code Review & Release APK Build

## Execution Waves

### Wave 1: Tracer Slice (Dynamic Shortcut Creator & Icon Generator)
- **Type:** tdd
- **Objective:**
  - Build `ShortcutIconGenerator.kt`: Renders customizable icon bitmaps (shapes: Circle, Squircle, Rounded Square; background colors, borders, icon/emoji badges).
  - Build `HomeScreenShortcutManager.kt`: Wraps Android `ShortcutManager.requestPinShortcut` with intent builder for launching installed apps or triggering agy actions.
  - Create `ShortcutIconGeneratorTest.kt` verifying bitmap generation, shapes, dimensions, and shortcut info builder.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/util/ShortcutIconGenerator.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/core/HomeScreenShortcutManager.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/util/ShortcutIconGeneratorTest.kt`

### Wave 2: Native AppWidgets Infrastructure (Prompt Pill, Actions Grid, Status Card)
- **Type:** auto
- **Objective:**
  - Create AppWidget XML metadata and layouts:
    - `res/xml/agy_prompt_widget_info.xml` & `res/layout/widget_prompt_pill.xml`
    - `res/xml/agy_actions_widget_info.xml` & `res/layout/widget_actions_grid.xml`
    - `res/xml/agy_status_widget_info.xml` & `res/layout/widget_status_card.xml`
  - Implement `AgyPromptWidgetReceiver.kt`, `AgyActionsWidgetReceiver.kt`, `AgyStatusWidgetReceiver.kt` extending `AppWidgetProvider`.
  - Register all widget receivers in `AndroidManifest.xml`.
  - Provide `HomeScreenWidgetManager.kt` helper to pin widgets via `AppWidgetManager.requestPinAppWidget()`.
- **Target Files:**
  - `android/app/src/main/res/xml/agy_prompt_widget_info.xml`
  - `android/app/src/main/res/xml/agy_actions_widget_info.xml`
  - `android/app/src/main/res/xml/agy_status_widget_info.xml`
  - `android/app/src/main/res/layout/widget_prompt_pill.xml`
  - `android/app/src/main/res/layout/widget_actions_grid.xml`
  - `android/app/src/main/res/layout/widget_status_card.xml`
  - `android/app/src/main/java/com/yashwant/agymobile/widget/AgyPromptWidgetReceiver.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/widget/AgyActionsWidgetReceiver.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/widget/AgyStatusWidgetReceiver.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/core/HomeScreenWidgetManager.kt`
  - `android/app/src/main/AndroidManifest.xml`

### Wave 3: In-App Home Screen Studio & Widget Reshaper UI
- **Type:** auto
- **Objective:**
  - Create `HomeScreenStudioModal.kt`:
    - Tab 1: **Shortcut Customizer**: Pick target (installed apps or agy actions), select icon shape (Circle, Squircle, Rounded Square), select accent color, enter label, view live preview card, tap "Pin to Home Screen".
    - Tab 2: **Widget Reshaper**: Choose widget type (Prompt Pill, Quick Actions, Status Card), reshape preview (pill vs card vs grid), customize theme tint, and tap "Pin Widget to Home Screen".
  - Wire access points:
    - Navigation Drawer: Add "Home Screen & Widgets" item in `ChatGptDrawer.kt`.
    - Composer: Add "Pin to Home Screen" option in `ChatGptComposer.kt` '+' attachment menu.
    - Wire modal state in `MainActivity.kt` and `AgyViewModel.kt`.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/HomeScreenStudioModal.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/ChatGptDrawer.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ChatGptComposer.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/MainActivity.kt`

### Wave 4: Integration, Testing, Code Review & Release APK Build
- **Type:** auto
- **Objective:**
  - Run full test suite (`./gradlew test`).
  - Run parallel code review subagents (Standards & Spec).
  - Assemble release debug APK `android/agy-mobile-debug.apk`.
  - Commit atomically using personal identity (`10a19yashwant@gmail.com` / `Yashwant00CR7`).
- **Verification Command:** `cd android && ./gradlew test && ./gradlew assembleDebug`
