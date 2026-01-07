package com.nano.ai.util

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.documentfile.provider.DocumentFile

data class SelectedFile(
    val uri: Uri,
    val name: String,
    val size: Long,
    val sizeFormatted: String,
    val mimeType: String?
)

class FilePickerState {
    var selectedFile: SelectedFile? = null
        private set

    var ggufInfo: GgufInfo? = null
        private set

    fun updateSelection(file: SelectedFile?, info: GgufInfo?) {
        selectedFile = file
        ggufInfo = info
    }

    fun clear() {
        selectedFile = null
        ggufInfo = null
    }
}

@Composable
fun rememberFilePickerState(): FilePickerState {
    return remember { FilePickerState() }
}

@Composable
fun rememberGgufFilePicker(
    context: Context,
    onFilePicked: (SelectedFile, GgufInfo) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val documentFile = DocumentFile.fromSingleUri(context, it)
            val fileName = documentFile?.name ?: "unknown.gguf"
            val fileSize = documentFile?.length() ?: 0L
            val mimeType = documentFile?.type

            val selectedFile = SelectedFile(
                uri = it,
                name = fileName,
                size = fileSize,
                sizeFormatted = GgufValidator.formatFileSize(fileSize),
                mimeType = mimeType
            )

            val ggufInfo = GgufValidator.validateUri(context, it)

            onFilePicked(selectedFile, ggufInfo)
        }
    }

    return {
        launcher.launch(arrayOf("*/*"))
    }
}

object FileUtils {

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        return DocumentFile.fromSingleUri(context, uri)?.name ?: "unknown"
    }

    fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        return DocumentFile.fromSingleUri(context, uri)?.length() ?: 0L
    }

    fun getMimeTypeFromUri(context: Context, uri: Uri): String? {
        return DocumentFile.fromSingleUri(context, uri)?.type
    }

    fun copyUriToInternal(
        context: Context,
        sourceUri: Uri,
        destFileName: String,
        destDir: String = "models"
    ): Result<String> {
        return try {
            val destDirectory = java.io.File(context.filesDir, destDir)
            if (!destDirectory.exists()) {
                destDirectory.mkdirs()
            }

            val destFile = java.io.File(destDirectory, destFileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output, bufferSize = 8192)
                }
            } ?: return Result.failure(Exception("Could not open source file"))

            Result.success(destFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteFile(filePath: String): Boolean {
        return try {
            java.io.File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }

    fun fileExists(filePath: String): Boolean {
        return java.io.File(filePath).exists()
    }

    fun getFileSize(filePath: String): Long {
        return java.io.File(filePath).length()
    }

    fun getFileSizeFormatted(filePath: String): String {
        return GgufValidator.formatFileSize(getFileSize(filePath))
    }
}
