package org.muslim.app.feature.reference.data

import android.content.Context
import org.muslim.app.feature.reference.R
import org.muslim.app.feature.reference.domain.InMemoryReferenceRepository
import org.muslim.app.feature.reference.domain.IslamIntroContent
import org.muslim.app.feature.reference.domain.ProphetsContent
import org.muslim.app.feature.reference.domain.ReferenceAssetCodec
import org.muslim.app.feature.reference.domain.ReferenceBook
import org.muslim.app.feature.reference.domain.ReferenceRepository
import org.muslim.app.feature.reference.domain.SiraContent

/** Creates the runtime repository from versioned bundled reference assets. */
object AndroidReferenceRepositoryFactory {

    fun create(context: Context): ReferenceRepository = InMemoryReferenceRepository(
        books = listOfNotNull(
            decode(context, R.raw.reference_islam_v2) ?: IslamIntroContent.book,
            decode(context, R.raw.reference_sira_v2) ?: SiraContent.book,
            decode(context, R.raw.reference_prophets_v2) ?: ProphetsContent.book,
            decode(context, R.raw.reference_companions_v2),
            decode(context, R.raw.reference_mothers_v2),
            decode(context, R.raw.reference_ahl_al_bayt_v2),
            decode(context, R.raw.reference_rashidun_v2),
            decode(context, R.raw.reference_aqeedah_v2),
            decode(context, R.raw.reference_quran_sciences_v2),
            decode(context, R.raw.reference_hadith_sciences_v2),
            decode(context, R.raw.reference_fiqh_worship_v2),
            decode(context, R.raw.reference_ethics_life_v2),
            decode(context, R.raw.reference_history_civilization_v2),
        ),
    )

    private fun decode(context: Context, rawResourceId: Int): ReferenceBook? = runCatching {
        context.resources
            .openRawResource(rawResourceId)
            .bufferedReader()
            .use { ReferenceAssetCodec.decode(it.readText()) }
    }.getOrNull()
}
