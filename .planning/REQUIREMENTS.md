# Requirements: ChatGPT-Standard Mobile UI & UX Overhaul

- [x] **REQ-07**: Update color palette and theme to authentic Antigravity Dark (`#131314` canvas, `#1E1F22` surfaces, `#2B2D31` bubbles, refined blue/violet accents), removing all neon cyberpunk borders and matrix badges.
- [x] **REQ-08**: Implement full ChatGPT-style left sliding `ModalNavigationDrawer` containing "+ New Chat" button, grouped conversation history (Today, Yesterday, Older), workspace switcher, and user account status bar.
- [x] **REQ-09**: Implement ChatGPT-style top bar with hamburger menu icon (opens drawer), centered interactive model dropdown pill (`Gemini 2.5 Flash ▾`), and new chat button.
- [x] **REQ-10**: Overhaul chat message layout to fluid ChatGPT canvas: right-aligned clean user bubbles, seamless full-width assistant messages, and collapsible accordion pills for thoughts and tool calls.
- [x] **REQ-11**: Redesign bottom composer as a floating capsule pill (`RoundedCornerShape(26.dp)`) with `+` action button, auto-expanding text input, and circular send/stop button that seamlessly switches to mic when empty.
- [x] **REQ-12**: Redesign empty state (welcome screen) to a clean, minimal ChatGPT-style layout with centered Antigravity logo, warm greeting, and clean suggestion prompt pills.
- [x] **REQ-13**: Maintain full integration with existing `AgyViewModel`, `AgyAgentService`, speech recognition, file diffs, tool approval flows, and storage permission checks; pass unit tests and `./gradlew assembleDebug`.
