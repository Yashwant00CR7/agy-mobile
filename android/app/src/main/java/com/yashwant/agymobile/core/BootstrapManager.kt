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
        val targetAgy = File(binDir, "agy")

        // 1. If nativeLibraryDir contains libagy.so, link or copy it to binDir
        if (nativeLibDir != null) {
            val libAgy = File(nativeLibDir, "libagy.so")
            if (libAgy.exists() && libAgy.canExecute()) {
                if (!targetAgy.exists() || targetAgy.length() != libAgy.length()) {
                    try {
                        libAgy.copyTo(targetAgy, overwrite = true)
                        targetAgy.setExecutable(true, false)
                    } catch (e: Exception) {
                        return libAgy
                    }
                }
                return if (targetAgy.canExecute()) targetAgy else libAgy
            }
        }

        // 2. If target already exists in binDir
        if (targetAgy.exists() && targetAgy.canExecute()) {
            return targetAgy
        }

        return null
    }

    fun getAgyExecutable(): File? {
        val inBin = File(binDir, "agy")
        if (inBin.exists() && inBin.canExecute()) return inBin

        if (nativeLibDir != null) {
            val inLib = File(nativeLibDir, "libagy.so")
            if (inLib.exists() && inLib.canExecute()) return inLib
        }

        return null
    }

    fun isAgyReady(): Boolean {
        return getAgyExecutable() != null
    }

    fun getEnvironmentVariables(): Map<String, String> {
        val pathBuilder = StringBuilder()
        pathBuilder.append(binDir.absolutePath)
        pathBuilder.append(":/system/bin:/system/xbin")

        val currentPath = System.getenv("PATH")
        if (!currentPath.isNullOrBlank()) {
            pathBuilder.append(":").append(currentPath)
        }

        return mapOf(
            "PATH" to pathBuilder.toString(),
            "HOME" to homeDir.absolutePath,
            "TMPDIR" to tmpDir.absolutePath,
            "TERM" to "xterm-256color",
            "PAGER" to "cat",
            "COLORTERM" to "truecolor"
        )
    }
}
