package org.muslim.app.feature.reference.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReferenceAssetCodecTest {

    @Test
    fun `decoder maps versioned pack into reference domain`() {
        val book = ReferenceAssetCodec.decode(validPack())

        assertThat(book.id).isEqualTo("islam")
        assertThat(book.contentRevision).isEqualTo(2)
        assertThat(book.chapters.single().topicIds).containsExactly("what_is_islam")
        assertThat(book.topics.single().sections.single().paragraphs.single().ar)
            .isEqualTo("الإسلام هو الاستسلام لله.")
        assertThat(book.topics.single().reviewStatus)
            .isEqualTo(ReferenceReviewStatus.NeedsReview)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decoder rejects unsupported schema versions`() {
        ReferenceAssetCodec.decode(validPack().replace("\"schemaVersion\": 2", "\"schemaVersion\": 999"))
    }

    @Test
    fun `decoded pack passes structural validation`() {
        val book = ReferenceAssetCodec.decode(validPack())

        assertThat(ReferenceContentValidator.validate(listOf(book))).isEmpty()
    }

    private fun validPack() = """
        {
          "schemaVersion": 2,
          "book": {
            "id": "islam",
            "titleAr": "التعريف بالإسلام",
            "titleEn": "Introduction to Islam",
            "subtitleAr": "مرجع",
            "subtitleEn": "Reference",
            "contentRevision": 2,
            "chapters": [
              {
                "id": "foundations",
                "titleAr": "الأساسيات",
                "titleEn": "Foundations",
                "topicIds": ["what_is_islam"]
              }
            ],
            "topics": [
              {
                "id": "what_is_islam",
                "titleAr": "ما هو الإسلام؟",
                "titleEn": "What is Islam?",
                "summaryAr": "تعريف موجز",
                "summaryEn": "A short definition",
                "reviewStatus": "NeedsReview",
                "sections": [
                  {
                    "id": "definition",
                    "titleAr": "التعريف",
                    "titleEn": "Definition",
                    "paragraphs": [
                      {
                        "ar": "الإسلام هو الاستسلام لله.",
                        "en": "Islam is submission to God."
                      }
                    ]
                  }
                ]
              }
            ]
          }
        }
    """.trimIndent()
}
