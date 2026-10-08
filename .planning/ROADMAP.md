# Roadmap: agy-mobile

## Milestones

### Milestone v1.1.0: Mobile Bash Runtime, Storage Access & GSD Bundle
*Goal:* Provide native ARM64 bash command execution on Android, grant full device storage search capabilities, and bundle the full GSD Core skills suite for on-device spec-driven development.
- [x] **Phase 01: Mobile Bash Runtime, Storage Access & GSD Bundle** (Completed)

### Milestone v1.2.0: ChatGPT-Standard Mobile UI & UX Architecture Overhaul
*Goal:* Completely refactor and restructure the mobile UI from scratch to match ChatGPT mobile's fluid UX, navigation drawer, floating composer, and clean message canvas, styled with authentic Antigravity design.
- [x] **Phase 02: ChatGPT-Standard Mobile UI & UX Overhaul** (Completed)

### Milestone v1.3.0: Latency Optimization, Folder-Wise Context Scoping, Session Persistence & Keyboard IME Fix
*Goal:* Eliminate delayed response latency with folder-wise scoping and fast model defaults, implement persistent local conversation storage with full drawer history restoration, and fix keyboard overlap with IME padding and auto-scrolling.
- [x] **Phase 03: Latency Optimization, Folder-Wise Context Scoping, Session Persistence & Keyboard IME Fix** (Completed)

### Milestone v1.4.0: Context Minimization & In-App Reasoning Effort Control
*Goal:* Drastically cut system prompt context overhead by moving to a Lean Skills profile, and introduce an interactive in-app reasoning effort selector bar for live performance testing.
- [x] **Phase 04: Context Minimization & In-App Reasoning Effort Control** (Completed)

### Milestone v1.5.0: Model Conflict Resolution & Persistent Stream-JSON Daemon
*Goal:* Completely eliminate model selection and effort collision crashes, and run turns with sub-second response times using a persistent stream-json background process.
- [x] **Phase 05: Model Conflict Resolution & Persistent Stream-JSON Daemon** (Completed)

### Milestone v1.6.0: Local Device Folder Workspaces & Native Bash Execution Hardening
*Goal:* Enable users to open chats directly in any local device directory via Android SAF picker and quick drawer shortcuts, and harden native bash execution so bash calls never fail with "not callable" or errno 13.
- [x] **Phase 06: Local Device Folder Workspaces & Native Bash Execution Hardening** (Completed)

### Milestone v1.7.0: Mobile AI Device Storage & Interactive In-App Folder Navigator
*Goal:* Transform agy-mobile into a true mobile AI assistant that operates directly on device storage (`/storage/emulated/0`), remembers active device folders across app restarts, provides an interactive in-app folder picker modal, and supports top bar and drawer switching.

#### Phases
- [x] **Phase 07: Mobile AI Device Storage & Interactive Folder Navigator** (Completed)
  - **Goal:** Build `DeviceFolderPickerModal`, enable persistent workspace saving and startup restoration in `WorkspaceManager` and `AgyViewModel`, add top bar workspace switching pill in `ChatGptTopBar`, and verify full device file interactions.
  - **Requirements Addressed:** REQ-32, REQ-33, REQ-34, REQ-35, REQ-36.
  - **Success Criteria:** User can browse and select any device folder from in-app modal, chosen folder persists across restarts, top bar and drawer allow fast switching, tests pass, and APK builds.
