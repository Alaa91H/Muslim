package org.muslim.app.feature.reference.data

import android.content.Context
import org.muslim.app.feature.reference.R
import org.muslim.app.feature.reference.domain.InMemoryReferenceRepository
import org.muslim.app.feature.reference.domain.IslamIntroContent
import org.muslim.app.feature.reference.domain.ProphetsContent
import org.muslim.app.feature.reference.domain.ReferenceAssetCodec
import org.muslim.app.feature.reference.domain.ReferenceRepository
import org.muslim.app.feature.reference.domain.SiraContent

/** Creates the runtime repository from versioned bundled reference assets. */
object AndroidReferenceRepositoryFactory {

    fun create(context: Context): ReferenceRepository {
        val islamBook = runCatching {
            context.resources
                .openRawResource(R.raw.reference_islam_v2)
                .bufferedReader()
                .use { ReferenceAssetCodec.decode(it.readText()) }
        }.getOrElse {
            // Keep the reference destination usable if a packaged asset is ever
            // corrupted. CI parity/validation checks are responsible for making
            // this fallback unreachable in release builds.
            IslamIntroContent.book
        }

        val siraBook = runCatching {
            context.resources
                .openRawResource(R.raw.reference_sira_v2)
                .bufferedReader()
                .use { ReferenceAssetCodec.decode(it.readText()) }
        }.getOrElse {
            SiraContent.book
        }

        val prophetsBook = runCatching {
            context.resources
                .openRawResource(R.raw.reference_prophets_v2)
                .bufferedReader()
                .use { ReferenceAssetCodec.decode(it.readText()) }
        }.getOrElse {
            ProphetsContent.book
        }

        return InMemoryReferenceRepository(
            books = listOf(
                islamBook,
                siraBook,
                prophetsBook,
            ),
        )
    }
}
