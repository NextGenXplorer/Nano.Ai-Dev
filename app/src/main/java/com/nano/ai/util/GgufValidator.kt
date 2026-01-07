package com.nano.ai.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class GgufInfo(
    val isValid: Boolean,
    val version: Int = 0,
    val fileName: String = "",
    val fileSize: Long = 0,
    val fileSizeFormatted: String = "",
    val errorMessage: String? = null
)

object GgufValidator {

    // GGUF magic number: "GGUF" in little-endian
    private const val GGUF_MAGIC = 0x46554747 // "GGUF" reversed

    fun validateFile(file: File): GgufInfo {
        if (!file.exists()) {
            return GgufInfo(
                isValid = false,
                errorMessage = "File does not exist"
            )
        }

        if (!file.canRead()) {
            return GgufInfo(
                isValid = false,
                errorMessage = "Cannot read file"
            )
        }

        return try {
            file.inputStream().use { stream ->
                validateStream(stream, file.name, file.length())
            }
        } catch (e: Exception) {
            GgufInfo(
                isValid = false,
                errorMessage = "Error reading file: ${e.message}"
            )
        }
    }

    fun validateUri(context: Context, uri: Uri): GgufInfo {
        val documentFile = DocumentFile.fromSingleUri(context, uri)
            ?: return GgufInfo(
                isValid = false,
                errorMessage = "Cannot access file"
            )

        val fileName = documentFile.name ?: "unknown.gguf"
        val fileSize = documentFile.length()

        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                validateStream(stream, fileName, fileSize)
            } ?: GgufInfo(
                isValid = false,
                errorMessage = "Cannot open file stream"
            )
        } catch (e: Exception) {
            GgufInfo(
                isValid = false,
                errorMessage = "Error reading file: ${e.message}"
            )
        }
    }

    private fun validateStream(stream: InputStream, fileName: String, fileSize: Long): GgufInfo {
        // Read first 8 bytes for magic and version
        val header = ByteArray(8)
        val bytesRead = stream.read(header)

        if (bytesRead < 8) {
            return GgufInfo(
                isValid = false,
                fileName = fileName,
                fileSize = fileSize,
                fileSizeFormatted = formatFileSize(fileSize),
                errorMessage = "File too small to be a valid GGUF"
            )
        }

        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        val magic = buffer.int
        val version = buffer.int

        if (magic != GGUF_MAGIC) {
            return GgufInfo(
                isValid = false,
                fileName = fileName,
                fileSize = fileSize,
                fileSizeFormatted = formatFileSize(fileSize),
                errorMessage = "Invalid GGUF magic number"
            )
        }

        if (version < 1 || version > 3) {
            return GgufInfo(
                isValid = false,
                version = version,
                fileName = fileName,
                fileSize = fileSize,
                fileSizeFormatted = formatFileSize(fileSize),
                errorMessage = "Unsupported GGUF version: $version"
            )
        }

        return GgufInfo(
            isValid = true,
            version = version,
            fileName = fileName,
            fileSize = fileSize,
            fileSizeFormatted = formatFileSize(fileSize)
        )
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
            else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
        }
    }

    fun isGgufFile(fileName: String): Boolean {
        return fileName.lowercase().endsWith(".gguf")
    }
}
