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

#### Phases
- [x] **Phase 04: Context Minimization & In-App Reasoning Effort Control** (Completed)
  - **Goal:** Move 70+ non-essential skills to on-demand library so input tokens drop from 20k to <1k, implement an interactive effort selector bar (`Low ⚡`, `Medium ⚖️`, `High 🧠`) in the UI, and connect it to `AgyProcessEngine`.
  - **Requirements Addressed:** REQ-20, REQ-21, REQ-22, REQ-23.
  - **Success Criteria:** Input tokens drop significantly, user can switch effort dynamically from within the app, tests pass, APK builds cleanly.
