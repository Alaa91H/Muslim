package org.muslim.app.core.common.text

/**
 * Heuristic Arabic root analyzer for Quran word lookup (PROJECT_PROMPT.md §6:
 * البحث عن كلمة — عرض جذر الكلمة والتصريفات المشتقة منها مثل معاجم القرآن).
 *
 * This is a lightweight, deterministic morphological guesser — not a full
 * lexicon. It strips common prefixes (ال، و، ف، ب، ل، ك، س، است) and
 * suffixes (ها، هم، نا، كم، كن، ات، ان، ون، ين، …), removes weak letters
 * (ا و ي) and folds hamza variants to their base, then takes the first three
 * remaining consonants as the root skeleton. Words that share a root are the
 * "derived inflections" of one another — e.g. رحيم/رحمن/رحمة/الرحيم all
 * derive from رحم.
 *
 * Pure JVM logic (no Android types) so it runs as a plain unit test.
 */
object QuranRootAnalyzer {

    /** Longest-first so «وال» is stripped before «ال», «ب», «و»… */
    private val PREFIXES = listOf(
        "وال", "بال", "فال", "كال", "لال", "ول", "بل",
        "است", "ال", "ب", "ف", "ك", "ل", "و", "س",
    )

    /** Longest-first; the caller keeps at least two base letters.
     * «ان» is deliberately excluded: it is a root letter in many Quranic
     * words (إيمان، إنسان، سلطان) and stripping it destroys the skeleton.
     */
    private val SUFFIXES = listOf(
        "كما", "هما", "كما", "كم", "كن", "هم", "هن", "ها", "نا",
        "تما", "تم", "تن", "ات", "ون", "ين", "ني", "تا", "تي",
    )

    private val WEAK = setOf('\u0627', '\u0648', '\u064A') // ا و ي
    private val HAMZA_BASES = mapOf(
        '\u0623' to '\u0621', '\u0625' to '\u0621', '\u0622' to '\u0621',
        '\u0624' to '\u0621', '\u0626' to '\u0621',
    )

    /** Canonical (search-tolerant) form of one word. */
    fun normalizeWord(word: String): String = ArabicText.normalizeForSearch(word).trim()

    /**
     * Estimates the triliteral (or biliteral) root skeleton of [word].
     * Returns an empty string for empty input.
     */
    fun deriveRoot(word: String): String {
        var w = normalizeWord(word)
        if (w.isEmpty()) return ""

        // Strip prefixes repeatedly (longest first), keeping ≥ 3 base letters.
        var changed = true
        while (changed) {
            changed = false
            for (prefix in PREFIXES) {
                if (w.length > prefix.length + 2 && w.startsWith(prefix)) {
                    w = w.removePrefix(prefix)
                    changed = true
                    break
                }
            }
        }

        // Strip suffixes repeatedly (longest first), keeping ≥ 2 base letters.
        changed = true
        while (changed) {
            changed = false
            for (suffix in SUFFIXES) {
                if (w.length > suffix.length + 1 && w.endsWith(suffix)) {
                    w = w.dropLast(suffix.length)
                    changed = true
                    break
                }
            }
        }

        // Fold hamza variants to their base form.
        w = w.map { HAMZA_BASES[it] ?: it }.joinToString("")

        // Collapse doubled letters (شَدَّد → شدّد → شدد).
        val deduped = buildString(w.length) {
            for (c in w) {
                if (isEmpty() || last() != c) append(c)
            }
        }

        // Remove weak letters, then keep the first three consonants. If the
        // stem is weak-only (e.g. استوى → وى), fall back to the deduped stem
        // so the root is never empty.
        val consonants = deduped.filter { it !in WEAK }
        return if (consonants.isNotEmpty()) consonants.take(3) else deduped.take(3)
    }

    /**
     * All corpus words (already normalized) that share [word]'s root —
     * the derived inflections — excluding [word] itself, sorted by length
     * then lexicographically for a stable display.
     */
    fun sharedDerivations(word: String, corpusWords: Collection<String>, limit: Int = 24): List<String> {
        val root = deriveRoot(word)
        if (root.isEmpty()) return emptyList()
        val normalizedWord = normalizeWord(word)
        return corpusWords.asSequence()
            .map { normalizeWord(it) }
            .distinct()
            .filter { it != normalizedWord && it.isNotEmpty() && deriveRoot(it) == root }
            .sortedWith(compareBy<String> { it.length }.thenBy { it })
            .take(limit.coerceAtLeast(0))
            .toList()
    }
}
