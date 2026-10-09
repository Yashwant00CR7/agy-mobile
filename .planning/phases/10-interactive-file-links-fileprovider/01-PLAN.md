---
phase: 10-interactive-file-links-fileprovider
plan: 01
type: execute
tracer_first: true
---

# Plan: Interactive File & Directory Deep-Linking with FileProvider

## Objective
Enable users to click file, directory, image, and web links directly in chat messages. Configure `FileProvider` to securely launch default external Android apps for images, PDFs, and documents, switch active workspace silently with a confirmation toast for directories, and render clean inline markdown links with type-specific icons and accent styling.

## Requirements Addressed
- REQ-47: FileProvider & System File Intent Architecture
- REQ-48: Markdown Link Parser with URL Percent-Decoding
- REQ-49: Clickable Inline Markdown Links in Compose
- REQ-50: Direct Deep-Link Action Dispatcher
- REQ-51: Unit Testing, Code Review & Release APK Build

## Execution Waves

### Wave 1: Tracer Slice (FileProvider Setup & FileOpener Utility)
- **Type:** auto
- **Objective:**
  - Create `android/app/src/main/res/xml/file_paths.xml` allowing `<external-path path="." name="external_files" />` and `<files-path path="." name="internal_files" />`.
  - Add `<provider android:name="androidx.core.content.FileProvider" ...>` to `AndroidManifest.xml`.
  - Create `FileOpener.kt` utility class with functions:
    - `resolveTarget(rawUriOrPath: String): FileTarget` (decodes percent-encoding, checks directory vs file vs web).
    - `openTarget(context: Context, target: FileTarget, onSelectDirectory: (File) -> Unit)` (handles directory switch, file intent with FileProvider, or browser intent).
    - `getMimeType(file: File): String`.
- **Target Files:**
  - `android/app/src/main/res/xml/file_paths.xml`
  - `android/app/src/main/AndroidManifest.xml`
  - `android/app/src/main/java/com/yashwant/agymobile/util/FileOpener.kt`

### Wave 2: Markdown Link Parser & Clickable Text in Compose
- **Type:** tdd
- **Objective:**
  - Create `MarkdownLinkParser.kt` helper that extracts `[label](url)` links and bare URIs into structured tokens.
  - Prefix links with type-specific emoji/icons:
    - 📁 for directories
    - 🖼️ for images (`.png`, `.jpg`, `.jpeg`, `.webp`, `.gif`, `.svg`, `.bmp`)
    - 📄 for documents/PDFs (`.pdf`, `.txt`, `.docx`, etc.)
    - 🔗 for web URLs (`http://`, `https://`)
  - Update `RichMarkdownMessage` in `ChatScreen.kt` to use Compose `ClickableText` or `AnnotatedString` with `LinkAnnotation` / `addStringAnnotation("URL", target)` so clicking a link invokes the link click handler.
  - Wire `onOpenLink: (String) -> Unit` in `ChatScreen.kt` and `MainActivity.kt`.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/util/MarkdownLinkParser.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/screens/ChatScreen.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/MainActivity.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/util/MarkdownLinkParserTest.kt`

### Wave 3: Integration, Review & Release Build
- **Type:** auto
- **Objective:**
  - Run full test suite (`./gradlew test`).
  - Run parallel code review subagents (Standards & Spec).
  - Assemble release debug APK (`./gradlew assembleDebug`).
  - Commit atomically with personal GitHub identity.
- **Verification Command:** `cd android && ./gradlew test && ./gradlew assembleDebug`
