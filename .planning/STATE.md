# GSD Project State

**Active Milestone:** v2.4.0
**Current Phase:** Phase 14 - Compiler Background IPC & In-Chat Intent Auto-Trigger
**Status:** Completed
**Last Updated:** 2026-10-14 15:50:00

## User Directives & Decisions
1. **Background Compilation**:
   - Always run APK compilation asynchronously in the background so the UI and chat stay fully responsive.
   - Notify the user immediately when done with the interactive `ApkInstallCard`, or present a clear diagnostic error card if it cannot be built.
2. **Dual-Trigger Initiation**:
   - Direct In-Chat Intent Recognition: When user types "compile the apk for me", "build apk", etc., automatically execute compilation in the background.
   - "🔨 Compile APK" Quick Action chip in the chat action bar.
   - Synchronous Bidirectional `build-apk` CLI IPC bridge: `build-apk` shell runner communicates with `ApkCompilerEngine` and returns real status, paths, or diagnostics to the agent.
3. **Agent Awareness**:
   - Write compiler policy directly into `GEMINI.md` and system rules so the LLM is explicitly informed of the on-device toolchain and never hallucinate that it's impossible.
4. **Self-Healing Loop**:
   - If build fails, `ApkInstallCard` shows diagnostic errors with an instant 1-tap "Ask AI to Fix & Recompile" button that pipes the error log straight to the agent.

## Current Position
- Phase: 14-compiler-background-ipc-intent-fix
- Total Plans: 1
- Completed Plans: 1
- Status: Completed & Verified
- Artifact Target: `android/agy-mobile-debug.apk` -> Uploaded to Google Drive folder `agy-mobile`
