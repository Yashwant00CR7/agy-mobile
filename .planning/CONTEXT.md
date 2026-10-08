# Phase Context: Mobile AI Device Storage & Interactive Folder Navigator

## Problem & User Intent
The user wants agy-mobile to function like a true mobile AI assistant that is not restricted to an app-private internal directory (`/data/user/0/.../workspaces`). The AI should directly interact with real phone files across device storage (`/storage/emulated/0`, `Download`, `Documents`, etc.). The previous SAF file picker had restrictions on Android, was hard to access, and did not persist the chosen phone folder across restarts.

## Mental Model & Core Abstractions
1. **Device Storage as First-Class Environment**:
   - The primary working directory is on real device storage (`/storage/emulated/0/...`).
   - The last selected mobile workspace is persisted across app launches. When the app reopens, it restores that folder instead of reverting to app internal storage.
2. **Interactive In-App Folder Navigator (`DeviceFolderPickerModal`)**:
   - Instead of relying solely on Android's SAF picker (which blocks root and Downloads access), the app provides a native Compose modal with direct filesystem drilldown.
   - Features: Breadcrumbs, quick shortcuts (`Downloads`, `Documents`, `Storage Root`), folder listing with item counts, `.. (Parent Folder)` navigation, "+ New Folder" creation, and "Select This Folder" button.
   - Built-in permission banner if `MANAGE_EXTERNAL_STORAGE` is missing, allowing one-tap access to Android Settings.
3. **Dual Quick Access (Drawer + Top Bar)**:
   - Accessible via a prominent "Choose Device Folder" button in the Navigation Drawer.
   - Also accessible directly from the Chat Top Bar via an interactive workspace pill (`📁 Download ▾`).
4. **Engine & Chat Binding**:
   - Switching folders immediately scopes `libagy.so` to that directory (`ANTIGRAVITY_WORKSPACE_ROOT`, `WORKSPACE_DIR`, `PWD`).
   - Each chat session records its associated device folder path and restores it when resumed.

## Verification & Backpressure Seams
- Unit Tests: `./gradlew test` (including `WorkspaceManagerTest`, `BootstrapManagerTest`, `AgyProcessEngineTest`)
- Build: `./gradlew assembleDebug`
