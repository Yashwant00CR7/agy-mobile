# GSD Project State

**Active Milestone:** v2.0.0
**Current Phase:** Phase 10 - Interactive File & Directory Deep-Linking with FileProvider
**Status:** Completed
**Last Updated:** 2026-10-09 17:47:00

## User Directives & Decisions
- **Interactive Markdown Links & FileProvider**:
  - User requested ability to tap links in chat to directly access file directories, images, PDFs, and documents.
  - Decision: Parse `[label](url)` markdown links and bare file/directory/web URIs.
  - Directories: Silently switch workspace in `AgyViewModel` (`selectWorkspace(dir, notify = true)`) and show an Android Toast confirming workspace switch.
  - Files (Images, PDFs, Docs): Open in default Android apps via `FileProvider` with appropriate MIME types, `ClipData`, and `FLAG_GRANT_READ_URI_PERMISSION`.
  - Visual: Clean inline links displaying the human-readable label with distinct accent color (`AntigravityBlue`), subtle underline, and icon prefix (📁 for directories, 🖼️ for images, 📄 for documents, 🔗 for web links), hiding the raw URL.
  - URL Percent-Decoding: Properly decode `%20`, `%C2%A0`, and UTF-8 characters via `URLDecoder.decode(..., "UTF-8")` to access real files on disk.

## Current Position
- Phase: 10-interactive-file-links-fileprovider
- Total Plans: 1
- Completed Plans: 1
- Next Action: Ready for next milestone or user feedback
