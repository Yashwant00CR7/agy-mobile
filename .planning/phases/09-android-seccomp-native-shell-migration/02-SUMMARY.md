# Phase 09 Summary: Android Seccomp Resolution & Native Bionic Shell Migration

**Phase:** 09-android-seccomp-native-shell-migration  
**Status:** Completed  
**Release Target:** v1.9.0  
**Artifact:** `android/agy-mobile-debug.apk` (74MB, libbash.so removed)

---

## 1. Problem & Root Cause

When running commands like `ls` or `find` on Android device folders (e.g. `/storage/emulated/0/Download/Notes`), the command execution tool crashed with:
`exit code 159 (SIGSYS: Bad System Call)`

### Root Cause Analysis:
1. **Linux glibc vs Android Seccomp:** The project previously bundled a static ARM64 `libbash.so` compiled with desktop Linux glibc.
2. **Android App Sandbox Policy:** Untrusted Android apps run in a zygote-installed seccomp-bpf sandbox (`/system/etc/seccomp_policy/app.policy`). Glibc's static initialization and execution paths execute system calls not in Android's app whitelist (such as `clone3` or `rseq`), which the kernel intercepts and terminates with `SIGSYS` (Signal 31).
3. **Exit Code Calculation:** In Linux/POSIX, a process killed by a signal returns `128 + signal` -> `128 + 31 = 159`.
4. **Android Native Shell (`/system/bin/sh`):** Android ROMs ship with `/system/bin/sh` (mksh or toybox sh) linked against Android Bionic libc. It never violates Android's seccomp policy and has full access to standard POSIX tools (`ls`, `cat`, `grep`, `find`, `mkdir`, `cp`, `mv`, `rm`).

---

## 2. Solution & Key Accomplishments

### Purged `libbash.so` & Standardized on Android Bionic Shell (REQ-43)
- Deleted `android/app/src/main/jniLibs/arm64-v8a/libbash.so` (`git rm`), permanently eliminating glibc seccomp crashes and removing 2.4MB of unneeded binary bloat.
- Updated `BootstrapManager.kt`:
  - `getShellExecutable()` resolves directly to Android's built-in `/system/bin/sh` (with `/bin/sh` fallback for macOS/Linux hosts).
  - `setupShellLinks()` links both `binDir/bash` and `binDir/sh` to the native `/system/bin/sh`.
  - `createShellWrapperScript()` invokes `/system/bin/sh "$@"`.

### Environment & Rule Hardening (REQ-44)
- Updated `BootstrapManager.kt` environment map to export `SHELL=/system/bin/sh`.
- Updated prompt policy files (`rules/system-android.md`, `rules/android-runtime.md`) explicitly notifying the agent that `/system/bin/sh` is the native Bionic shell, linked to `bash` and `sh`, and capable of executing all standard POSIX commands (`ls`, `find`, etc.).

### Drawer Settings & Diagnostic Inspector Updates (REQ-45)
- Updated `ChatGptDrawer.kt` About dialog to display:
  `• /system/bin/sh (Android Bionic Shell)`
- Updated the Engine & Shell Logs header and "Copy All" clipboard export to identify the native shell as `/system/bin/sh (Android Bionic Shell)`.

### Test Suite & Release Build (REQ-46)
- Updated `BootstrapManagerTest.kt` to verify Bionic shell resolution, symlink creation, and self-healing.
- 100% unit tests passing (`./gradlew test`).
- Parallel code review subagents (Standards & Spec) passed with 0 blockers.
- Built clean release APK: `android/agy-mobile-debug.apk` without `libbash.so`.

---

## 3. Commit History

1. `177ef1e` - `chore(gsd): initialize phase 09 seccomp resolution and bionic shell migration plan`
2. `8c75715` - `fix(shell): purge libbash.so and migrate to native android bionic system shell to fix seccomp sigsys 159`
