# PRD: agy-mobile — Mobile PWA & Daemon for Antigravity in Termux

## Problem & User Intent
Running Google Antigravity CLI (`agy`) directly on mobile via Android Termux currently requires typing complex commands and navigating text-heavy terminal UIs on a cramped touchscreen virtual keyboard. The user wants a dedicated mobile application experience that runs *completely* inside their Android phone over Termux, wrapping `agy` into a touch-first, mobile-friendly interface with one-tap approvals, streaming chat cards, collapsible diffs, workspace drawers, and a built-in terminal toggle, all running seamlessly in the background without Android killing the process.

## Mental Model & Core Abstractions
- **Self-Contained Mobile Runtime**: The entire stack (the `agy` agent, the Node.js server daemon, and the static PWA frontend) executes directly inside Termux on Android with zero cloud intermediaries or external server dependencies.
- **Dual Surface (Chat + Terminal)**:
  - *Touch Chat View*: High-level developer experience for mobile. Visual cards for tool runs, one-tap approval buttons, diff viewer, voice input, and markdown rendering.
  - *Terminal View*: Direct xterm.js pseudo-terminal with a mobile touch accessory bar (Ctrl, Alt, Esc, Tab, arrows) for low-level shell commands.
- **Execution Modes**:
  - *Autonomous Mode*: Runs with `--dangerously-skip-permissions` for hands-free background execution while the phone is locked or in your pocket.
  - *Supervised Mode*: Intercepts tool calls and displays approval cards on mobile with vibration and notification alerts.
- **Termux Native Lifecycle**: Uses `termux-wake-lock` to keep the CPU awake during task execution and `termux-notification` to notify the user when input is required.

## Architectural Decisions & Constraints
- **Zero-Dependency Runtime Bundle**: The Node.js backend and client assets are pre-bundled with `esbuild` into a single standalone `dist/server.js` and `dist/public/`. Running on Android requires only `node dist/server.js` (no heavy `npm install` or C-compiler toolchains required in Termux).
- **Communication Protocol**:
  - `agy` process orchestration via `--input-format stream-json --output-format stream-json` for real-time NDJSON event streaming and tool approval handling.
  - WebSockets (`/ws/chat` for stream-json events, `/ws/pty` for interactive terminal).
  - REST endpoints for workspace browsing, conversation switching, and system status.
- **Repo & Git Identity**:
  - Git identity must strictly be repo-local to Yashwant's personal account: `Yashwant00CR7` / `10a19yashwant@gmail.com`.
  - Remote URL: `git@github-personal:Yashwant00CR7/agy-mobile.git`. Never touch `--global` git config.

## Verification & Backpressure
Command(s) to run after each task:
- Test: `npm test` (using native `node --test`)
- Lint/Typecheck: `npx tsc --noEmit`
- Build: `npm run build` (esbuild bundle)

---

## Discrete Tasks

### Task 1: Project Scaffolding & Build Pipeline
- **Objective:** Initialize `package.json`, `tsconfig.json`, `build.mjs` (esbuild pipeline), `.gitignore`, and initial test scaffolding.
- **Acceptance Criteria:** `npm run build` succeeds, `npm test` runs cleanly, and `npx tsc --noEmit` passes.
- **Target Files:** `package.json`, `tsconfig.json`, `build.mjs`, `.gitignore`, `test/setup.test.ts`.

### Task 2: Antigravity CLI Process & NDJSON Stream Engine
- **Objective:** Implement `AgyProcessManager` that manages `agy` child processes, handles NDJSON streaming (`stream-json`), dispatches turns, manages conversation IDs, and supports autonomous vs supervised permission flags.
- **Acceptance Criteria:** Automated unit tests verify NDJSON event parsing, mock turn streaming, approval dispatch, and process lifecycle.
- **Target Files:** `src/server/agy-process.ts`, `src/types/agy.ts`, `test/agy-process.test.ts`.

### Task 3: PTY Terminal Bridge for Live Mobile Shell
- **Objective:** Implement `TerminalBridge` to manage an interactive terminal session over WebSockets, relaying stdin/stdout and handling terminal resize events.
- **Acceptance Criteria:** Unit tests verifying WebSocket PTY connection lifecycle, input relay, resize events, and clean teardown.
- **Target Files:** `src/server/terminal-bridge.ts`, `test/terminal-bridge.test.ts`.

### Task 4: Workspace, Git & Conversation History Service
- **Objective:** Implement `WorkspaceService` to list directories in Termux, detect active git branches, switch working directories, and parse existing conversation transcripts from `~/.gemini/antigravity-cli/brain/`.
- **Acceptance Criteria:** Unit tests passing for directory traversal security (prevent path escape), git branch detection, and transcript discovery.
- **Target Files:** `src/server/workspace-service.ts`, `test/workspace-service.test.ts`.

### Task 5: HTTP & WebSocket API Server
- **Objective:** Implement the unified Node.js HTTP + WebSocket server (`src/server/index.ts`) serving REST endpoints (`/api/workspaces`, `/api/conversations`, `/api/status`, `/api/prompt`, `/api/approve`) and WebSocket routes (`/ws/chat` for stream-json streaming, `/ws/pty` for terminal).
- **Acceptance Criteria:** Automated integration tests in `test/server.test.ts` pass, testing REST responses and WebSocket upgrades.
- **Target Files:** `src/server/index.ts`, `test/server.test.ts`.

### Task 6: Touch-First Mobile Frontend (Chat, Approvals, Diffs & Terminal)
- **Objective:** Build the responsive mobile web interface in `src/client/`:
  - Touch-optimized Chat Dashboard (live message feed, syntax-highlighted code blocks, expandable tool execution cards, one-tap approval actions "Allow Once" / "Always Allow" / "Deny", collapsible diff viewer).
  - Terminal tab with xterm.js and a touch accessory bar (Ctrl, Alt, Esc, Tab, arrows, Ctrl+C).
  - Navigation drawer for Workspaces (browse directories, switch folder, git branch badge) and Conversation history.
  - Header with Autonomous vs Supervised toggle, Connection status, and model badge.
  - Speech-to-text / Microphone voice input using Web Speech API + Quick action chips.
- **Acceptance Criteria:** Frontend assets build cleanly with esbuild into `dist/public/`, PWA manifest and service worker are valid, and server serves static assets.
- **Target Files:** `src/client/index.html`, `src/client/app.ts`, `src/client/style.css`, `src/client/sw.ts`, `src/client/manifest.json`.

### Task 7: Android / Termux Integration Launcher & Wake-Lock Script
- **Objective:** Create `agy-mobile.sh` and Termux integration utilities:
  - Acquire `termux-wake-lock` on launch, release on exit trap.
  - Check & auto-install `agy` if missing (`curl -fsSL https://antigravity.google/cli/install.sh | bash`).
  - Check Node.js runtime (`pkg install -y nodejs`).
  - Launch bundled `dist/server.js` on `http://localhost:8080`.
  - Automatically open browser via `termux-open-url` or display access URL and QR code.
  - Post Termux notifications via `termux-notification` when turns complete or require approval.
- **Acceptance Criteria:** `bash -n agy-mobile.sh` passes syntax validation, script executes cleanly in dry-run mode.
- **Target Files:** `agy-mobile.sh`, `README.md`.

### Task 8: End-to-End Build, Bundling & Full Verification
- **Objective:** Bundle complete zero-dependency server (`dist/server.js`) and client assets (`dist/public/`), run full test suite (`npm test`), verify TypeScript (`tsc --noEmit`), and verify standalone execution.
- **Acceptance Criteria:** All tests passing, zero lint/type errors, single-command startup verified.
- **Target Files:** `dist/server.js`, `dist/public/*`.
