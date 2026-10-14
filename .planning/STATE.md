# GSD Project State

**Active Milestone:** v2.3.0
**Current Phase:** Phase 13 - On-Device APK Compiler Engine & Conversational App Builder
**Status:** Completed
**Last Updated:** 2026-10-14 13:42:00

## User Directives & Decisions
1. **Interaction Model**:
   - Purely conversation-driven in `agy-mobile` chat.
   - When the user asks `agy` to build an Android app, `agy` writes the code, invokes the on-device compiler toolchain in the background, and outputs an interactive Cyber-Minimalist `ApkInstallCard` directly in chat.
2. **Error Handling & Self-Healing**:
   - Transparent self-healing loop with live status pills (`Build failed: fixing syntax error in MainActivity.java...`).
   - `agy` parses compiler output, patches code, and retries compilation autonomously up to 3 times before surfacing blockers.
3. **Post-Build Actions**:
   - Full lifecycle actions on the chat card: `[Install Now]` (system `PackageInstaller` via `FileProvider`), `[Share]` (Android `ACTION_SEND` share sheet), and `[Save to Downloads]` (copies to `/storage/emulated/0/Download/`).
4. **Toolchain & Permissions**:
   - 100% offline bundled. Pre-bundles a lightweight `android.jar`, `libaapt2.so`, and in-process `ecj`, `d8`, and `apksigner`.
   - Contextually requests `REQUEST_INSTALL_PACKAGES` only when the user taps `[Install Now]`.
5. **Target Capability Baseline**:
   - Standard native Android apps (Activities, XML layouts with ConstraintLayout/LinearLayout, drawables, Java logic).
   - Validated end-to-end with a functional tracer app (Sleek Calculator).

## Current Position
- Phase: 13-on-device-apk-compiler
- Total Plans: 1
- Completed Plans: 1
- Status: Milestone v2.3.0 Complete
- Artifact Target: `android/agy-mobile-debug.apk` (125 MB)
