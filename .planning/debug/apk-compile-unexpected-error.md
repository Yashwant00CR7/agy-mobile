---
status: resolved
trigger: "still the apk is failing to compile and throwing unexpecteed error , please use the gsd plugin and fix it for me"
created: 2026-10-15
updated: 2026-10-15
symptoms:
  expected: "On-device compilation should successfully produce a signed APK without errors."
  actual: "Compilation fails and displays 'Unexpected compilation error'."
  error: "Unexpected compilation error: <exception/throwable details>"
  timeline: "Occurs after crash-prevention changes when user triggers compile on device"
  repro: "Trigger APK compilation in agy-mobile app"
---

## Root Causes Identified
1. **Monolithic Catch-All Exception Handler in ApkCompilerEngine**:
   - `ApkCompilerEngine.kt` wrapped all compilation stages in a single outer `try-catch (t: Throwable)` block.
   - Any failure in any stage was caught and collapsed into `"Unexpected compilation error: ${t.message ?: t.javaClass.simpleName}"` with `failedStage = CompilationStage.FAILED`, suppressing stage context.
2. **ApkSigner Missing MinSdkVersion**:
   - `ApkSigner.Builder` omitted `.setMinSdkVersion(...)`.
   - When the input project manifest lacked explicit `<uses-sdk>` or when intermediate APK metadata was queried, `apksig:8.6.0` threw `MinSdkVersionException: Failed to determine APK's minimum supported platform version. Use --min-sdk-version to override`.
3. **ZIP STORED Entry Compression Corruption in `packageApk`**:
   - `packageApk` created new `ZipEntry` instances without preserving `ZipEntry.STORED` method, CRC, and uncompressed size for `resources.arsc`.
   - Recompressing `resources.arsc` with DEFLATE broke Android's memory-mapping expectations and signature verification.
4. **Missing Min and Target SDK Flags in `aapt2 link`**:
   - `aapt2 link` omitted `--min-sdk-version` and `--target-sdk-version`, generating incomplete binary manifests.
5. **Native Library Extraction on Android 10+**:
   - `android:extractNativeLibs="true"` was missing in the application tag of `AndroidManifest.xml`.
   - `libaapt2.so` in `nativeLibraryDir` needed explicit `setExecutable(true, false)` and `setReadable(true, false)` in `CompilerBootstrapManager`.
6. **Opaque UI Diagnostics in ApkInstallCard**:
   - UI omitted `result.rawOutput` when no structured errors were returned, obscuring root cause stack traces from both the user and the AI fix prompt.

## Fixes Implemented
1. **Stage Isolation in `ApkCompilerEngine.kt`**:
   - Wrapped each compilation phase (`RESOURCES_COMPILE`, `RESOURCES_LINK`, `JAVA_COMPILATION`, `DEXING`, `PACKAGING`, `SIGNING`) in isolated error handlers tracking `currentStage`.
   - Extracted `minSdkVersion` and `targetSdkVersion` in `parseManifest` (defaults 26 and 34).
   - Injected `--min-sdk-version` and `--target-sdk-version` into `aapt2 link`.
   - Configured `setMinSdkVersion(minSdk.coerceAtLeast(26))` on `ApkSigner.Builder`.
   - Preserved `ZipEntry.STORED`, CRC, size, and compressed size in `packageApk`.
2. **Native Executable Permissions in `CompilerBootstrapManager.kt`**:
   - Added `setExecutable(true, false)` and `setReadable(true, false)` calls on `libaapt2.so` in `nativeLibraryDir`.
3. **Application Manifest in `AndroidManifest.xml`**:
   - Added `android:extractNativeLibs="true"` to ensure `libaapt2.so` is unpacked to `nativeLibraryDir`.
4. **Diagnostic Error Display in `ApkInstallCard.kt`**:
   - Display `result.rawOutput` snippet in UI when errors list is empty and build fails.
   - Passed `rawOutput` (stack trace) to `onFixWithAi` prompt for intelligent AI repairs.
5. **Unit Tests Added in `ApkCompilerEngineTest.kt`**:
   - Added `testParseManifestSdkDefaultsAndCustom` verifying default and explicit SDK parsing.
   - Added `testPackageApkPreservesStoredEntries` verifying `STORED` method and CRC preservation for `resources.arsc`.

## Verification
- Unit test suite (`./gradlew testDebugUnitTest`) passed (100% green).
- Full assemble build (`./gradlew assembleDebug`) generated 125 MB release artifact `agy-mobile-debug.apk`.
- Updated `agy-mobile-debug.apk` in Google Drive (`1xKv4W1AX0cw9wSmXc-EkC3v_VknrVgYC`).
