# Phase 03: Latency Optimization, Folder-Wise Scoping, Session Persistence & Keyboard IME Fix

## Plan Overview
- **Goal:** Resolve delayed turn latency, introduce folder-wise context scoping, build robust on-device conversation persistence with full history restoration, and fix keyboard occlusion with IME padding and auto-scrolling.
- **Requirements:** REQ-14, REQ-15, REQ-16, REQ-17, REQ-18, REQ-19.

---

## Waves of Execution

### Wave 1: Keyboard Insets & Auto-Scroll
- **Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/screens/ChatScreen.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ChatGptComposer.kt`
- **Actions:**
  - Update `ChatGptComposer` and `ChatScreen` bottomBar layout with `Modifier.navigationBarsPadding().imePadding()`.
  - Listen for IME height changes using `WindowInsets.ime.getBottom(LocalDensity.current)` in Compose.
  - Automatically scroll `listState.animateScrollToItem(messages.size - 1)` whenever the keyboard appears or messages update.

### Wave 2: Turn Latency & Folder-Wise Context Scoping
- **Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/BootstrapManager.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/core/AgyProcessEngine.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ModelSelectorBottomSheet.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/screens/ChatScreen.kt`
- **Actions:**
  - Change default model from high-effort to `Gemini 3.8 Flash (Medium)` / `gemini-3.8-flash-medium` to achieve fast sub-second stream commencement.
  - Strictly isolate `workspaceDir` execution: pass directory and folder-scoped environment variables so the agent never scans root device storage during a project turn.
  - Add an instant animated thinking indicator in `ChatScreen.kt` ("Antigravity is thinking...") that renders immediately upon sending a message while `isRunning == true` until the first token arrives.

### Wave 3: On-Device Conversation Persistence Engine & Drawer Restoration
- **Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/ConversationRepository.kt` (New)
  - `android/app/src/main/java/com/yashwant/agymobile/model/AgyModels.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/drawers/ChatGptDrawer.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/core/WorkspaceManager.kt`
- **Actions:**
  - Implement `ConversationRepository` reading/writing JSON files to `context.filesDir/conversations/<id>.json`.
  - Serialize full `ChatMessage` history (user, assistant, thoughts, tool calls, diffs, approvals).
  - Save session automatically after each message and at `TurnComplete`.
  - On app launch, start with a fresh clean "New Chat" (as decided by user).
  - In `AgyViewModel.resumeConversation(id)`, load the saved message history, restore `activeConversationId`, and switch workspace folder to that session's folder.
  - Update `ChatGptDrawer` to display stored sessions with their workspace folder badges and snippets.

### Wave 4: Testing & Release Build
- **Actions:**
  - Add comprehensive unit tests in `android/app/src/test/java/com/yashwant/agymobile/core/ConversationRepositoryTest.kt`.
  - Verify all unit tests pass with `./gradlew test`.
  - Compile debug APK with `./gradlew assembleDebug` and copy to `android/agy-mobile-debug.apk`.
  - Verify git status and commit under personal git identity.
