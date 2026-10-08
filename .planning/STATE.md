# GSD Project State

**Active Milestone:** v1.6.0
**Current Phase:** Phase 06 - Local Device Folder Workspaces & Native Bash Execution Hardening
**Status:** In Progress (Planning Complete)
**Last Updated:** 2026-10-08 22:45:00

## User Directives & Decisions
- **Local Mobile Folder Workspaces**: Enable users to pick device folders via Android SAF (`ACTION_OPEN_DOCUMENT_TREE`) and quick shortcuts (`Downloads`, `Documents`, Storage Root) in the Navigation Drawer. The selected folder becomes the working directory for `libagy.so` and binds to the conversation session.
- **Bash Callable Hardening**:
  - In `BootstrapManager`, write an environment profile script exporting `bash() { "$SHELL" "$@"; }` and wire it via `BASH_ENV` and `ENV`. Subshells and scripts running `bash` resolve directly to `libbash.so` without touching forbidden `binDir/bash` (avoiding errno 13 W^X denial).
  - Deploy `~/.gemini/rules/system-android.md` instructing models to invoke the official `run_command` tool instead of hallucinating an uncallable `bash` tool.
- **Persistence**: Save and restore conversations with their exact bound folder path.

## Current Position
- Phase: 06-local-folder-workspace-bash-fix
- Total Plans: 1
- Completed Plans: 0
- Next Action: Execute Plan 01 Wave 1
