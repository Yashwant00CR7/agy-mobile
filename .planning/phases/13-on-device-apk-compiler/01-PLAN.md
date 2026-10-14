# Plan 01: On-Device APK Compiler Engine & Conversational App Builder

## Objective
Build a complete, standalone on-device APK compilation pipeline inside `agy-mobile` so that the agent and user can build, test, auto-fix, install, and share native Android apps directly on their phone without any computer.

---

## Wave 1: Toolchain Preparation & Bootstrapper (REQ-63)

### Task 1.1: Toolchain Asset Scaffolding
- Place ARM64 `libaapt2.so` in `android/app/src/main/jniLibs/arm64-v8a/libaapt2.so`.
- Package DEX-converted `ecj-dex.jar`, `d8-dex.jar`, `apksigner-dex.jar`, lightweight `android.jar`, and `debug.keystore` in `android/app/src/main/assets/compiler/`.
- **Target Files:**
  - `android/app/src/main/jniLibs/arm64-v8a/libaapt2.so`
  - `android/app/src/main/assets/compiler/*`

### Task 1.2: Compiler Bootstrap Manager (`CompilerBootstrapManager.kt`)
- Implement `CompilerBootstrapManager.kt`:
  - Extracts compiler assets from assets to `context.filesDir/compiler/` with SHA-256 caching.
  - Resolves `libaapt2.so` from `context.applicationInfo.nativeLibraryDir`.
  - Exposes tool paths and dynamic DEX classloaders for ECJ, D8, and ApkSigner.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/compiler/CompilerBootstrapManager.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/core/compiler/CompilerBootstrapManagerTest.kt`

---

## Wave 2: In-Process Compilation Engine Core (REQ-64)

### Task 2.1: 5-Stage Compilation Engine (`ApkCompilerEngine.kt`)
- Implement `ApkCompilerEngine.kt` orchestrating:
  1. `libaapt2.so compile` on `res/` directory ➔ `resources.zip`.
  2. `libaapt2.so link` with `AndroidManifest.xml` and `android.jar` ➔ `intermediate.ap_` and `R.java`.
  3. ECJ compilation of all `.java` sources and `R.java` ➔ `.class` files.
  4. D8 dexing of all `.class` files ➔ `classes.dex`.
  5. Injecting `classes.dex` into `intermediate.ap_` with 4-byte ZIP alignment ➔ `unsigned.apk`.
  6. APK signing with `debug.keystore` via `apksigner` ➔ `signed.apk`.
- Real-time stage callbacks: `Stage.RESOURCES`, `Stage.JAVA_COMPILATION`, `Stage.DEXING`, `Stage.PACKAGING`, `Stage.SIGNING`.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/compiler/ApkCompilerEngine.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/core/compiler/CompilerModels.kt`

### Task 2.2: Tracer App Scaffolding & Compilation Verification Test
- Create test fixture representing a sleek Material 3 Calculator app (`CalculatorTracer`).
- Unit test `ApkCompilerEngineTest.kt` executing the full pipeline, asserting exit code 0, generating a signed APK, and inspecting APK contents (valid `classes.dex`, `resources.arsc`, `AndroidManifest.xml`, `META-INF/` signatures).
- **Target Files:**
  - `android/app/src/test/java/com/yashwant/agymobile/core/compiler/ApkCompilerEngineTest.kt`

---

## Wave 3: Agent Tool Integration & Self-Healing Loop (REQ-65)

### Task 3.1: Agent `build_apk` Tool & Shell Bridge
- Implement `build_apk` tool handler in `AgyProcessEngine.kt` and create a standalone script in `binDir/build-apk`.
- Accepts workspace directory, target app name, and debug flag.
- Invokes `ApkCompilerEngine` and formats results as structured JSON:
  `{ "success": true, "apkPath": "...", "appName": "...", "packageName": "...", "sizeBytes": 12345, "buildDurationMs": 2400 }`
  or
  `{ "success": false, "stage": "JAVA_COMPILATION", "errors": [ ... ] }`.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/compiler/BuildApkBridge.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/core/AgyProcessEngine.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/viewmodel/AgyViewModel.kt`

### Task 3.2: Error Diagnostics & Self-Healing Guidance
- Implement error parser extracting: file, line, column, error message from AAPT2 and ECJ outputs.
- Update agent system prompt / rules to automatically detect compilation errors, show status pill ("Build failed: fixing syntax error in MainActivity.java..."), patch files, and recompile (up to 3 retries).
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/core/compiler/CompilerErrorParser.kt`
  - `android/app/src/test/java/com/yashwant/agymobile/core/compiler/CompilerErrorParserTest.kt`

---

## Wave 4: Cyber-Minimalist UI & Native Actions (REQ-66)

### Task 4.1: Interactive `ApkInstallCard.kt` Component
- Build Cyber-Minimalist Jetpack Compose card with:
  - App icon badge, App Title, Package Name, Version Code/Name, APK file size, and build duration.
  - Tactile Action 1: `[Install Now]` (Accent Violet button) launching `PackageInstaller` via `FileProvider`.
  - Tactile Action 2: `[Share]` (Cyan outline button) launching Android `ACTION_SEND` share sheet.
  - Tactile Action 3: `[Save to Downloads]` (Slate icon button) copying APK to `/storage/emulated/0/Download/` with toast.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/ui/components/ApkInstallCard.kt`
  - `android/app/src/main/java/com/yashwant/agymobile/ui/screens/ChatScreen.kt`

### Task 4.2: Android PackageInstaller & Just-In-Time Permissions
- Implement `ApkInstallerHelper.kt`:
  - Check `packageManager.canRequestPackageInstalls()`.
  - If false, open `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` with explanation dialog.
  - If true, launch `Intent.ACTION_VIEW` with `FLAG_GRANT_READ_URI_PERMISSION` and `FileProvider.getUriForFile`.
- Add `REQUEST_INSTALL_PACKAGES` permission in `AndroidManifest.xml`.
- **Target Files:**
  - `android/app/src/main/java/com/yashwant/agymobile/util/ApkInstallerHelper.kt`
  - `android/app/src/main/AndroidManifest.xml`

---

## Wave 5: Full Test Suite, Build & Verification (REQ-67)

### Task 5.1: Unit Test Verification
- Run `./gradlew testDebugUnitTest` and verify 100% pass across all tests.
- **Target Files:** Test reports.

### Task 5.2: Release Build Assembly & Summary
- Run `./gradlew assembleDebug` to produce `android/agy-mobile-debug.apk`.
- Write `SUMMARY.md` documenting verified features, performance benchmarks, and user workflows.
- **Target Files:**
  - `android/agy-mobile-debug.apk`
  - `.planning/phases/13-on-device-apk-compiler/SUMMARY.md`
