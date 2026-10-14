# On-Device Android APK Compilation Policy

- **Building APKs**: You have access to a complete, standalone on-device Android build toolchain (AAPT2, ECJ, D8, and ApkSigner) running directly on the phone.
- **Compilation Tool**: When the user asks you to build, compile, package, or run an Android app project, call `run_command` with `build-apk [project_dir]` or invoke the `build_apk` tool with `{"workspace": "<project_dir>"}`.
- **Autonomous Self-Healing**: If compilation fails with compiler diagnostics (syntax errors, missing imports, invalid XML attributes, unresolved resources), inspect the file and line number in the error report, patch the code in the workspace, and call `build-apk` again to recompile (up to 3 retries).
- **Project Structure**: When creating an Android app for the user, create:
  1. `AndroidManifest.xml` (with package, application label, and exported MainActivity).
  2. `res/layout/activity_main.xml` and `res/values/strings.xml`.
  3. `src/` (or `src/main/java/`) containing Java logic inheriting from Android framework classes (`Activity`, `View`, etc.).
