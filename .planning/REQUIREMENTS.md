# Requirements: Mobile Bash Runtime, Storage Access & GSD Bundle

- [x] **REQ-01**: Bundle a statically-linked ARM64 `bash` binary as `libbash.so` under `android/app/src/main/jniLibs/arm64-v8a/` and ensure it is packaged into the APK native library directory.
- [x] **REQ-02**: Update `BootstrapManager.kt` to resolve the native shell executable (`libbash.so` or `/system/bin/sh`), export `SHELL` in the environment, configure `PATH`, and create local symlinks (`bin/bash`, `bin/sh`).
- [x] **REQ-03**: Configure `AndroidManifest.xml` with `MANAGE_EXTERNAL_STORAGE` and `READ_EXTERNAL_STORAGE` permissions, and implement a Just-in-Time All Files Access permission dialog in the UI linking to Android system settings.
- [x] **REQ-04**: Bundle the complete set of GSD Core skills into `android/app/src/main/assets/skills/` and extend `BootstrapManager.kt` to extract them to `~/.gemini/config/skills/` on app startup.
- [x] **REQ-05**: Ensure `/gsd-*` commands and bash execution are handled cleanly by `AgyProcessEngine` and the chat interface without UI clutter.
- [x] **REQ-06**: Verify all unit tests pass with `./gradlew test` and assemble a production debug APK with `./gradlew assembleDebug`.
