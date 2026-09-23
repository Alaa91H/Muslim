package org.muslim.app.feature.scholarlibrary.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

internal data class ScholarSelectedTextFile(
    val displayName: String?,
    val text: String,
)

internal fun readScholarTextFile(
    context: Context,
    uri: Uri?,
): ScholarSelectedTextFile? = uri?.let { selectedUri ->
    runCatching {
        val displayName = context.contentResolver.query(
            selectedUri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
        val text = context.contentResolver.openInputStream(selectedUri)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { reader -> reader.readText() }
            ?: return@runCatching null
        ScholarSelectedTextFile(displayName = displayName, text = text)
    }.getOrNull()
}

internal fun writeScholarTextFile(
    context: Context,
    uri: Uri?,
    text: String?,
): Boolean {
    if (uri == null || text == null) return false
    return runCatching {
        context.contentResolver.openOutputStream(uri, "wt")
            ?.bufferedWriter(Charsets.UTF_8)
            ?.use { writer -> writer.write(text) }
            ?: error("تعذر فتح ملف الحفظ.")
        true
    }.getOrDefault(false)
}
