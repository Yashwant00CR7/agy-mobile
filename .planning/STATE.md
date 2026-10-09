# GSD Project State

**Active Milestone:** v1.8.0
**Current Phase:** Phase 08 - Shell Path Hardening, Cold-Start Optimization, Conversation Deletion & Command Inspector
**Status:** In Progress
**Last Updated:** 2026-10-09 11:15:00

## User Directives & Decisions
- **Shell Environment & Path Hardening**:
  - `libagy.so` executes commands via `bash -c ...`. On Android, `binDir/bash` symlink pointed to `nativeLibraryDir/libbash.so`. Reinstalls/updates change the randomized package path hash (`/data/app/~~<hash>/...`), breaking the symlink.
  - Fix: Self-healing symlink creation (`Files.deleteIfExists()` before `Files.createSymbolicLink()`), plus a fallback executable shell script in `binDir/bash` that delegates to `/system/bin/sh "$@"` if `libbash.so` cannot be symlinked or invoked.
  - Stream close error suppression: In `AgyProcessEngine`, catch and suppress `IOException` containing `"closed"` or `"close() on another thread"` or during planned daemon teardown so it is treated as a clean EOF instead of an error event.
- **Conversation Deletion UX**:
  - In `ChatGptDrawer.kt`, add a trash icon button to history items.
  - Tapping shows an `AlertDialog` confirmation ("Delete Conversation? This will permanently delete this conversation and its logs.").
  - Deletes from both `ConversationRepository` (`conversations/<id>.json`) and `WorkspaceManager` (`.gemini/brain/<id>`).
  - If the deleted chat was currently open in the active chat screen, automatically reset the screen to a fresh conversation.
- **In-Chat Command Execution Cards**:
  - Upgrade `MessageRole.TOOL` rendering in `ChatScreen.kt` to show interactive collapsible cards for `run_command` with CommandLine, Cwd, duration, status, and stdout/stderr output.
- **Engine Process & Shell Debug Logs Viewer in Settings**:
  - In Settings dialog, add an "Engine Logs & Shell Commands" viewer showing recent process execution history, environment variables, daemon status, and stdout/stderr output lines for real-time mobile debugging.

## Current Position
- Phase: 08-mobile-shell-diagnostics-conversation-deletion
- Total Plans: 1
- Completed Plans: 0
- Next Action: Execute Plan 01 Wave 1 (Shell Hardening & Stream Resiliency)
