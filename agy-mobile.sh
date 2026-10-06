#!/usr/bin/env bash
# ==============================================================================
# agy-mobile — Antigravity Mobile Launcher for Android Termux
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PORT="${PORT:-8080}"
HOST="${HOST:-0.0.0.0}"
AUTO_OPEN=true
DRY_RUN=false
ACQUIRED_WAKE_LOCK=false

# ANSI styling
BOLD='\033[1m'
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

cleanup() {
  if [ "$ACQUIRED_WAKE_LOCK" = true ] && command -v termux-wake-unlock >/dev/null 2>&1; then
    termux-wake-unlock
    echo -e "\n${YELLOW}Released Termux wake-lock.${NC}"
  fi
}

trap cleanup EXIT INT TERM

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    --port|-p)
      PORT="$2"
      shift 2
      ;;
    --host|-h)
      HOST="$2"
      shift 2
      ;;
    --no-open)
      AUTO_OPEN=false
      shift
      ;;
    --dry-run)
      DRY_RUN=true
      shift
      ;;
    --help)
      echo "Usage: ./agy-mobile.sh [options]"
      echo "Options:"
      echo "  -p, --port <port>   Port to listen on (default: 8080)"
      echo "  -h, --host <host>   Host to bind to (default: 0.0.0.0)"
      echo "      --no-open       Do not auto-open browser"
      echo "      --dry-run       Validate environment and exit"
      echo "      --help          Show this help message"
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

echo -e "${CYAN}${BOLD}"
echo "   ___    ____  __  __  __  ___  ____ ___ _     _____ "
echo "  / _ \  / ___|\ \/ / |  \/  |/ _ \| __ )_ _| |   | ____|"
echo " / /_\ \| |  _  \  /  | |\/| | | | |  _ \| || |   |  _|  "
echo "/ /   \ \ |_| | / /   | |  | | |_| | |_) | || |___| |___ "
echo "\_/    \_\____|/_/    |_|  |_|\___/|____/___|_____|_____|"
echo -e "${NC}"
echo -e "${BOLD}Google Antigravity Mobile Controller for Termux${NC}\n"

# 1. Termux Environment & Wake-lock
if [ -n "$TERMUX_VERSION" ] || [ -d "/data/data/com.termux" ]; then
  echo -e "${GREEN}✓ Termux environment detected.${NC}"
  if command -v termux-wake-lock >/dev/null 2>&1; then
    termux-wake-lock
    ACQUIRED_WAKE_LOCK=true
    echo -e "${GREEN}✓ Acquired Termux wake-lock (prevents Android background sleep).${NC}"
  else
    echo -e "${YELLOW}! termux-wake-lock not found. For uninterrupted background runs, run: pkg install termux-api${NC}"
  fi
else
  echo -e "${CYAN}i Running in standard POSIX environment.${NC}"
fi

# 2. Check Node.js
if ! command -v node >/dev/null 2>&1; then
  echo -e "${YELLOW}! Node.js is not installed.${NC}"
  if command -v pkg >/dev/null 2>&1; then
    echo -e "${CYAN}Installing Node.js via pkg...${NC}"
    pkg install -y nodejs
  else
    echo -e "${RED}Error: node is required. Please install Node.js (>= 20).${NC}"
    exit 1
  fi
fi
NODE_VER=$(node -v)
echo -e "${GREEN}✓ Node.js found (${NODE_VER}).${NC}"

# 3. Check agy binary
AGY_BIN=""
if command -v agy >/dev/null 2>&1; then
  AGY_BIN="$(command -v agy)"
elif [ -f "$HOME/.local/bin/agy" ]; then
  AGY_BIN="$HOME/.local/bin/agy"
elif [ -f "/data/data/com.termux/files/home/.local/bin/agy" ]; then
  AGY_BIN="/data/data/com.termux/files/home/.local/bin/agy"
fi

if [ -n "$AGY_BIN" ]; then
  echo -e "${GREEN}✓ Antigravity CLI found: ${AGY_BIN}${NC}"
else
  echo -e "${YELLOW}! Antigravity CLI (agy) was not found in PATH or ~/.local/bin/agy.${NC}"
  echo -e "${CYAN}To install agy in Termux, run:${NC}"
  echo -e "  curl -fsSL https://antigravity.google/cli/install.sh | bash"
  echo -e "${YELLOW}Starting server anyway (terminal shell mode will remain available).${NC}"
fi

# 4. Check bundled server
SERVER_BUNDLE="$SCRIPT_DIR/dist/server.js"
if [ ! -f "$SERVER_BUNDLE" ]; then
  echo -e "${YELLOW}! Server bundle not found at dist/server.js. Building...${NC}"
  (cd "$SCRIPT_DIR" && npm run build)
fi

if [ "$DRY_RUN" = true ]; then
  echo -e "\n${GREEN}${BOLD}✓ Dry-run completed successfully! Environment is ready.${NC}"
  exit 0
fi

# 5. Launch Server
echo -e "\n${BOLD}${GREEN}Starting agy-mobile on http://${HOST}:${PORT}${NC}"
echo -e "Add to Home Screen in Android Chrome for fullscreen PWA app experience."
echo -e "Press ${BOLD}Ctrl+C${NC} to stop.\n"

if [ "$AUTO_OPEN" = true ] && command -v termux-open-url >/dev/null 2>&1; then
  (sleep 1 && termux-open-url "http://localhost:${PORT}") &
fi

exec node "$SERVER_BUNDLE" --port "$PORT" --host "$HOST"
