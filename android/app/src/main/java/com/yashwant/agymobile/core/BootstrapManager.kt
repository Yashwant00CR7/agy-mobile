package com.yashwant.agymobile.core

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class BootstrapManager(
    val baseDir: File,
    private val nativeLibDir: File? = null
) {
    val binDir: File = File(baseDir, "bin")
    val homeDir: File = File(baseDir, "home")
    val tmpDir: File = File(baseDir, "tmp")
    val defaultWorkspaceDir: File = File(baseDir, "workspaces")

    fun initializeDirectories(): Boolean {
        return binDir.mkdirs() or
                homeDir.mkdirs() or
                tmpDir.mkdirs() or
                defaultWorkspaceDir.mkdirs()
    }

    fun installBinary(name: String, inputStream: InputStream): File {
        initializeDirectories()
        val targetFile = File(binDir, name)
        FileOutputStream(targetFile).use { output ->
            inputStream.copyTo(output)
        }
        targetFile.setExecutable(true, false)
        targetFile.setReadable(true, false)
        return targetFile
    }

    fun setupAgyFromNativeLib(): File? {
        initializeDirectories()

        // 1. Clean up any stale copy in binDir that would fail with error=13 on Android 10+
        val staleInBin = File(binDir, "agy")
        if (staleInBin.exists()) {
            try {
                staleInBin.delete()
            } catch (_: Exception) {}
        }

        // 2. Return libagy.so directly from nativeLibraryDir.
        // On Android 10+ (API 29+), executing from nativeLibraryDir is permitted by SELinux (apk_data_file),
        // whereas executing copied binaries from app filesDir is strictly blocked by W^X restrictions (error=13).
        if (nativeLibDir != null) {
            val libAgy = File(nativeLibDir, "libagy.so")
            if (libAgy.exists() && libAgy.canExecute()) {
                return libAgy
            }
        }

        return null
    }

    fun getAgyExecutable(): File? {
        // ALWAYS prefer nativeLibDir because Android 10+ (API 29+) SELinux W^X
        // strictly blocks executing binaries from filesDir (error=13 Permission denied).
        // Only binaries inside nativeLibraryDir (apk_data_file context) can be executed.
        if (nativeLibDir != null) {
            val inLib = File(nativeLibDir, "libagy.so")
            if (inLib.exists() && inLib.canExecute()) return inLib
        }

        val inBin = File(binDir, "agy")
        if (inBin.exists() && inBin.canExecute()) return inBin

        return null
    }

    fun isAgyReady(): Boolean {
        return getAgyExecutable() != null
    }

    fun getEnvironmentVariables(): Map<String, String> {
        val pathBuilder = StringBuilder()
        if (nativeLibDir != null) {
            pathBuilder.append(nativeLibDir.absolutePath).append(":")
        }
        pathBuilder.append(binDir.absolutePath)
        pathBuilder.append(":/system/bin:/system/xbin")

        val currentPath = System.getenv("PATH")
        if (!currentPath.isNullOrBlank()) {
            pathBuilder.append(":").append(currentPath)
        }

        val env = mutableMapOf(
            "PATH" to pathBuilder.toString(),
            "HOME" to homeDir.absolutePath,
            "TMPDIR" to tmpDir.absolutePath,
            "TERM" to "xterm-256color",
            "PAGER" to "cat",
            "COLORTERM" to "truecolor"
        )

        if (nativeLibDir != null) {
            env["LD_LIBRARY_PATH"] = nativeLibDir.absolutePath
        }

        return env
    }
}
