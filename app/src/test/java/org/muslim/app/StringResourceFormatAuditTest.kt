package org.muslim.app

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

/**
 * Static, repo-wide audit of every `stringResource(R.string.X, ...)` call.
 *
 * Passing a `String` to a `%d`/`%f` specifier throws
 * `IllegalFormatConversionException` at runtime and crashes the screen on
 * composition; passing an `Int` to `%s` is the reverse type mismatch. This
 * test parses the Kotlin sources and the module `strings.xml` files and fails
 * on any such mismatch, so the bug class is caught in CI instead of on device.
 *
 * Argument classification is deliberately conservative: only expressions that
 * can be typed with high confidence (string literals, `.toString()`, nested
 * `stringResource(...)`, known String-returning helpers, integer/decimal
 * literals, `.toInt()`/`.toFloat()`, and the `.count/.size/.length` properties)
 * participate. Everything else is skipped so the audit never false-positives.
 */
class StringResourceFormatAuditTest {

    @Test
    fun every_stringResource_call_passes_matching_argument_types() {
        val root = findRepoRoot()
        val modules = listOf(File(root, "app")) +
            File(root, "core").listFiles().orEmpty().filter { it.isDirectory } +
            File(root, "feature").listFiles().orEmpty().filter { it.isDirectory }

        val problems = mutableListOf<String>()
        for (module in modules.sortedBy { it.name }) {
            val specTable = parseSpecifierTable(module)
            val javaDir = File(module, "src/main/java")
            if (!javaDir.isDirectory) continue
            javaDir.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file -> auditFile(file, specTable, problems) }
        }

        assertWithMessage(
            problems.joinToString(prefix = "stringResource format mismatches (${problems.size}):\n", separator = "\n"),
        ).that(problems).isEmpty()
    }

    // --- Source scanning ----------------------------------------------------

    private fun auditFile(
        file: File,
        specTable: Map<String, List<Spec>>,
        problems: MutableList<String>,
    ) {
        val src = file.readText()
        var i = 0
        while (i < src.length) {
            if (src.startsWith("stringResource", i) &&
                isIdentifierBoundary(src, i - 1) &&
                isIdentifierBoundary(src, i + "stringResource".length)
            ) {
                val open = nextNonWhitespace(src, i + "stringResource".length)
                if (open >= 0 && src[open] == '(') {
                    val close = matchingCloseParen(src, open)
                    if (close != null) {
                        val body = src.substring(open + 1, close).trim()
                        // Only literal `R.string.NAME` keys are statically resolvable.
                        val keyMatch = RES_KEY.find(body)
                        if (keyMatch != null && keyMatch.range.first == 0) {
                            val name = keyMatch.groupValues[1]
                            val specs = specTable[name]
                            if (specs != null) {
                                val argsText = body.substring(keyMatch.range.last + 1).trim()
                                val args = if (argsText.isEmpty() || argsText == ",") {
                                    emptyList()
                                } else {
                                    splitTopLevelArgs(argsText.removePrefix(","))
                                }
                                checkCall(file, src, i, name, specs, args, problems)
                            }
                        }
                    }
                }
            }
            i++
        }
    }

    private fun checkCall(
        file: File,
        src: String,
        callOffset: Int,
        name: String,
        specs: List<Spec>,
        args: List<String>,
        problems: MutableList<String>,
    ) {
        // A no-arg call returns the raw format template (it is later applied
        // with String.format outside stringResource) — nothing to check.
        if (args.isEmpty()) return

        val line = src.take(callOffset).count { it == '\n' } + 1
        val rel = file.relativeTo(findRepoRoot()).path

        // Count check: a string with N consuming specifiers needs N args
        // (fewer throws MissingFormatArgumentException at runtime).
        val required = (specs.maxOfOrNull { it.position } ?: -1) + 1
        if (args.size != required) {
            problems += "$rel:$line — $name expects $required arg(s) but the call passes ${args.size} (${args.joinToString { "\"${it.trim()}\"" }})"
            return
        }

        specs.forEach { spec ->
            if (spec.kind == SpecKind.OTHER) return@forEach
            val arg = args.getOrNull(spec.position) ?: return@forEach
            val actual = classifyArg(arg) ?: return@forEach
            val mismatch = when {
                spec.kind == SpecKind.INT && actual == ArgKind.STRING ->
                    "String passed to %d — IllegalFormatConversionException (crash)"
                spec.kind == SpecKind.FLOAT && actual == ArgKind.STRING ->
                    "String passed to %f — IllegalFormatConversionException (crash)"
                spec.kind == SpecKind.INT && actual == ArgKind.FLOAT ->
                    "Float/Double passed to %d — IllegalFormatConversionException (crash)"
                spec.kind == SpecKind.STRING && (actual == ArgKind.INT || actual == ArgKind.FLOAT) ->
                    "numeric arg passed to %s — type mismatch"
                else -> null
            }
            if (mismatch != null) {
                problems += "$rel:$line — $name(${arg.trim()}): $mismatch"
            }
        }
    }

    // --- String-resource specifier extraction --------------------------------

    private fun parseSpecifierTable(module: File): Map<String, List<Spec>> {
        val table = mutableMapOf<String, List<Spec>>()
        val resDir = File(module, "src/main/res")
        // Default (Arabic) first, then English as a fallback for any key that
        // lives only in values-en.
        for (folder in listOf("values", "values-en")) {
            val stringsFile = File(resDir, "$folder/strings.xml")
            if (!stringsFile.isFile) continue
            for (m in STRING_ENTRY.findAll(stringsFile.readText())) {
                val name = m.groupValues[1]
                if (name !in table) {
                    table[name] = extractSpecifiers(m.groupValues[2])
                }
            }
        }
        return table
    }

    private fun extractSpecifiers(text: String): List<Spec> {
        val specs = mutableListOf<Spec>()
        var sequential = 0
        for (m in FORMAT_SPEC.findAll(text)) {
            val conversion = m.groupValues[2].last()
            val kind = when (conversion) {
                's', 'S' -> SpecKind.STRING
                'd', 'x', 'X', 'o' -> SpecKind.INT
                'f', 'e', 'E', 'g', 'G', 'a', 'A' -> SpecKind.FLOAT
                '%' -> continue        // literal percent, consumes no argument
                'n' -> continue        // newline, consumes no argument
                else -> SpecKind.OTHER // b/B, c/C, h/H, t/T — no type check
            }
            val positional = m.groupValues[1].removeSuffix("$").toIntOrNull()
            val position = if (positional != null) positional - 1 else sequential
            if (positional == null) sequential++
            specs += Spec(position, kind)
        }
        return specs
    }

    // --- Argument type classification ----------------------------------------

    private fun classifyArg(raw: String): ArgKind? {
        val a = raw.trim().trimEnd(',')
        if (a.isEmpty()) return null

        // String signals, strongest first.
        if (a.startsWith("\"")) return ArgKind.STRING
        if (a.contains(".toString()")) return ArgKind.STRING
        if (a.startsWith("stringResource(")) return ArgKind.STRING
        if (STRING_HELPERS.any { a.startsWith(it) }) return ArgKind.STRING

        // Int signals.
        if (a.matches(Regex("-?\\d+"))) return ArgKind.INT
        if (a.contains(".toInt()") || a.contains(".roundToInt()")) return ArgKind.INT
        if (INT_HELPERS.any { a.startsWith(it) }) return ArgKind.INT
        if (INT_PROPERTIES.any { a.endsWith(it) }) return ArgKind.INT

        // Float signals.
        if (a.matches(Regex("-?\\d+\\.\\d+"))) return ArgKind.FLOAT
        if (a.contains(".toFloat()") || a.contains(".toDouble()")) return ArgKind.FLOAT

        return null // unknown — do not flag
    }

    // --- Small parsing helpers -----------------------------------------------

    private fun splitTopLevelArgs(text: String): List<String> {
        val parts = mutableListOf<String>()
        val sb = StringBuilder()
        var depth = 0
        var inString = false
        var inChar = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                inString -> {
                    sb.append(c)
                    when {
                        c == '\\' && i + 1 < text.length -> { sb.append(text[i + 1]); i++ }
                        c == '"' -> inString = false
                    }
                }
                inChar -> {
                    sb.append(c)
                    when {
                        c == '\\' && i + 1 < text.length -> { sb.append(text[i + 1]); i++ }
                        c == '\'' -> inChar = false
                    }
                }
                c == '"' -> { inString = true; sb.append(c) }
                c == '\'' -> { inChar = true; sb.append(c) }
                c == '(' || c == '[' || c == '{' -> { depth++; sb.append(c) }
                c == ')' || c == ']' || c == '}' -> { depth--; sb.append(c) }
                c == ',' && depth == 0 -> { parts += sb.toString(); sb.setLength(0) }
                else -> sb.append(c)
            }
            i++
        }
        if (sb.isNotBlank()) parts += sb.toString()
        return parts
    }

    private fun matchingCloseParen(src: String, open: Int): Int? {
        var depth = 0
        var i = open
        var inString = false
        var inChar = false
        while (i < src.length) {
            val c = src[i]
            when {
                inString -> {
                    if (c == '\\') i++ else if (c == '"') inString = false
                }
                inChar -> {
                    if (c == '\\') i++ else if (c == '\'') inChar = false
                }
                c == '"' -> inString = true
                c == '\'' -> inChar = true
                c == '(' -> depth++
                c == ')' -> { depth--; if (depth == 0) return i }
            }
            i++
        }
        return null
    }

    private fun nextNonWhitespace(src: String, from: Int): Int {
        var i = from
        while (i < src.length && src[i].isWhitespace()) i++
        return if (i < src.length) i else -1
    }

    private fun isIdentifierBoundary(src: String, i: Int): Boolean =
        i < 0 || i >= src.length || !(src[i].isLetterOrDigit() || src[i] == '_')

    private fun findRepoRoot(): File {
        var dir: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        while (dir != null && !File(dir, "settings.gradle.kts").exists()) {
            dir = dir.parentFile
        }
        return checkNotNull(dir) { "repository root not found (no settings.gradle.kts)" }
    }

    // --- Model + tables ------------------------------------------------------

    private enum class SpecKind { STRING, INT, FLOAT, OTHER }
    private enum class ArgKind { STRING, INT, FLOAT }

    private data class Spec(val position: Int, val kind: SpecKind)

    /** `stringResource(` with a literal `R.string.NAME` immediately after. */
    private val RES_KEY = Regex("""R\.string\.([A-Za-z0-9_]+)""")

    /** `<string name="X">…</string>` — the project has no nested tags/CDATA. */
    private val STRING_ENTRY = Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)

    /**
     * Java `Formatter` conversion specifiers: `%s`, `%1$d`, `%1$.1f`, `%%`, …
     * Group 1 is the optional positional index (`1$`), group 2 the conversion
     * character.
     */
    private val FORMAT_SPEC = Regex("""%(\d+\$)?[-#+ 0,(<]*\d*(?:\.\d+)?[tT]?([a-zA-Z%])""")

    /** Functions known to return a String and used as format arguments. */
    private val STRING_HELPERS = listOf(
        "formatBytes(",
        "formatDistance(",
        "formatDistanceKm(",
        "formatGregorian(",
        "formatTime(",
        "formatDate(",
        "formatMinutes(",
        "formatWindow(",
        "cardinal(",
        "appLanguageName(",
    )

    /** Functions known to return an Int and used as format arguments. */
    private val INT_HELPERS = listOf("alphaPercent(")

    /** Property suffixes that are Int in this codebase. */
    private val INT_PROPERTIES = listOf(".count", ".size", ".length", ".number", ".index")
}
