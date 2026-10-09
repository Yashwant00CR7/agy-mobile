# Phase Context: Shell Path Hardening, Cold-Start Optimization, Conversation Deletion & Command Inspector

## Problem & User Intent
The user encountered several critical mobile issues:
1. When running commands in mobile device folders (`/storage/emulated/0/Download/Notes`), the assistant reported:
   *"I encountered an error trying to list the contents of your directory. It appears that `bash` (the command-line shell I use to run commands) is not installed or not available in the system's `$PATH`... unable to run commands like `ls`"*
   along with a red banner: *"Read interrupted by close() on another thread"*.
2. Cold-start latency is slow and needs debugging: the user wants to see what commands the agent runs, understand the cold start bottleneck, and have engine debug logs.
3. The user wants the ability to delete conversations with full mobile AI app features (confirmation dialog, removing local sessions and brain files, drawer UI integration).

## Mental Model & Core Abstractions
1. **Shell Self-Healing Links & Fallback Wrapper**:
   - Android APK installs relocate native libraries to dynamic directories (`/data/app/~~<random>/...`). Existing symlinks in `binDir/bash` become dangling.
   - `BootstrapManager` must aggressively clean up broken links using `Files.deleteIfExists()` and re-link directly to `nativeLibraryDir/libbash.so` and `/system/bin/sh`.
   - In addition, write an executable shell script wrapper fallback at `binDir/bash` that invokes `libbash.so "$@"` or `/system/bin/sh "$@"`.
   - Prepend `binDir` and `nativeLibraryDir` to `PATH`, and export `SHELL`, `BASH_ENV`, and `ENV`.
2. **Stream Close & Cold Start Optimization**:
   - `AgyProcessEngine` stream reader throws `IOException: Read interrupted by close() on another thread` when terminating or recycling the daemon. We must filter out stream closure errors and handle them as clean termination.
   - Keep daemon process warm across turns in the same conversation and device folder.
3. **Conversation Deletion UX**:
   - Delete icon button on conversation items in `ChatGptDrawer`.
   - `AlertDialog` confirmation to prevent accidental loss.
   - Atomic cleanup in `ConversationRepository` (`conversations/<id>.json`) and `WorkspaceManager` (`.gemini/brain/<id>`).
   - If the active conversation is deleted, clear chat and navigate to a fresh session.
4. **Command Inspector & Debug Engine Visibility**:
   - In-chat collapsible card for `run_command` tools with command line, cwd, execution duration, status, and stdout/stderr output.
   - Settings dialog includes "Engine Logs & Shell Commands" viewer showing recent process execution history, environment variables, daemon status, and stdout/stderr output lines for real-time mobile debugging.

## Verification & Backpressure Seams
- Unit Tests: `./gradlew test` (including `ConversationRepositoryTest`, `WorkspaceManagerTest`, `BootstrapManagerTest`, `AgyProcessEngineTest`)
- Build: `./gradlew assembleDebug`
