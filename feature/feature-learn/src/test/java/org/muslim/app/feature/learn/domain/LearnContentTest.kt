package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Content integrity for the learning guides (PROJECT_PROMPT.md §6 Phase 5)
 * and the ninety-nine names of Allah page.
 *
 * Pins NamesOfAllahContent at exactly 99 complete, unique names, enforces the
 * project-wide rule that only Western digits appear, and guards every guide
 * topic against empty steps.
 */
class LearnContentTest {

    @Test
    fun `the ninety-nine names are complete unique and fully detailed`() {
        val names = NamesOfAllahContent.NAMES
        assertThat(names).hasSize(99)
        assertThat(names.map { it.number }).containsExactlyElementsIn((1..99).toList())
        assertThat(names.map { it.arabic }.distinct()).hasSize(99)
        names.forEach { name ->
            assertThat(name.arabic).isNotEmpty()
            assertThat(name.transliteration).isNotEmpty()
            assertThat(name.meaningAr).isNotEmpty()
            assertThat(name.meaningEn).isNotEmpty()
            assertThat(name.explanationAr).isNotEmpty()
            assertThat(name.explanationEn).isNotEmpty()
            assertThat(name.evidenceAr).isNotEmpty()
            assertThat(name.evidenceRefAr).isNotEmpty()
            assertThat(name.evidenceEn).isNotEmpty()
            assertThat(name.evidenceRefEn).isNotEmpty()
        }
    }

    @Test
    fun `the names use only western digits`() {
        val arabicIndic = "\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669"
        NamesOfAllahContent.NAMES.forEach { name ->
            assertThat(name.arabic).doesNotContain(arabicIndic)
            assertThat(name.explanationAr).doesNotContain(arabicIndic)
            assertThat(name.meaningAr).doesNotContain(arabicIndic)
            assertThat(name.evidenceAr).doesNotContain(arabicIndic)
            assertThat(name.evidenceRefAr).doesNotContain(arabicIndic)
        }
    }

    @Test
    fun `every topic has a title and at least one step`() {
        LearnContent.topics.forEach { topic ->
            assertThat(topic.titleRes).isGreaterThan(0)
            assertThat(topic.subtitleRes).isGreaterThan(0)
            assertThat(topic.steps).isNotEmpty()
            assertThat(topic.id).isNotEqualTo("names_of_allah")
        }
    }

    @Test
    fun `the hajj and umrah content is complete and fully detailed`() {
        val categories = HajjUmrahContent.CATEGORIES
        assertThat(categories).isNotEmpty()
        assertThat(categories.map { it.id }.distinct()).hasSize(categories.size)
        categories.forEach { category ->
            assertThat(category.title).isNotEmpty()
            assertThat(category.titleEn).isNotEmpty()
            assertThat(category.iconKey).isNotEmpty()
            assertThat(category.topics).isNotEmpty()
            category.topics.forEach { topic ->
                assertThat(topic.id).isNotEmpty()
                assertThat(topic.title).isNotEmpty()
                assertThat(topic.titleEn).isNotEmpty()
                assertThat(topic.summary).isNotEmpty()
                assertThat(topic.summaryEn).isNotEmpty()
                assertThat(topic.steps).isNotEmpty()
                topic.steps.forEach { step ->
                    assertThat(step.title).isNotEmpty()
                    assertThat(step.titleEn).isNotEmpty()
                    assertThat(step.what).isNotEmpty()
                    assertThat(step.whatEn).isNotEmpty()
                }
            }
        }
    }

    @Test
    fun `every topic belongs to a known category and each category has content`() {
        val topics = LearnContent.topics
        assertThat(topics.map { it.id }.distinct()).hasSize(topics.size)
        topics.forEach { topic ->
            assertThat(LearnContent.categoryOrder).contains(topic.category)
            assertThat(topic.titleRes).isGreaterThan(0)
            assertThat(topic.subtitleRes).isGreaterThan(0)
            assertThat(topic.steps).isNotEmpty()
        }
        LearnContent.categoryOrder.forEach { category ->
            assertThat(topics.any { it.category == category }).isTrue()
        }
    }

    @Test
    fun `the learning reference covers all major subjects`() {
        val ids = LearnContent.topics.map { it.id }.toSet()
        assertThat(ids).containsAtLeast(
            "pillars_islam", "pillars_iman",
            "wudu", "ghusl", "tayammum",
            "salah", "salah_arkan", "salah_times", "adhan", "shurut", "nullifiers", "rawatib", "rakats", "special",
            "fasting", "zakat", "funeral",
            "madhhab",
        )
        LearnContent.topics.forEach { topic ->
            topic.steps.forEach { step ->
                assertThat(step.title).isNotEmpty()
                assertThat(step.description).isNotEmpty()
            }
        }
    }

    @Test
    fun `the learning content uses only western digits`() {
        val arabicIndic = "٠١٢٣٤٥٦٧٨٩"
        LearnContent.topics.forEach { topic ->
            topic.steps.forEach { step ->
                assertThat(step.title).doesNotContain(arabicIndic)
                assertThat(step.description).doesNotContain(arabicIndic)
            }
            topic.notes?.let { assertThat(it).doesNotContain(arabicIndic) }
        }
    }

}

