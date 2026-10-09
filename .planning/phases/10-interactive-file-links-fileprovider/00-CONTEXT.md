# Phase Context: Interactive File & Directory Deep-Linking with FileProvider

## Problem & User Intent
When Antigravity locates files (e.g. `[Gmail - Congratulations! Vendasta Offer for Developer l.pdf](file:///storage/emulated/0/Download/Gmail%20-%20Congratulations!%20Vendasta%20Offer%20for%C2%A0Developer%20l.pdf)`), markdown links are rendered as plain unclickable text with raw brackets and percent-encoded URLs.
The user wants to be able to tap these links in the chat to directly open files/images in their default Android apps or switch into the referenced directory.

## Locked Architectural Decisions
- **FileProvider Integration**: Register `androidx.core.content.FileProvider` in `AndroidManifest.xml` with authority `${applicationId}.fileprovider` and `res/xml/file_paths.xml` allowing `<external-path>` access.
- **Link Click Behavior**:
  - **Directories**: Switch workspace silently in `AgyViewModel` (`selectWorkspace(dir, notify = true)`) and show an Android Toast confirming workspace switch.
  - **Files (Images, PDFs, Docs)**: Open via default Android apps using `Intent(Intent.ACTION_VIEW)` with `FileProvider.getUriForFile(...)`, setting appropriate MIME type (`image/*`, `application/pdf`, `text/*`, `*/*`) and `FLAG_GRANT_READ_URI_PERMISSION`.
  - **Web Links (`http://`, `https://`)**: Open in default web browser.
- **Visual Presentation**: Clean inline link displaying the human-readable label with distinct accent color (`AntigravityBlue`), subtle underline, and icon prefix (📁 for directories, 🖼️ for images, 📄 for documents, 🔗 for web links), hiding the raw URL.
- **URL Percent-Decoding**: Properly decode `%20`, `%C2%A0`, and UTF-8 characters via `URLDecoder.decode(..., "UTF-8")` before creating `File` objects.

## Verification Seams
- Unit tests for markdown link parsing and URL decoding in `MarkdownParserTest`.
- `./gradlew test`
- `./gradlew assembleDebug`
