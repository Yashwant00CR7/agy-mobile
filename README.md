# agy-mobile 📱✨

> **Google Antigravity (`agy`) Mobile Web App & Daemon for Android Termux**  
> Run Antigravity autonomously or interactively directly inside your Android phone with a touch-first PWA interface.

---

## Overview

`agy-mobile` brings Google's Antigravity CLI (`agy`) to Android phones via Termux with a mobile-optimized Progressive Web App (PWA) interface.

Instead of fighting with an on-screen touchscreen keyboard in raw terminal sessions, `agy-mobile` provides:
- **Touch-First Chat & Dashboard**: Visual cards for streaming turns, collapsible thoughts, and formatted markdown code blocks.
- **One-Tap Tool Approvals**: Intercepts tool calls (e.g. bash commands, file modifications) with touch-friendly **[Approve]** and **[Deny]** buttons, accompanied by phone vibration and notification alerts.
- **Autonomous Mode Toggle**: Switch seamlessly between **Supervised Mode** (manual touch approvals) and **Autonomous Mode** (`--dangerously-skip-permissions` for hands-free background execution while your phone is locked).
- **Embedded Web Terminal**: Full interactive terminal view with a mobile touch accessory bar (`ESC`, `TAB`, `CTRL`, `ALT`, `▲`, `▼`, `◀`, `▶`, `^C`) to run commands directly.
- **Voice / Speech-to-Text Input**: Dictate prompts directly using your phone's microphone via the Web Speech API.
- **Multi-Workspace & History Drawer**: Switch between working directories, view git status/branches, and resume past `agy` conversations saved in `~/.gemini/antigravity-cli/brain/`.
- **Termux Wake-Lock Integration**: Automatically acquires `termux-wake-lock` to ensure Android doesn't kill your background coding runs when the screen turns off.
- **Zero-Dependency Runtime Bundle**: The server and client are pre-bundled into a single standalone JavaScript file. Running on Termux requires only Node.js (`pkg install nodejs`), with zero C-compilers or heavy npm installs on the phone.

---

## Quick Start on Android (Termux)

### 1. Install Dependencies in Termux
```bash
pkg update && pkg install -y git nodejs termux-api
```

### 2. Install Antigravity CLI (if not already installed)
```bash
curl -fsSL https://antigravity.google/cli/install.sh | bash
export PATH="$HOME/.local/bin:$PATH"
```

### 3. Clone & Run `agy-mobile`
```bash
git clone git@github-personal:Yashwant00CR7/agy-mobile.git
cd agy-mobile
./agy-mobile.sh
```

### 4. Install as a Mobile App (PWA)
1. Open your browser on Android (e.g., Google Chrome) to `http://localhost:8080`.
2. Tap the Chrome menu (`⋮`) and select **"Add to Home Screen"** / **"Install App"**.
3. Launch `agy-mobile` directly from your phone's home screen for a fullscreen, standalone app experience!

---

## Command Line Options

```bash
./agy-mobile.sh [options]

Options:
  -p, --port <port>   Port to listen on (default: 8080)
  -h, --host <host>   Host to bind to (default: 0.0.0.0)
      --no-open       Do not auto-open browser in Termux
      --dry-run       Validate environment setup and exit
      --help          Display help message
```

---

## Development & Verification

Built with TypeScript and bundled using `esbuild`.

```bash
# Run tests (Node 22 native test runner)
npm test

# Typecheck
npm run typecheck

# Build standalone distribution
npm run build
```

---

## Architecture

```
                    ┌──────────────────────────┐
                    │ Android Mobile (Chrome)  │
                    │   Installable PWA App    │
                    └────────────┬─────────────┘
                                 │
           HTTP / Static Assets  │  WebSockets (/ws/chat & /ws/pty)
                                 │
                    ┌────────────▼─────────────┐
                    │   agy-mobile Daemon      │
                    │     (Node.js Server)     │
                    │   Running inside Termux  │
                    └────────────┬─────────────┘
                                 │
            ┌────────────────────┴────────────────────┐
            ▼                                         ▼
  ┌───────────────────┐                     ┌───────────────────┐
  │ AgyProcessManager │                     │  TerminalBridge   │
  │   (stream-json)   │                     │  (Interactive PTY)│
  └─────────┬─────────┘                     └─────────┬─────────┘
            │                                         │
            ▼                                         ▼
  ┌───────────────────┐                     ┌───────────────────┐
  │  Antigravity CLI  │                     │   Termux Shell    │
  │    (agy binary)   │                     │      (bash)       │
  └───────────────────┘                     └───────────────────┘
```

---

## License

MIT © Yashwant Kanagaraj
