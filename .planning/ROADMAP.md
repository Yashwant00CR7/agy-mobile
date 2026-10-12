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

### Milestone v1.8.0: Shell Path Hardening, Cold-Start Optimization, Conversation Deletion & Command Inspector
*Goal:* Guarantee reliable bash execution on device folders with self-healing symlinks and system sh fallback, eliminate cold-start stream read errors, support full conversation deletion with confirmation, and display rich command cards and engine debug logs.

#### Phases
- [x] **Phase 08: Shell Path Hardening, Cold-Start Optimization, Conversation Deletion & Command Inspector** (Completed)
  - **Goal:** Fix bash not found in `$PATH` on Android via self-healing links/wrappers, suppress stream close errors, provide conversation deletion in drawer, and surface command execution cards and engine debug logs.
  - **Requirements Addressed:** REQ-37, REQ-38, REQ-39, REQ-40, REQ-41, REQ-42.
  - **Success Criteria:** Agent runs commands like `ls` on device folders without bash errors, stream close errors vanish, conversations can be deleted cleanly from drawer, command cards and debug logs are visible, all tests pass, and APK compiles.

### Milestone v1.9.0: Android Seccomp Resolution & Native Bionic Shell Migration
*Goal:* Completely eliminate exit code 159 (SIGSYS / Bad System Call) on Android device directories by removing the static glibc libbash.so binary, migrating shell execution to Android's built-in Bionic /system/bin/sh (mksh/toybox), and verifying flawless command execution on device storage.

#### Phases
- [x] **Phase 09: Android Seccomp Resolution & Native Bionic Shell Migration** (Completed)
  - **Goal:** Purge libbash.so, resolve primary shell to Android /system/bin/sh, symlink bin/bash to /system/bin/sh, update agent rules, update drawer diagnostics, and build lightweight APK.
  - **Requirements Addressed:** REQ-43, REQ-44, REQ-45, REQ-46.
  - **Success Criteria:** Agent executes commands like `ls` and `find` on device folders without exit code 159 (SIGSYS), tests pass, APK size reduces by ~2.4MB.

### Milestone v2.0.0: Interactive File & Directory Deep-Linking with FileProvider
*Goal:* Enable users to tap file and folder links directly in the chat to open documents and images in default Android apps via FileProvider or switch into the referenced folder.

#### Phases
- [x] **Phase 10: Interactive File & Directory Deep-Linking with FileProvider** (Completed)
  - **Goal:** Set up FileProvider in manifest, parse markdown links with URL-decoding, style clean inline links with icons, and dispatch directory switches and external app view intents.
  - **Requirements Addressed:** REQ-47, REQ-48, REQ-49, REQ-50, REQ-51.
  - **Success Criteria:** Tapping a directory link switches workspace with a toast; tapping an image/PDF/document link opens the default viewer via FileProvider; links render as clean labels with icons.

### Milestone v2.1.0: Home Screen Customization & Widget Studio
*Goal:* Enable users to customize their phone's home screen by designing & pinning custom app and action shortcuts, as well as configuring, previewing, and pinning native Android widgets (Quick AI Prompt Pill, Quick Actions Grid, Workspace & Device Status Card) directly to their existing system launcher.

#### Phases
- [ ] **Phase 11: Home Screen Customization & Widget Studio**
  - **Goal:** Build dynamic shortcut generator with custom shapes & icons, build 3 native AppWidgets, create in-app Home Screen Studio & Widget Reshaper modal, and wire drawer/composer entry points.
  - **Requirements Addressed:** REQ-52, REQ-53, REQ-54, REQ-55, REQ-56.
  - **Success Criteria:** User can visually design custom shortcuts, preview reshaped widgets, pin them to their home screen via Android ShortcutManager and AppWidgetManager, all tests pass, and APK builds.
