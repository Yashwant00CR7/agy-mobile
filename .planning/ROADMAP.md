# Roadmap: agy-mobile

## Milestones

### Milestone v1.1.0: Mobile Bash Runtime, Storage Access & GSD Plugin Engine
*Goal:* Provide native ARM64 bash command execution on Android, grant full device storage search capabilities, and bundle the full GSD Core skills suite for on-device spec-driven development.
- [x] **Phase 01: Mobile Bash Runtime, Storage Access & GSD Bundle** (Completed)

### Milestone v1.2.0: ChatGPT-Standard Mobile UI & UX Architecture Overhaul
*Goal:* Completely refactor and restructure the mobile UI from scratch to match ChatGPT mobile's fluid UX, navigation drawer, floating composer, and clean message canvas, styled with authentic Antigravity design.

#### Phases
- [x] **Phase 02: ChatGPT-Standard Mobile UI & UX Overhaul**
  - **Goal:** Redesign theme palette, implement left `ModalNavigationDrawer`, centered model dropdown topbar, fluid chat canvas with collapsible pills, floating capsule composer, and minimal welcome screen.
  - **Requirements Addressed:** REQ-07, REQ-08, REQ-09, REQ-10, REQ-11, REQ-12, REQ-13.
  - **Success Criteria:** Automated tests pass (45 tasks), APK builds cleanly via `./gradlew assembleDebug`, end-to-end user flows verified.
