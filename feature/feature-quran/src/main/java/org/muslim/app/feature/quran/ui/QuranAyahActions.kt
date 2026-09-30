package org.muslim.app.feature.quran.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.Ayah

internal fun ayahShareText(
    ayah: Ayah,
    surahName: String,
): String = buildString {
    if (surahName.isNotBlank()) {
        append(surahName)
        append(" — ")
    }
    append(ayah.numberInSurah)
    append("\n")
    append(ayah.text)
}

internal fun shareAyah(
    context: Context,
    ayah: Ayah,
    surahName: String,
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, ayahShareText(ayah, surahName))
    }
    val chooser = Intent.createChooser(intent, context.getString(R.string.quran_share_ayah))
    runCatching { context.startActivity(chooser) }
}

internal fun copyAyah(
    context: Context,
    ayah: Ayah,
    surahName: String,
) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard?.setPrimaryClip(
        ClipData.newPlainText(
            context.getString(R.string.quran_copy_ayah),
            ayahShareText(ayah, surahName),
        ),
    )
}
