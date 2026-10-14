# Phase 13 Context: On-Device APK Compiler Engine & Conversational App Builder

## Problem & User Intent
The user wants `agy-mobile` to compile complete, valid, signed Android APKs directly inside the phone itself without relying on any external PC or cloud compiler.
The user experience must be purely conversational: when the user asks `agy` in chat to build an Android app, `agy` writes the code into the workspace, compiles the project into an APK on the device in the background, and drops an interactive Cyber-Minimalist `ApkInstallCard` directly in chat with 1-tap installation (`[Install Now]`), Android system sharing (`[Share]`), and local storage backup (`[Save to Downloads]`). If compilation errors occur, `agy` automatically enters a transparent self-healing loop with live status pills, reading compiler diagnostics and patching code autonomously (up to 3 retries).

## Mental Model & Core Abstractions
1. **On-Device 5-Stage Compilation Pipeline (`ApkCompilerEngine`)**:
   - Stage 1: Resource compilation & linking (`libaapt2.so` via `ProcessBuilder`) against `android.jar`, outputting `resources.ap_` and `R.java`.
   - Stage 2: Java bytecode compilation (`ecj` Eclipse Java Compiler loaded in-process via DEX ClassLoader) compiling `.java` sources and `R.java` into `.class` files.
   - Stage 3: Dalvik bytecode generation (`d8` loaded in-process via DEX ClassLoader) compiling `.class` files into `classes.dex`.
   - Stage 4: Container assembly & ZIP alignment (`zip` / Java Zip stream) injecting `classes.dex` into `resources.ap_` with 4-byte alignment.
   - Stage 5: Cryptographic APK signing (`apksigner` loaded in-process) signing the APK with an embedded debug keystore (`debug.keystore`).
2. **Toolchain Asset Management (`CompilerBootstrapManager`)**:
   - Native ARM64 `libaapt2.so` placed in `jniLibs/arm64-v8a/` to satisfy Android 10–15 W^X execution rules.
   - Dexed standalone jars (`ecj-dex.jar`, `d8-dex.jar`, `apksigner-dex.jar`) and `android.jar` bundled in `assets/compiler/`, extracted to `context.filesDir/compiler/` with SHA-256 verification.
3. **Conversational Feedback & Agent Tool Seam**:
   - Tool `build_apk`: Invoked by `agy` or shell wrapper. Returns structured JSON containing success/failure, output APK path, APK size, duration, or detailed compiler error diagnostics.
   - Live status pill: Displays active build stages or self-healing error recovery.
4. **Interactive UI Card (`ApkInstallCard.kt`)**:
   - Cyber-Minimalist card rendered inline in chat.
   - Shows app title, package ID, version, APK size, and compilation duration.
   - Direct integration with Android `PackageInstaller` via `FileProvider` (`content://com.yashwant.agymobile.fileprovider/...`), requesting `REQUEST_INSTALL_PACKAGES` permission just-in-time if disabled.

## Locked Architectural Decisions
- **Execution Mechanism**: `libaapt2.so` runs via `ProcessBuilder` from `context.applicationInfo.nativeLibraryDir`. All Java build tools (`ecj`, `d8`, `apksigner`) run in-process on ART via DEX classloading for minimal RAM and instant execution.
- **Offline Guarantee**: All necessary compiler assets (`android.jar`, toolchain jars, keystore) are 100% pre-bundled offline. No network required.
- **Memory & Process Safety**: Runs inside `AgyAgentService` with `PARTIAL_WAKE_LOCK` to ensure Android's Low Memory Killer never terminates long compilations.
- **Tracer App**: Sleek native Calculator app project with XML layout, drawables, and Java business logic.

## Verification & Backpressure Seams
- Unit Tests: `./gradlew testDebugUnitTest`
- Pipeline Verification: `ApkCompilerEngineTest` verifying end-to-end compilation of the tracer project into a valid, signed APK.
- APK Assembly: `./gradlew assembleDebug` producing `android/agy-mobile-debug.apk`.
