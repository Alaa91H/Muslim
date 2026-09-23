package org.muslim.app.feature.family.domain

enum class FamilySearchKind {
    Article,
    BabyName,
    Ruqyah,
    Checklist,
}

data class FamilyGlobalSearchHit(
    val kind: FamilySearchKind,
    val id: String,
    val title: LocalizedFamilyText,
    val summary: LocalizedFamilyText,
)

object FamilyGlobalSearch {
    fun search(
        query: String,
        kind: FamilySearchKind? = null,
        limit: Int = 80,
    ): List<FamilyGlobalSearchHit> {
        val normalized = FamilyLifeContent.normalizeSearch(query)
        if (normalized.isEmpty()) return emptyList()

        return buildList {
            if (kind == null || kind == FamilySearchKind.Article) {
                FamilyLifeContent.searchArticles(query).forEach { article ->
                    add(
                        FamilyGlobalSearchHit(
                            kind = FamilySearchKind.Article,
                            id = article.id,
                            title = article.title,
                            summary = article.summary,
                        ),
                    )
                }
            }
            if (kind == null || kind == FamilySearchKind.BabyName) {
                FamilyLifeContent.searchNames(query).forEach { name ->
                    add(
                        FamilyGlobalSearchHit(
                            kind = FamilySearchKind.BabyName,
                            id = name.id,
                            title = LocalizedFamilyText(name.nameArabic, name.transliteration),
                            summary = LocalizedFamilyText(name.meaningArabic, name.meaningEnglish),
                        ),
                    )
                }
            }
            if (kind == null || kind == FamilySearchKind.Ruqyah) {
                addAll(searchRuqyah(normalized))
            }
            if (kind == null || kind == FamilySearchKind.Checklist) {
                addAll(searchChecklists(normalized))
            }
        }.distinctBy { it.kind to it.id }.take(limit.coerceAtLeast(0))
    }

    private fun searchRuqyah(normalized: String): List<FamilyGlobalSearchHit> {
        val passages = FamilyLifeContent.ruqyahPassages.mapNotNull { passage ->
            val text = listOf(
                passage.title.arabic,
                passage.title.english,
                passage.text.arabic,
                passage.text.english,
                passage.reference.arabic,
                passage.reference.english,
            ).joinToString(" ")
            passage.takeIf { FamilyLifeContent.normalizeSearch(text).contains(normalized) }?.let {
                FamilyGlobalSearchHit(
                    kind = FamilySearchKind.Ruqyah,
                    id = "passage:${it.id}",
                    title = it.title,
                    summary = it.reference,
                )
            }
        }
        val supplications = FamilyLifeContent.ruqyahSupplications.mapNotNull { dua ->
            val text = listOf(
                dua.title.arabic,
                dua.title.english,
                dua.arabic,
                dua.meaning.arabic,
                dua.meaning.english,
                dua.reference.arabic,
                dua.reference.english,
            ).joinToString(" ")
            dua.takeIf { FamilyLifeContent.normalizeSearch(text).contains(normalized) }?.let {
                FamilyGlobalSearchHit(
                    kind = FamilySearchKind.Ruqyah,
                    id = "dua:${it.id}",
                    title = it.title,
                    summary = it.meaning,
                )
            }
        }
        return passages + supplications
    }

    private fun searchChecklists(normalized: String): List<FamilyGlobalSearchHit> =
        FamilyUtilityContent.checklists.mapNotNull { checklist ->
            val text = buildList {
                add(checklist.title.arabic)
                add(checklist.title.english)
                add(checklist.description.arabic)
                add(checklist.description.english)
                checklist.items.forEach { item ->
                    add(item.title.arabic)
                    add(item.title.english)
                }
            }.joinToString(" ")
            checklist.takeIf {
                FamilyLifeContent.normalizeSearch(text).contains(normalized)
            }?.let {
                FamilyGlobalSearchHit(
                    kind = FamilySearchKind.Checklist,
                    id = it.id,
                    title = it.title,
                    summary = it.description,
                )
            }
        }
}
