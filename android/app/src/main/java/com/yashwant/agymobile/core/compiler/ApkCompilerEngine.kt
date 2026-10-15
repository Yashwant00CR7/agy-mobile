package com.yashwant.agymobile.core.compiler

import com.android.apksig.ApkSigner
import com.android.tools.r8.D8
import com.android.tools.r8.D8Command
import com.android.tools.r8.OutputMode
import org.eclipse.jdt.internal.compiler.batch.Main
import java.io.*
import java.nio.file.Path
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ApkCompilerEngine(
    private val bootstrapManager: CompilerBootstrapManager
) {

    fun compileProject(
        projectDir: File,
        outputDir: File? = null,
        onProgress: ((CompilationProgress) -> Unit)? = null
    ): CompilerResult {
        val startTime = System.currentTimeMillis()

        onProgress?.invoke(CompilationProgress(CompilationStage.INIT, "Initializing compiler environment...", 5))

        // 1. Verify environment and prerequisites
        val aapt2 = bootstrapManager.getAapt2Executable()
            ?: return CompilerResult(
                success = false,
                failedStage = CompilationStage.INIT,
                errorMessage = "aapt2 binary not found. Ensure libaapt2.so is available in app binaries.",
                durationMs = System.currentTimeMillis() - startTime
            )

        val androidJar = bootstrapManager.androidJarFile
        if (!androidJar.exists() || androidJar.length() == 0L) {
            return CompilerResult(
                success = false,
                failedStage = CompilationStage.INIT,
                errorMessage = "android.jar platform library missing from compiler storage.",
                durationMs = System.currentTimeMillis() - startTime
            )
        }

        val manifestFile = resolveManifestFile(projectDir)
            ?: return CompilerResult(
                success = false,
                failedStage = CompilationStage.INIT,
                errorMessage = "AndroidManifest.xml not found in project directory.",
                durationMs = System.currentTimeMillis() - startTime
            )

        val manifestInfo = parseManifest(manifestFile)
        val resDir = resolveResDir(projectDir)

        val buildDir = File(projectDir, "build").apply { mkdirs() }
        val genDir = File(buildDir, "gen").apply { mkdirs() }
        val classesDir = File(buildDir, "classes").apply { mkdirs() }
        val dexDir = File(buildDir, "dex").apply { mkdirs() }
        val intermediateApk = File(buildDir, "intermediate.ap_")
        val unsignedApk = File(buildDir, "unsigned.apk")
        val finalOutputDir = outputDir ?: File(buildDir, "outputs")
        finalOutputDir.mkdirs()

        val cleanAppName = manifestInfo.appName.ifBlank { "App" }.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val signedApk = File(finalOutputDir, "$cleanAppName-debug.apk")

        var currentStage = CompilationStage.INIT
        try {
            // Stage 1: Resource Compile (if resources exist)
            currentStage = CompilationStage.RESOURCES_COMPILE
            val resourcesZip = File(buildDir, "resources.zip")
            if (resDir != null && resDir.exists() && resDir.listFiles()?.isNotEmpty() == true) {
                onProgress?.invoke(CompilationProgress(CompilationStage.RESOURCES_COMPILE, "Compiling XML resources...", 20))
                val compileCmd = listOf(
                    aapt2.absolutePath,
                    "compile",
                    "--dir", resDir.absolutePath,
                    "-o", resourcesZip.absolutePath
                )
                val compileProc = try {
                    ProcessBuilder(compileCmd).redirectErrorStream(true).apply {
                        val tmpDir = File(bootstrapManager.compilerDir, "tmp").apply { mkdirs() }
                        environment()["TMPDIR"] = tmpDir.absolutePath
                        aapt2.parentFile?.let { environment()["LD_LIBRARY_PATH"] = it.absolutePath }
                    }.start()
                } catch (t: Throwable) {
                    return CompilerResult(
                        success = false,
                        failedStage = CompilationStage.RESOURCES_COMPILE,
                        errorMessage = "Failed to start AAPT2 compiler: ${t.message ?: t.javaClass.simpleName}",
                        rawOutput = t.stackTraceToString(),
                        durationMs = System.currentTimeMillis() - startTime
                    )
                }
                val compileOutput = compileProc.inputStream.bufferedReader().readText()
                val compileExit = compileProc.waitFor()
                if (compileExit != 0) {
                    val errors = CompilerErrorParser.parseErrors(CompilationStage.RESOURCES_COMPILE, compileOutput)
                    return CompilerResult(
                        success = false,
                        failedStage = CompilationStage.RESOURCES_COMPILE,
                        errorMessage = "Resource compilation failed: ${errors.firstOrNull()?.message ?: compileOutput.take(150)}",
                        errors = errors,
                        rawOutput = compileOutput,
                        durationMs = System.currentTimeMillis() - startTime
                    )
                }
            }

            // Stage 2: Resource Link
            currentStage = CompilationStage.RESOURCES_LINK
            onProgress?.invoke(CompilationProgress(CompilationStage.RESOURCES_LINK, "Linking resources & generating R.java...", 35))
            val linkCmd = mutableListOf(
                aapt2.absolutePath,
                "link",
                "-o", intermediateApk.absolutePath,
                "-I", androidJar.absolutePath,
                "--manifest", manifestFile.absolutePath,
                "--java", genDir.absolutePath,
                "--auto-add-overlay",
                "--min-sdk-version", manifestInfo.minSdkVersion.toString(),
                "--target-sdk-version", manifestInfo.targetSdkVersion.toString()
            )
            if (resourcesZip.exists()) {
                linkCmd.add("-R")
                linkCmd.add(resourcesZip.absolutePath)
            }

            val linkProc = try {
                ProcessBuilder(linkCmd).redirectErrorStream(true).apply {
                    val tmpDir = File(bootstrapManager.compilerDir, "tmp").apply { mkdirs() }
                    environment()["TMPDIR"] = tmpDir.absolutePath
                    aapt2.parentFile?.let { environment()["LD_LIBRARY_PATH"] = it.absolutePath }
                }.start()
            } catch (t: Throwable) {
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.RESOURCES_LINK,
                    errorMessage = "Failed to start AAPT2 linker: ${t.message ?: t.javaClass.simpleName}",
                    rawOutput = t.stackTraceToString(),
                    durationMs = System.currentTimeMillis() - startTime
                )
            }
            val linkOutput = linkProc.inputStream.bufferedReader().readText()
            val linkExit = linkProc.waitFor()
            if (linkExit != 0) {
                val errors = CompilerErrorParser.parseErrors(CompilationStage.RESOURCES_LINK, linkOutput)
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.RESOURCES_LINK,
                    errorMessage = "Resource linking failed: ${errors.firstOrNull()?.message ?: linkOutput.take(150)}",
                    errors = errors,
                    rawOutput = linkOutput,
                    durationMs = System.currentTimeMillis() - startTime
                )
            }

            // Stage 3: Java Source Compilation via ECJ
            currentStage = CompilationStage.JAVA_COMPILATION
            onProgress?.invoke(CompilationProgress(CompilationStage.JAVA_COMPILATION, "Compiling Java sources...", 50))
            val javaFiles = mutableListOf<File>()
            collectJavaFiles(resolveSrcDir(projectDir), javaFiles)
            collectJavaFiles(genDir, javaFiles)

            if (javaFiles.isEmpty()) {
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.JAVA_COMPILATION,
                    errorMessage = "No Java source files found in project.",
                    durationMs = System.currentTimeMillis() - startTime
                )
            }

            val ecjArgs = mutableListOf(
                "-1.8",
                "-cp", androidJar.absolutePath,
                "-d", classesDir.absolutePath,
                "-nowarn"
            )
            javaFiles.forEach { ecjArgs.add(it.absolutePath) }

            val outWriter = StringWriter()
            val errWriter = StringWriter()
            val ecjSuccess = try {
                val ecjMain = Main(
                    PrintWriter(outWriter),
                    PrintWriter(errWriter),
                    false,
                    null,
                    null
                )
                ecjMain.compile(ecjArgs.toTypedArray())
            } catch (t: Throwable) {
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.JAVA_COMPILATION,
                    errorMessage = "ECJ compilation exception: ${t.message ?: t.javaClass.simpleName}",
                    rawOutput = t.stackTraceToString(),
                    durationMs = System.currentTimeMillis() - startTime
                )
            }
            val ecjErrors = errWriter.toString()

            if (!ecjSuccess) {
                val errors = CompilerErrorParser.parseErrors(CompilationStage.JAVA_COMPILATION, ecjErrors)
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.JAVA_COMPILATION,
                    errorMessage = "Java compilation failed: ${errors.firstOrNull()?.message ?: ecjErrors.take(150)}",
                    errors = errors,
                    rawOutput = ecjErrors,
                    durationMs = System.currentTimeMillis() - startTime
                )
            }

            // Stage 4: D8 Dexing
            currentStage = CompilationStage.DEXING
            onProgress?.invoke(CompilationProgress(CompilationStage.DEXING, "Converting bytecode to Dalvik DEX...", 70))
            System.gc()
            val classFiles = mutableListOf<Path>()
            collectClassFiles(classesDir, classFiles)

            if (classFiles.isEmpty()) {
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.DEXING,
                    errorMessage = "No class files generated by Java compiler.",
                    durationMs = System.currentTimeMillis() - startTime
                )
            }

            val targetMinSdk = manifestInfo.minSdkVersion.coerceAtLeast(26)
            try {
                val d8Command = D8Command.builder()
                    .setOutput(dexDir.toPath(), OutputMode.DexIndexed)
                    .setMinApiLevel(targetMinSdk)
                    .setMode(com.android.tools.r8.CompilationMode.DEBUG)
                    .addLibraryFiles(androidJar.toPath())
                    .addProgramFiles(classFiles)
                    .build()

                D8.run(d8Command)
            } catch (t: Throwable) {
                // If D8 runs low on memory or fails parsing heavy android.jar, retry in lightweight direct mode
                System.gc()
                try {
                    val d8Fallback = D8Command.builder()
                        .setOutput(dexDir.toPath(), OutputMode.DexIndexed)
                        .setMinApiLevel(targetMinSdk)
                        .setMode(com.android.tools.r8.CompilationMode.DEBUG)
                        .setDisableDesugaring(true)
                        .addProgramFiles(classFiles)
                        .build()

                    D8.run(d8Fallback)
                } catch (fallbackError: Throwable) {
                    return CompilerResult(
                        success = false,
                        failedStage = CompilationStage.DEXING,
                        errorMessage = "D8 dexing failed: ${fallbackError.message ?: fallbackError.javaClass.simpleName}",
                        rawOutput = fallbackError.stackTraceToString(),
                        durationMs = System.currentTimeMillis() - startTime
                    )
                }
            }

            val classesDex = File(dexDir, "classes.dex")
            if (!classesDex.exists()) {
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.DEXING,
                    errorMessage = "D8 failed to generate classes.dex.",
                    durationMs = System.currentTimeMillis() - startTime
                )
            }

            // Stage 5: Package into Unsigned APK
            currentStage = CompilationStage.PACKAGING
            onProgress?.invoke(CompilationProgress(CompilationStage.PACKAGING, "Packaging APK container...", 85))
            try {
                packageApk(intermediateApk, dexDir, unsignedApk)
            } catch (t: Throwable) {
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.PACKAGING,
                    errorMessage = "APK packaging failed: ${t.message ?: t.javaClass.simpleName}",
                    rawOutput = t.stackTraceToString(),
                    durationMs = System.currentTimeMillis() - startTime
                )
            }

            if (!unsignedApk.exists() || unsignedApk.length() == 0L) {
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.PACKAGING,
                    errorMessage = "Packaging produced an empty or missing unsigned APK.",
                    durationMs = System.currentTimeMillis() - startTime
                )
            }

            // Stage 6: Sign APK with ApkSigner
            currentStage = CompilationStage.SIGNING
            onProgress?.invoke(CompilationProgress(CompilationStage.SIGNING, "Signing APK with debug keystore...", 95))
            val keystoreFile = bootstrapManager.debugKeystoreFile
            if (!keystoreFile.exists() || keystoreFile.length() == 0L) {
                generateDebugKeystore(keystoreFile)
            }
            if (!keystoreFile.exists() || keystoreFile.length() == 0L) {
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.SIGNING,
                    errorMessage = "Debug keystore missing or empty at ${keystoreFile.absolutePath}",
                    durationMs = System.currentTimeMillis() - startTime
                )
            }

            try {
                signApk(unsignedApk, signedApk, keystoreFile, manifestInfo.minSdkVersion.coerceAtLeast(26))
            } catch (t: Throwable) {
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.SIGNING,
                    errorMessage = "APK signing failed: ${t.message ?: t.javaClass.simpleName}",
                    rawOutput = t.stackTraceToString(),
                    durationMs = System.currentTimeMillis() - startTime
                )
            }

            if (!signedApk.exists() || signedApk.length() == 0L) {
                return CompilerResult(
                    success = false,
                    failedStage = CompilationStage.SIGNING,
                    errorMessage = "Signing produced an empty or missing signed APK.",
                    durationMs = System.currentTimeMillis() - startTime
                )
            }

            val duration = System.currentTimeMillis() - startTime
            onProgress?.invoke(CompilationProgress(CompilationStage.COMPLETED, "APK compilation complete!", 100))

            return CompilerResult(
                success = true,
                apkFile = signedApk,
                appName = manifestInfo.appName,
                packageName = manifestInfo.packageName,
                versionCode = manifestInfo.versionCode,
                versionName = manifestInfo.versionName,
                sizeBytes = signedApk.length(),
                durationMs = duration
            )

        } catch (t: Throwable) {
            return CompilerResult(
                success = false,
                failedStage = currentStage,
                errorMessage = "Compilation failed at stage ${currentStage.name}: ${t.message ?: t.javaClass.simpleName}",
                rawOutput = t.stackTraceToString(),
                durationMs = System.currentTimeMillis() - startTime
            )
        }
    }

    private fun resolveManifestFile(projectDir: File): File? {
        val candidates = listOf(
            File(projectDir, "AndroidManifest.xml"),
            File(projectDir, "src/main/AndroidManifest.xml"),
            File(projectDir, "app/src/main/AndroidManifest.xml")
        )
        return candidates.firstOrNull { it.exists() }
    }

    private fun resolveResDir(projectDir: File): File? {
        val candidates = listOf(
            File(projectDir, "res"),
            File(projectDir, "src/main/res"),
            File(projectDir, "app/src/main/res")
        )
        return candidates.firstOrNull { it.exists() }
    }

    private fun resolveSrcDir(projectDir: File): File? {
        val candidates = listOf(
            File(projectDir, "src"),
            File(projectDir, "src/main/java"),
            File(projectDir, "app/src/main/java")
        )
        return candidates.firstOrNull { it.exists() }
    }

    private fun collectJavaFiles(dir: File?, list: MutableList<File>) {
        if (dir == null || !dir.exists()) return
        dir.walkTopDown().filter { it.isFile && it.extension.equals("java", ignoreCase = true) }.forEach {
            list.add(it)
        }
    }

    private fun collectClassFiles(dir: File, list: MutableList<Path>) {
        dir.walkTopDown().filter { it.isFile && it.extension.equals("class", ignoreCase = true) }.forEach {
            list.add(it.toPath())
        }
    }

    internal fun packageApk(intermediateApk: File, dexDir: File, outputApk: File) {
        val buffer = ByteArray(8192)
        val dexFiles = dexDir.listFiles()?.filter { it.isFile && it.name.endsWith(".dex") } ?: emptyList()

        if (outputApk.exists()) {
            outputApk.delete()
        }

        ZipOutputStream(FileOutputStream(outputApk).buffered()).use { outZip ->
            // Copy entries from intermediateApk
            if (intermediateApk.exists()) {
                ZipFile(intermediateApk).use { inZip ->
                    val entries = inZip.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (entry.isDirectory) {
                            outZip.putNextEntry(ZipEntry(entry.name))
                            outZip.closeEntry()
                            continue
                        }

                        val newEntry = ZipEntry(entry.name)
                        // If entry was stored uncompressed (e.g. resources.arsc), preserve STORED method, size, and crc
                        if (entry.method == ZipEntry.STORED) {
                            newEntry.method = ZipEntry.STORED
                            newEntry.size = entry.size
                            newEntry.compressedSize = entry.compressedSize
                            newEntry.crc = entry.crc
                        }
                        outZip.putNextEntry(newEntry)
                        inZip.getInputStream(entry).use { input ->
                            var len: Int
                            while (input.read(buffer).also { len = it } > 0) {
                                outZip.write(buffer, 0, len)
                            }
                        }
                        outZip.closeEntry()
                    }
                }
            }

            // Inject DEX files
            dexFiles.forEach { dexFile ->
                val dexEntry = ZipEntry(dexFile.name)
                outZip.putNextEntry(dexEntry)
                FileInputStream(dexFile).buffered().use { input ->
                    var len: Int
                    while (input.read(buffer).also { len = it } > 0) {
                        outZip.write(buffer, 0, len)
                    }
                }
                outZip.closeEntry()
            }
        }
    }

    private fun loadKeyStore(keystoreFile: File, storePass: String): KeyStore {
        val types = listOf("PKCS12", "BKS", "JKS", KeyStore.getDefaultType())
        var lastEx: Exception? = null
        for (type in types) {
            try {
                val ks = KeyStore.getInstance(type)
                FileInputStream(keystoreFile).use { ks.load(it, storePass.toCharArray()) }
                return ks
            } catch (e: Exception) {
                lastEx = e
            }
        }
        throw lastEx ?: IllegalStateException("Failed to load keystore $keystoreFile")
    }

    private fun signApk(inputApk: File, outputApk: File, keystoreFile: File, minSdk: Int) {
        val ks = loadKeyStore(keystoreFile, "android")
        val privateKey = ks.getKey("androiddebugkey", "android".toCharArray()) as PrivateKey
        val cert = ks.getCertificate("androiddebugkey") as X509Certificate

        val signerConfig = ApkSigner.SignerConfig.Builder(
            "androiddebugkey",
            privateKey,
            listOf(cert)
        ).build()

        val apkSigner = ApkSigner.Builder(listOf(signerConfig))
            .setInputApk(inputApk)
            .setOutputApk(outputApk)
            .setMinSdkVersion(minSdk.coerceAtLeast(26))
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .build()

        apkSigner.sign()
    }

    private fun generateDebugKeystore(keystoreFile: File) {
        keystoreFile.parentFile?.mkdirs()
        // Generate with keytool if absent
        val cmd = listOf(
            "keytool", "-genkeypair",
            "-v", "-keystore", keystoreFile.absolutePath,
            "-storepass", "android",
            "-alias", "androiddebugkey",
            "-keypass", "android",
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "10000",
            "-dname", "CN=Android Debug,O=Android,C=US"
        )
        try {
            val proc = ProcessBuilder(cmd).redirectErrorStream(true).start()
            proc.waitFor()
        } catch (_: Exception) {}
    }

    data class ManifestInfo(
        val packageName: String,
        val appName: String,
        val versionCode: Int,
        val versionName: String,
        val minSdkVersion: Int = 26,
        val targetSdkVersion: Int = 34
    )

    internal fun parseManifest(manifestFile: File): ManifestInfo {
        val content = manifestFile.readText()
        val pkgMatch = Regex("""package\s*=\s*"([^"]+)"""").find(content)
        val labelMatch = Regex("""android:label\s*=\s*"([^"]+)"""").find(content)
        val vCodeMatch = Regex("""android:versionCode\s*=\s*"([^"]+)"""").find(content)
        val vNameMatch = Regex("""android:versionName\s*=\s*"([^"]+)"""").find(content)
        val minSdkMatch = Regex("""android:minSdkVersion\s*=\s*"(\d+)"""").find(content)
        val targetSdkMatch = Regex("""android:targetSdkVersion\s*=\s*"(\d+)"""").find(content)

        return ManifestInfo(
            packageName = pkgMatch?.groupValues?.get(1) ?: "com.example.app",
            appName = labelMatch?.groupValues?.get(1)?.removePrefix("@string/") ?: "App",
            versionCode = vCodeMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1,
            versionName = vNameMatch?.groupValues?.get(1) ?: "1.0",
            minSdkVersion = minSdkMatch?.groupValues?.get(1)?.toIntOrNull() ?: 26,
            targetSdkVersion = targetSdkMatch?.groupValues?.get(1)?.toIntOrNull() ?: 34
        )
    }
}
