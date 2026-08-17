package org.muslim.app.feature.quran.domain

/**
 * Reader-only colour themes (PROJECT_PROMPT.md §4.6: وضع ليلي للقارئ +
 * «قراءة مريحة»). Independent of the app theme so a reader can read in
 * sepia/dark even when the app follows the system.
 */
enum class ReaderTheme {
    Light,
    Sepia,
    Dark,
}
