# Roadmap: agy-mobile

## Milestones

### Milestone v1.1.0: Mobile Bash Runtime, Storage Access & GSD Plugin Engine
*Goal:* Provide native ARM64 bash command execution on Android, grant full device storage search capabilities, and bundle the full GSD Core skills suite for on-device spec-driven development.

#### Phases
- [x] **Phase 01: Mobile Bash Runtime, Storage Access & GSD Bundle**
  - **Goal:** Bundle `libbash.so`, configure shell environment variables & fallbacks in `BootstrapManager`, declare and handle `MANAGE_EXTERNAL_STORAGE` permissions, and auto-bootstrap full GSD Core skills into mobile engine.
  - **Requirements Addressed:** REQ-01, REQ-02, REQ-03, REQ-04, REQ-05, REQ-06.
  - **Success Criteria:** Automated test suite passes, debug APK builds with `libbash.so` and GSD assets, `BootstrapManager` extracts assets and resolves shell.
