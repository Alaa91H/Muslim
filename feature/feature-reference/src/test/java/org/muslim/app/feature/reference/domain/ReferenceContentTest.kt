package org.muslim.app.feature.reference.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReferenceContentTest {

    @Test
    fun `library ships the three expected books`() {
        assertThat(ReferenceLibrary.books.map { it.id })
            .containsExactly("islam", "sira", "prophets")
            .inOrder()
    }

    @Test
    fun `every book has indexed topics with unique ids`() {
        ReferenceLibrary.books.forEach { book ->
            assertThat(book.topics).isNotEmpty()
            val ids = book.topics.map { it.id }
            assertThat(ids).containsNoDuplicates()
        }
    }

    @Test
    fun `every topic has complete Arabic and English title and summary`() {
        ReferenceLibrary.books.forEach { book ->
            book.topics.forEach { topic ->
                assertThat(topic.titleAr).isNotEmpty()
                assertThat(topic.titleEn).isNotEmpty()
                assertThat(topic.summaryAr).isNotEmpty()
                assertThat(topic.summaryEn).isNotEmpty()
            }
        }
    }

    @Test
    fun `every topic has sections with non-blank paragraphs in both languages`() {
        ReferenceLibrary.books.forEach { book ->
            book.topics.forEach { topic ->
                assertThat(topic.sections).isNotEmpty()
                topic.sections.forEach { section ->
                    assertThat(section.titleAr).isNotEmpty()
                    assertThat(section.titleEn).isNotEmpty()
                    assertThat(section.paragraphs).isNotEmpty()
                    section.paragraphs.forEach { paragraph ->
                        assertThat(paragraph.ar).isNotEmpty()
                        assertThat(paragraph.en).isNotEmpty()
                    }
                }
            }
        }
    }

    @Test
    fun `search filters topics by query in the selected language`() {
        val islam = ReferenceLibrary.byId("islam")!!
        val matches = ReferenceLibrary.search(islam, "الصلاة", RefLang.Arabic)
        assertThat(matches.map { it.id }).contains("pillars_islam")
        val english = ReferenceLibrary.search(islam, "prayer", RefLang.English)
        assertThat(english.map { it.id }).contains("pillars_islam")
    }
}
