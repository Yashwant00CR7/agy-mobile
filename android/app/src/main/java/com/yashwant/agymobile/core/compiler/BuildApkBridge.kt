package com.yashwant.agymobile.core.compiler

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object BuildApkBridge {

    fun executeBuild(
        engine: ApkCompilerEngine,
        workspaceDir: File,
        targetAppDir: File? = null,
        onProgress: ((CompilationProgress) -> Unit)? = null
    ): Pair<CompilerResult, JSONObject> {
        val buildDir = targetAppDir ?: workspaceDir
        val result = try {
            engine.compileProject(buildDir, onProgress = onProgress)
        } catch (t: Throwable) {
            CompilerResult(
                success = false,
                failedStage = CompilationStage.FAILED,
                errorMessage = "Fatal compilation error: ${t.message ?: t.javaClass.simpleName}",
                rawOutput = t.stackTraceToString()
            )
        }

        val json = JSONObject()
        json.put("success", result.success)
        json.put("app_name", result.appName)
        json.put("package_name", result.packageName)
        json.put("duration_ms", result.durationMs)

        if (result.success && result.apkFile != null) {
            json.put("apk_path", result.apkFile.absolutePath)
            json.put("size_bytes", result.sizeBytes)
            json.put("version_code", result.versionCode)
            json.put("version_name", result.versionName)
            json.put("message", "APK compiled and signed successfully on-device! Path: ${result.apkFile.absolutePath}")
        } else {
            json.put("failed_stage", result.failedStage?.name ?: "UNKNOWN")
            json.put("error_message", result.errorMessage ?: "Compilation failed")

            val errorsArray = JSONArray()
            result.errors.forEach { err ->
                val errObj = JSONObject()
                errObj.put("stage", err.stage.name)
                err.file?.let { errObj.put("file", it) }
                err.line?.let { errObj.put("line", it) }
                err.column?.let { errObj.put("column", it) }
                errObj.put("message", err.message)
                errorsArray.put(errObj)
            }
            json.put("errors", errorsArray)
            json.put("instruction", "Inspect the errors above, correct the code/layout in the workspace, and call build_apk again to recompile.")
        }

        return Pair(result, json)
    }
}
