# Requirements: agy-mobile

## Completed Milestones
- [x] **REQ-01**: Bundle native ARM64 bash binary with execute permissions.
- [x] **REQ-02**: Implement Terminal / Shell execution helper.
- [x] **REQ-03**: Support device-wide storage access and permission flows.
- [x] **REQ-04**: Bundle GSD Core skills in assets and extract to runtime directory.
- [x] **REQ-05**: Inject shell and GSD environment variables to agent process.
- [x] **REQ-06**: Verify shell execution and GSD workflow end-to-end.
- [x] **REQ-07**: Update color palette and theme to authentic Antigravity Dark.
- [x] **REQ-08**: Implement full ChatGPT-style left sliding `ModalNavigationDrawer`.
- [x] **REQ-09**: Implement ChatGPT-style top bar with centered model dropdown pill.
- [x] **REQ-10**: Overhaul chat message layout to fluid ChatGPT canvas.
- [x] **REQ-11**: Redesign bottom composer as a floating capsule pill.
- [x] **REQ-12**: Redesign empty state (welcome screen).
- [x] **REQ-13**: Maintain full integration with existing `AgyViewModel` and background service.
- [x] **REQ-14**: Turn Latency & Folder-Wise Context Scoping.
- [x] **REQ-15**: Instant Visual Thinking State.
- [x] **REQ-16**: Conversation Session Persistence.
- [x] **REQ-17**: Session Restoration & App Startup.
- [x] **REQ-18**: Keyboard IME Padding & Auto-Scroll.
- [x] **REQ-19**: End-to-End Build & Verification.
- [x] **REQ-20**: Lean Skills Profile.
- [x] **REQ-21**: In-App Reasoning Effort Bar.
- [x] **REQ-22**: Effort Integration in Engine & TopBar.
- [x] **REQ-23**: Verification & Release Build v1.4.0.
- [x] **REQ-24**: Fixed-Tier Model Mapping & Conflict Elimination.
- [x] **REQ-25**: Persistent Stream-JSON Daemon in AgyProcessEngine.
- [x] **REQ-26**: UI Synchronization & Effort Locking.
- [x] **REQ-27**: Comprehensive Verification & Build v1.5.0.
- [x] **REQ-28**: Local Mobile Device Folder Workspaces.
- [x] **REQ-29**: Direct Chat-to-Folder Binding & Persistence.
- [x] **REQ-30**: Native Bash Callable Hardening in Android Runtime.
- [x] **REQ-31**: Testing, Verification & Release Build v1.6.0.

---

## Milestone v1.7.0: Mobile AI Device Storage & Interactive In-App Folder Navigator

- [x] **REQ-32**: **In-App Device Folder Navigator & Picker Modal**:
  - Build `DeviceFolderPickerModal.kt` providing an interactive directory browser navigating through device storage (`/storage/emulated/0`), with breadcrumbs, quick shortcuts (`Downloads`, `Documents`, `Storage Root`), folder listing with subfolder item counts, `.. (Parent Folder)` navigation, "+ New Folder" dialog, and a primary "Select This Folder" action button.
  - If `Environment.isExternalStorageManager()` is false, display an in-modal permission prompt banner with a one-tap action taking the user to Android Settings to enable "All Files Access".
- [x] **REQ-33**: **Persistent Mobile Workspace Across App Launches**:
  - In `WorkspaceManager` and `AgyViewModel`, persist the last selected workspace path to `settings.json`.
  - On app launch, if a device workspace was previously chosen and still exists, automatically initialize and restore that mobile folder as the active workspace instead of reverting to the app-internal default `workspaces/` directory.
  - If storage permission is granted and no custom workspace has been set, default to a device folder (e.g. `/storage/emulated/0/Download` or `/storage/emulated/0`) rather than app private internal storage so the app behaves like a true mobile AI.
- [x] **REQ-34**: **Dual Access Points (Drawer & Top Bar) for Mobile Folder Switching**:
  - Navigation Drawer (`ChatGptDrawer.kt`): Add a prominent "Choose Device Folder" action card and allow tapping the current workspace pill to open the modal.
  - Chat Top Bar (`ChatGptTopBar.kt` / `ChatScreen.kt`): Add an interactive folder indicator pill beside or below the model selector (e.g. `📁 Download ▾`) so the user can see and change their active device folder directly from the chat screen at any time without opening the drawer.
- [x] **REQ-35**: **Device File Interaction & Engine Workspace Binding**:
  - When a device folder is chosen, instantly update `AgyViewModel`, notify the user with a subtle system message ("Workspace switched to: /path"), and bind all future queries, file edits, and commands to that path.
  - `AgyProcessEngine` terminates any stale background daemon if the workspace path changes and spawns the engine in the new device folder with `ANTIGRAVITY_WORKSPACE_ROOT`, `WORKSPACE_DIR`, `PWD`, and `processBuilder.directory(folder)`.
- [x] **REQ-36**: **Unit Testing & Release Build Verification**:
  - Add unit tests for `WorkspaceManager` persistent workspace loading/saving and directory navigation.
  - Run `./gradlew test` and compile `android/agy-mobile-debug.apk`.

---

## Milestone v1.8.0: Shell Path Hardening, Cold-Start Optimization, Conversation Deletion & Command Inspector

- [x] **REQ-37**: **Native Bash Path Hardening & Self-Healing Links**:
  - In `BootstrapManager`, always recreate fresh symlinks (`Files.deleteIfExists()` before `Files.createSymbolicLink()`) so `binDir/bash` and `binDir/sh` never remain dangling after APK updates when `nativeLibraryDir` path hash changes.
  - Provide a dual-mode fallback: if symlink creation or execution fails, create a POSIX shell script wrapper in `binDir/bash` invoking `/system/bin/sh "$@"`.
  - Ensure `$SHELL` and `$PATH` point to valid executables, avoiding `exec: "bash": executable file not found in $PATH`.
- [x] **REQ-38**: **Daemon Stream Resiliency & Cold-Start Fix**:
  - In `AgyProcessEngine`, catch and suppress `IOException: Read interrupted by close() on another thread` during daemon restarts/shutdown so it is never displayed as a red user error banner.
  - Keep the background `stream-json` process alive and warm across consecutive turns in the same conversation and workspace directory, eliminating repetitive 3-4s process startup overhead.
- [x] **REQ-39**: **Conversation Deletion with Confirmation & Multi-Store Cleanup**:
  - In `ChatGptDrawer.kt`, add a delete action button (trash icon) to history items in the drawer list.
  - Display an `AlertDialog` confirmation ("Delete Conversation? This will permanently delete this conversation and its logs.").
  - Upon confirmation, delete the session from both `ConversationRepository` (`conversations/<id>.json`) and `WorkspaceManager` (`.gemini/brain/<id>`), refresh the conversation list, and reset the active chat screen if the deleted conversation is currently open.
- [x] **REQ-40**: **In-Chat Collapsible Command Execution Cards**:
  - In `ChatScreen.kt` and `MessageItem.kt`, upgrade `MessageRole.TOOL` rendering to display an interactive command card showing tool name (`run_command`), the command line string, working directory, status (`Running`, `Success`, `Failed`), execution duration, and expandable stdout/stderr outputs with code syntax styling.
- [x] **REQ-41**: **Engine Process & Shell Debug Logs Viewer in Settings**:
  - In `ChatGptDrawer.kt` Settings dialog, add an "Engine Logs & Shell Commands" viewer showing recent process execution history, environment variables, daemon status, and stdout/stderr output lines for real-time mobile debugging.
- [x] **REQ-42**: **Testing, Verification & Release Build v1.8.0**:
  - Add unit tests for `ConversationRepository` and `WorkspaceManager` deletion, and `BootstrapManager` self-healing links.
  - Run `./gradlew test` and compile `android/agy-mobile-debug.apk`.

---

## Milestone v1.9.0: Android Seccomp Resolution & Native Bionic Shell Migration

- [x] **REQ-43**: **Native Bionic Shell Resolution & Purge `libbash.so`**:
  - Remove `android/app/src/main/jniLibs/arm64-v8a/libbash.so` from the project to eliminate the static glibc ELF binary that triggers Android seccomp filter `SIGSYS` (exit code 159).
  - Update `BootstrapManager.kt` so `getShellExecutable()` resolves to Android's built-in `/system/bin/sh` (or `/bin/sh` on Unix/macOS) rather than looking for `libbash.so`.
  - Re-link both `binDir/bash` and `binDir/sh` to the native `/system/bin/sh` executable.
- [x] **REQ-44**: **Android Environment & Rule Hardening**:
  - Update `SHELL` environment variable to point to `/system/bin/sh`.
  - Update runtime prompt rules (`system-android.md`, `android-runtime.md`) to reflect that the native shell is `/system/bin/sh` (mksh/toybox), fully callable as both `sh` and `bash`.
- [x] **REQ-45**: **Drawer Settings & Engine Diagnostics UI Update**:
  - Update `ChatGptDrawer.kt` Engine & Shell Logs dialog and About section to display `/system/bin/sh (Android Bionic Shell)` instead of `libbash.so`.
- [x] **REQ-46**: **Test Suite, Code Review & Release APK Build**:
  - Update `BootstrapManagerTest.kt` to verify `/system/bin/sh` and `/bin/sh` resolution.
  - Run `./gradlew test` and verify 100% pass rate.
  - Assemble release debug APK `android/agy-mobile-debug.apk` and verify APK size drops by ~2.4MB.

---

## Milestone v2.0.0: Interactive File & Directory Deep-Linking with FileProvider

- [x] **REQ-47**: **FileProvider & System File Intent Architecture**:
  - Configure `androidx.core.content.FileProvider` in `AndroidManifest.xml` with authority `${applicationId}.fileprovider`.
  - Provide `res/xml/file_paths.xml` allowing `<external-path>` and `<files-path>`.
  - Implement `FileOpener.kt` to launch `Intent(Intent.ACTION_VIEW)` with `content://` URI, correct MIME types, and `FLAG_GRANT_READ_URI_PERMISSION`.
- [x] **REQ-48**: **Markdown Link Parser with URL Percent-Decoding**:
  - Implement `MarkdownLinkParser.kt` to extract markdown links `[label](url)` and bare file/directory/web URIs.
  - Properly URL-decode `%20`, `%C2%A0`, and UTF-8 characters via `URLDecoder.decode(..., "UTF-8")` to access real files on disk.
  - Categorize links into Directory, Image, Document, or Web.
- [x] **REQ-49**: **Clickable Inline Markdown Links in Compose**:
  - Upgrade `RichMarkdownMessage` in `ChatScreen.kt` using Compose `AnnotatedString` with link annotations and click listener.
  - Display clean human-readable labels with icon prefix (📁 for directories, 🖼️ for images, 📄 for documents, 🔗 for web links), hiding raw URLs.
  - Style with distinct `AntigravityBlue` accent and subtle underline.
- [x] **REQ-50**: **Direct Deep-Link Action Dispatcher**:
  - For directory links: Silently switch workspace in `AgyViewModel` (`selectWorkspace(dir, notify = true)`) and show an Android Toast confirming workspace switch.
  - For file links: Open in default external apps via `FileOpener.openFile()`.
  - For web links: Open in default browser.
- [x] **REQ-51**: **Unit Testing, Code Review & Release APK Build**:
  - Add unit tests for `MarkdownLinkParser` and URL-decoding in `MarkdownLinkParserTest.kt`.
  - Run `./gradlew test` and assemble `android/agy-mobile-debug.apk`.

---

## Milestone v2.1.0: Home Screen Customization & Widget Studio

- [x] **REQ-52**: **Dynamic Shortcut Creator & Bitmap Icon Generator**:
  - Implement `ShortcutIconGenerator.kt` to dynamically draw custom styled app icons into Bitmaps (shapes: Circle, Squircle, Rounded Square; customizable background colors, border rings, and icon/emoji centerpieces).
  - Implement `HomeScreenShortcutManager.kt` to wrap Android `ShortcutManager.requestPinShortcut()` with dynamic intent mapping (launch installed apps or trigger agy actions like new chat or folder workspace).
- [x] **REQ-53**: **Native AppWidget Architecture & XML Providers**:
  - Create and register 3 native Android AppWidgets:
    1. `AgyPromptWidgetReceiver`: Quick AI Prompt Pill (one-tap voice or text query into agy-mobile).
    2. `AgyActionsWidgetReceiver`: Quick Actions Bar (customizable multi-action grid for phone tools and AI triggers).
    3. `AgyStatusWidgetReceiver`: Workspace & Device Status Card (live workspace path, git branch, and quick chat launch).
  - Provide XML metadata and RemoteViews layouts with high-contrast Dark & Neon aesthetics.
  - Implement `HomeScreenWidgetManager.kt` supporting `AppWidgetManager.requestPinAppWidget()`.
- [x] **REQ-54**: **In-App Home Screen Studio & Widget Reshaper UI**:
  - Create `HomeScreenStudioModal.kt` with two tabs:
    - *Shortcuts Tab*: Pick app/action, select shape (Circle, Squircle, Rounded Square), select accent color, enter label, preview live, and tap "Pin to Home Screen".
    - *Widgets Tab*: Choose widget type, preview reshaped layouts (Pill vs Grid vs Status Card), select theme tint, and tap "Pin Widget to Home Screen".
- [x] **REQ-55**: **Dual Entry Points & Assistant Integration**:
  - Add "Home Screen & Widgets" item in `ChatGptDrawer.kt`.
  - Add "Pin to Home Screen" action in `ChatGptComposer.kt` '+' attachment menu.
  - Connect state management in `AgyViewModel.kt` and `MainActivity.kt`.
- [x] **REQ-56**: **Test Suite, Code Review & Release APK Build**:
  - Write unit tests for `ShortcutIconGeneratorTest.kt` verifying shape clipping, dimensions, and shortcut info generation.
  - Run `./gradlew test` and assemble `android/agy-mobile-debug.apk`.

---

## Milestone v2.2.0: Git Runtime, Heading Markdown Engine, Live Command HUD, Usage Credits & Remote Accessibility

- [ ] **REQ-57**: **Heading & Markdown Rich Typography Engine**:
  - Implement full Markdown block and line parser in `MarkdownLinkParser.kt` and `ChatScreen.kt`.
  - Parse `# H1` (22sp bold, elevated line height), `## H2` (18sp bold), `### H3` (15sp bold), stripping raw `#` characters.
  - Format bullet lists (`*`, `-`) as indented bullet points (`• `) with clean typography.
  - Retain code block syntax highlighting, copy buttons, and interactive FileProvider links.
- [ ] **REQ-58**: **Native Standalone ARM64 Git Runtime & Dynamic Git Accounts**:
  - Bundle standalone ARM64 `git` executable / wrapper in the app environment so standard commands (`git status`, `git log`, `git commit`, `git diff`, `git branch`) execute reliably in Bionic shell without 'inaccessible or not found'.
  - Support Dynamic Git Accounts in `BootstrapManager.kt` and Settings, defaulting to personal identity (`Yashwant00CR7` / `10a19yashwant@gmail.com`) with instant switcher.
- [ ] **REQ-59**: **Interactive Live Command Monitoring HUD**:
  - Implement real-time streaming Command Execution HUD in `ToolCallCard.kt`.
  - Live animated running badge, ticking duration timer (`Running 3.4s...`), and bounded streaming terminal viewport (~220dp max-height) with auto-scroll and line-counter.
  - Fullscreen terminal expand dialog and instant "Stop / Cancel" button to kill executing commands.
- [ ] **REQ-60**: **Usage Credits & Token Quota Tracker**:
  - Implement `UsageCreditsManager.kt` tracking prompt tokens, output tokens, and message turns per session and day.
  - Add sleek "Credits & Token Usage" card in `ChatGptDrawer.kt` with live percentage gauge and tier indicator.
  - Provide detailed Usage & Quota Breakdown modal in Drawer Settings showing per-chat and per-model consumption.
- [ ] **REQ-61**: **Remote Control Accessibility Service**:
  - Implement `AgyAccessibilityService.kt` registered in AndroidManifest with `accessibility_service_config.xml`.
  - Provide automation actions: `device_click`, `device_scroll`, `device_type_text`, `device_press_key` (Home/Back/Recents), `device_read_screen`.
  - Implement safety guardrails (autonomous execution for navigation, confirmation prompt for sensitive actions).
  - Add in-app Accessibility Settings bridge card directing user to enable permission if not yet active.
- [ ] **REQ-62**: **Test Suite Verification, Build & Google Drive Deployment**:
  - Write comprehensive unit tests for markdown typography, git environment setup, usage tracker, and accessibility helpers.
  - Build verified debug APK `android/agy-mobile-debug.apk`.
  - Upload release APK directly to Google Drive folder `agy-mobile` (ID: `1nvhmMmrcd8U6oOtMNNjUP1_uB1s0WZtT`).
