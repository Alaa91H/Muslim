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
        books = listOf(
            decodeOrFallback(context, R.raw.reference_islam_v2, IslamIntroContent.book),
            decodeOrFallback(context, R.raw.reference_sira_v2, SiraContent.book),
            decodeOrFallback(context, R.raw.reference_prophets_v2, ProphetsContent.book),
            decode(context, R.raw.reference_companions_v2),
            decode(context, R.raw.reference_mothers_v2),
            decode(context, R.raw.reference_ahl_al_bayt_v2),
            decode(context, R.raw.reference_rashidun_v2),
        ),
    )

    private fun decodeOrFallback(
        context: Context,
        resourceId: Int,
        fallback: ReferenceBook,
    ): ReferenceBook = runCatching {
        decode(context, resourceId)
    }.getOrElse {
        // Legacy books keep a Kotlin fallback during the staged asset migration.
        fallback
    }

    private fun decode(context: Context, resourceId: Int): ReferenceBook =
        context.resources
            .openRawResource(resourceId)
            .bufferedReader()
            .use { ReferenceAssetCodec.decode(it.readText()) }
}
