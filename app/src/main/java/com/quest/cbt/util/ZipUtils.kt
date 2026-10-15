package com.quest.cbt.util

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

data class ExtractedPackage(
    val jsonContent: String,
    val imagesDir: File?
)

object ZipUtils {

    fun processImportUri(context: Context, uri: Uri, testId: String): ExtractedPackage {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: ""
        val fileName = uri.lastPathSegment?.lowercase() ?: ""

        val isZip = fileName.endsWith(".zip") ||
                mimeType.contains("zip") ||
                mimeType.contains("compressed") ||
                mimeType.contains("octet-stream")

        // First attempt reading as ZIP
        return try {
            contentResolver.openInputStream(uri)?.use { stream ->
                extractZip(context, stream, testId)
            } ?: throw IllegalArgumentException("Could not open file.")
        } catch (e: Exception) {
            // If extracting as ZIP failed or file is plain JSON, try reading as raw JSON text
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    val text = stream.bufferedReader().use { it.readText() }
                    if (text.trim().startsWith("{")) {
                        ExtractedPackage(text, null)
                    } else {
                        throw e
                    }
                } ?: throw e
            } catch (_: Exception) {
                throw e
            }
        }
    }

    fun extractZip(context: Context, inputStream: InputStream, testId: String): ExtractedPackage {
        var jsonContent: String? = null
        val targetImagesDir = File(context.filesDir, "tests/$testId/images").apply { mkdirs() }

        val zis = ZipInputStream(inputStream)
        var entry = zis.nextEntry
        val buffer = ByteArrayOutputStream()
        val readBuf = ByteArray(8192)

        while (entry != null) {
            val name = entry.name
            // Normalize slashes
            val cleanName = name.replace('\\', '/')

            if (!entry.isDirectory) {
                if (cleanName.endsWith(".json", ignoreCase = true) && jsonContent == null) {
                    buffer.reset()
                    var len: Int
                    while (zis.read(readBuf).also { len = it } > 0) {
                        buffer.write(readBuf, 0, len)
                    }
                    jsonContent = buffer.toString("UTF-8")
                } else if (cleanName.contains("images/", ignoreCase = true) ||
                    cleanName.endsWith(".png", ignoreCase = true) ||
                    cleanName.endsWith(".jpg", ignoreCase = true) ||
                    cleanName.endsWith(".jpeg", ignoreCase = true) ||
                    cleanName.endsWith(".webp", ignoreCase = true)
                ) {
                    // Extract image safely
                    val baseName = File(cleanName).name
                    val destinationFile = File(targetImagesDir, baseName)
                    // Guard against Zip Slip
                    if (destinationFile.canonicalPath.startsWith(targetImagesDir.canonicalPath)) {
                        FileOutputStream(destinationFile).use { fos ->
                            var len: Int
                            while (zis.read(readBuf).also { len = it } > 0) {
                                fos.write(readBuf, 0, len)
                            }
                        }
                    }
                }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }

        if (jsonContent == null) {
            throw IllegalArgumentException("No .json file found inside the ZIP archive. Please include 'test.json'.")
        }

        return ExtractedPackage(
            jsonContent = jsonContent,
            imagesDir = targetImagesDir
        )
    }
}
