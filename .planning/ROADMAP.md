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

#### Phases
- [x] **Phase 03: Latency Optimization, Folder-Wise Context Scoping, Session Persistence & Keyboard IME Fix**
  - **Goal:** Add folder-wise scoping, low-latency streaming defaults, instant visual thinking indicator, robust local JSON conversation persistence, drawer session restoration, and keyboard-aware IME layout with auto-scroll.
  - **Requirements Addressed:** REQ-14, REQ-15, REQ-16, REQ-17, REQ-18, REQ-19.
  - **Success Criteria:** Automated unit tests pass, debug APK compiles cleanly, chat history survives app lifecycles, composer never covered by keyboard, sub-2s streaming response start.
