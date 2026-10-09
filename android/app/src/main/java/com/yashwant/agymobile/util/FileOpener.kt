package com.yashwant.agymobile.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLDecoder

sealed class LinkTarget {
    abstract val label: String
    abstract val icon: String

    data class Directory(val file: File, override val label: String) : LinkTarget() {
        override val icon: String = "📁 "
    }

    data class Image(val file: File, override val label: String) : LinkTarget() {
        override val icon: String = "🖼️ "
    }

    data class Document(val file: File, override val label: String, val mimeType: String) : LinkTarget() {
        override val icon: String = "📄 "
    }

    data class Media(val file: File, override val label: String, val mimeType: String) : LinkTarget() {
        override val icon: String = "🎬 "
    }

    data class Web(val url: String, override val label: String) : LinkTarget() {
        override val icon: String = "🔗 "
    }

    data class Unknown(val raw: String, override val label: String) : LinkTarget() {
        override val icon: String = "🔗 "
    }
}

object FileOpener {

    private val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "webp", "gif", "bmp", "svg")
    private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "m4a", "flac", "ogg", "aac")
    private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "webm", "avi", "mov", "3gp")
    private val DOCUMENT_EXTENSIONS = setOf("pdf", "txt", "md", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "csv", "json", "kt", "java", "py", "sh", "xml", "html")
    private val COMMON_DOCUMENT_NAMES = setOf("license", "makefile", "dockerfile", "readme", "gemfile", "procfile", "cname", "authors", "contributors", "changelog")

    fun resolveTarget(rawUrlOrPath: String, customLabel: String? = null): LinkTarget {
        val trimmed = rawUrlOrPath.trim()
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            val label = customLabel?.ifBlank { null } ?: trimmed
            return LinkTarget.Web(trimmed, label)
        }

        val isFileScheme = trimmed.startsWith("file://", ignoreCase = true)
        val isExplicitPath = trimmed.startsWith("/") || trimmed.startsWith("./") || trimmed.startsWith("../")

        // If it uses an unrecognized scheme like tel:, mailto:, custom://, emit Unknown
        if (!isFileScheme && !isExplicitPath && trimmed.contains("://")) {
            val label = customLabel?.ifBlank { null } ?: trimmed
            return LinkTarget.Unknown(trimmed, label)
        }

        // Clean file:// scheme and decode URL encoding (e.g. %20 -> space, %C2%A0 -> non-breaking space)
        val cleanPath = when {
            isFileScheme -> trimmed.substring(7)
            else -> trimmed
        }

        val decodedPath = try {
            URLDecoder.decode(cleanPath, "UTF-8")
        } catch (_: Exception) {
            cleanPath
        }

        val file = File(decodedPath)
        val defaultLabel = file.name.ifBlank { decodedPath }
        val label = customLabel?.ifBlank { null } ?: defaultLabel
        val ext = file.extension.lowercase()
        val nameLower = file.name.lowercase()
        val isKnownDocName = COMMON_DOCUMENT_NAMES.contains(nameLower)
        val isStorageRootOrDir = (decodedPath.startsWith("/storage/emulated/0") || decodedPath.startsWith("/sdcard")) && !nameLower.contains(".")

        return when {
            file.isDirectory || decodedPath.endsWith("/") -> LinkTarget.Directory(file, label)
            isKnownDocName -> LinkTarget.Document(file, label, "text/plain")
            IMAGE_EXTENSIONS.contains(ext) -> LinkTarget.Image(file, label)
            AUDIO_EXTENSIONS.contains(ext) || VIDEO_EXTENSIONS.contains(ext) -> LinkTarget.Media(file, label, getMimeType(file))
            DOCUMENT_EXTENSIONS.contains(ext) -> LinkTarget.Document(file, label, getMimeType(file))
            file.exists() && file.isFile -> LinkTarget.Document(file, label, getMimeType(file))
            isStorageRootOrDir -> LinkTarget.Directory(file, label)
            else -> LinkTarget.Document(file, label, getMimeType(file))
        }
    }

    fun getMimeType(file: File): String {
        val ext = file.extension.lowercase()
        if (ext == "pdf") return "application/pdf"
        if (ext == "md" || ext == "kt" || ext == "java" || ext == "py" || ext == "sh" || ext == "json") return "text/plain"
        val fromMap = try {
            MimeTypeMap.getSingleton()?.getMimeTypeFromExtension(ext)
        } catch (_: Throwable) {
            null
        }
        return fromMap ?: when {
            IMAGE_EXTENSIONS.contains(ext) -> "image/*"
            AUDIO_EXTENSIONS.contains(ext) -> "audio/*"
            VIDEO_EXTENSIONS.contains(ext) -> "video/*"
            else -> "*/*"
        }
    }

    fun openTarget(
        context: Context,
        rawUrlOrPath: String,
        onSelectDirectory: ((File) -> Unit)? = null
    ) {
        val target = resolveTarget(rawUrlOrPath)
        openTarget(context, target, onSelectDirectory)
    }

    fun openTarget(
        context: Context,
        target: LinkTarget,
        onSelectDirectory: ((File) -> Unit)? = null
    ) {
        when (target) {
            is LinkTarget.Web -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target.url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open web link: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            is LinkTarget.Directory -> {
                val dir = target.file
                if (onSelectDirectory != null) {
                    onSelectDirectory(dir)
                    Toast.makeText(context, "Switched workspace to: ${dir.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Folder: ${dir.absolutePath}", Toast.LENGTH_SHORT).show()
                }
            }

            is LinkTarget.Image, is LinkTarget.Document, is LinkTarget.Media -> {
                val (file, mime) = when (target) {
                    is LinkTarget.Image -> target.file to getMimeType(target.file)
                    is LinkTarget.Document -> target.file to target.mimeType
                    is LinkTarget.Media -> target.file to target.mimeType
                    else -> return
                }

                if (!file.exists()) {
                    Toast.makeText(context, "File does not exist: ${file.name}", Toast.LENGTH_SHORT).show()
                    return
                }

                try {
                    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mime)
                        clipData = ClipData.newRawUri("", uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    val chooser = Intent.createChooser(intent, "Open ${file.name}").apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Unable to open file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            is LinkTarget.Unknown -> {
                Toast.makeText(context, "Unsupported link: ${target.raw}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
