#!/usr/bin/env python3
"""Verify versioned Reference Library assets against their legacy Kotlin corpus."""

from __future__ import annotations

import json
import sys
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOMAIN = ROOT / "feature/feature-reference/src/main/java/org/muslim/app/feature/reference/domain"
RAW = ROOT / "feature/feature-reference/src/main/res/raw"


@dataclass(frozen=True)
class CorpusSpec:
    name: str
    legacy: Path | None
    asset: Path
    min_topics: int
    min_chapters: int
    min_revision: int
    required_topic_ids: frozenset[str] = frozenset()


NAMED_PROPHET_TOPIC_IDS = {
    "adam",
    "idris",
    "nuh",
    "hud",
    "salih",
    "ibrahim",
    "lut",
    "ismail",
    "ishaq",
    "yaqub",
    "yusuf",
    "ayyub",
    "shuayb",
    "musa",
    "harun",
    "dhul_kifl",
    "dawud",
    "sulayman",
    "ilyas",
    "alyasa",
    "yunus",
    "zakariyya",
    "yahya",
    "isa",
    "muhammad",
}


CORPORA = (
    CorpusSpec(
        name="Introduction to Islam",
        legacy=DOMAIN / "IslamIntroContent.kt",
        asset=RAW / "reference_islam_v2.json",
        min_topics=60,
        min_chapters=7,
        min_revision=5,
    ),
    CorpusSpec(
        name="Prophetic Biography",
        legacy=DOMAIN / "SiraContent.kt",
        asset=RAW / "reference_sira_v2.json",
        min_topics=80,
        min_chapters=8,
        min_revision=3,
    ),
    CorpusSpec(
        name="Stories of the Prophets",
        legacy=DOMAIN / "ProphetsContent.kt",
        asset=RAW / "reference_prophets_v2.json",
        min_topics=65,
        min_chapters=9,
        min_revision=3,
        required_topic_ids=frozenset(NAMED_PROPHET_TOPIC_IDS),
    ),
    CorpusSpec(
        name="The Companions",
        legacy=None,
        asset=RAW / "reference_companions_v2.json",
        min_topics=34,
        min_chapters=5,
        min_revision=1,
        required_topic_ids=frozenset(
            {
                "abu_bakr",
                "umar",
                "uthman",
                "ali",
                "bilal",
                "ammar",
                "sumayya",
                "musab",
                "salman",
                "ibn_masud",
                "ubayy",
                "muadh",
                "abu_hurayra",
                "ibn_abbas",
                "ibn_umar",
                "zayd_thabit",
                "asma",
                "umm_sulaym",
                "nusaybah",
            }
        ),
    ),
    CorpusSpec(
        name="Mothers of the Believers",
        legacy=None,
        asset=RAW / "reference_mothers_v2.json",
        min_topics=15,
        min_chapters=3,
        min_revision=1,
        required_topic_ids=frozenset(
            {
                "khadijah",
                "sawda",
                "aisha",
                "hafsa",
                "zaynab_khuzayma",
                "umm_salama",
                "zaynab_jahsh",
                "juwayriyya",
                "umm_habiba",
                "safiyya",
                "maymuna",
            }
        ),
    ),
    CorpusSpec(
        name="Ahl al-Bayt",
        legacy=None,
        asset=RAW / "reference_ahl_al_bayt_v2.json",
        min_topics=18,
        min_chapters=4,
        min_revision=1,
        required_topic_ids=frozenset(
            {
                "ali_household",
                "fatimah",
                "hasan",
                "husayn",
                "abbas",
                "hamza",
                "jafar",
                "aqil",
            }
        ),
    ),
    CorpusSpec(
        name="Rightly Guided Caliphs",
        legacy=None,
        asset=RAW / "reference_rashidun_v2.json",
        min_topics=25,
        min_chapters=6,
        min_revision=1,
        required_topic_ids=frozenset(
            {
                "abu_bakr_caliph",
                "umar_caliph",
                "uthman_caliph",
                "ali_caliph",
                "hasan_reconciliation",
            }
        ),
    ),
    CorpusSpec(
        name="Aqeedah and Foundations of Faith",
        legacy=None,
        asset=RAW / "reference_aqeedah_v2.json",
        min_topics=25,
        min_chapters=6,
        min_revision=1,
        required_topic_ids=frozenset(
            {
                "tawhid",
                "names_attributes",
                "faith_components",
                "angels",
                "revealed_books",
                "messengers",
                "final_prophet",
                "qadar",
                "resurrection",
                "judgment",
                "takfir_caution",
            }
        ),
    ),
    CorpusSpec(
        name="Quranic Sciences",
        legacy=None,
        asset=RAW / "reference_quran_sciences_v2.json",
        min_topics=25,
        min_chapters=6,
        min_revision=1,
        required_topic_ids=frozenset(
            {
                "quran_definition",
                "revelation",
                "preservation",
                "collection_abu_bakr",
                "uthman_codex",
                "seven_ahruf",
                "qiraat",
                "canonical_qiraat",
                "asbab_nuzul",
                "nasikh_mansukh",
                "tafsir_principles",
                "translation",
            }
        ),
    ),
    CorpusSpec(
        name="Hadith and Sunnah Sciences",
        legacy=None,
        asset=RAW / "reference_hadith_sciences_v2.json",
        min_topics=28,
        min_chapters=7,
        min_revision=1,
        required_topic_ids=frozenset(
            {
                "sunnah_authority",
                "isnad_matn",
                "sahih",
                "hasan",
                "daif",
                "fabricated",
                "mutawatir_ahad",
                "jarh_tadil",
                "hidden_defects",
                "takhrij",
                "grading_disagreement",
                "bukhari_muslim",
                "digital_hadith_verification",
            }
        ),
    ),
)


def call_blocks(source: str, token: str) -> list[str]:
    blocks: list[str] = []
    position = 0
    while True:
        start = source.find(token, position)
        if start < 0:
            return blocks

        opening = source.find("(", start)
        if opening < 0:
            raise ValueError(f"Missing opening parenthesis after {token}")

        depth = 0
        in_string = False
        escaped = False
        end = -1
        for index in range(opening, len(source)):
            char = source[index]
            if in_string:
                if escaped:
                    escaped = False
                elif char == "\\":
                    escaped = True
                elif char == '"':
                    in_string = False
                continue

            if char == '"':
                in_string = True
            elif char == "(":
                depth += 1
            elif char == ")":
                depth -= 1
                if depth == 0:
                    end = index + 1
                    break

        if end < 0:
            raise ValueError(f"Unbalanced call for {token}")
        blocks.append(source[start:end])
        position = end


def decode_string_at(source: str, quote: int) -> str:
    value: list[str] = []
    escaped = False
    for index in range(quote + 1, len(source)):
        char = source[index]
        if escaped:
            value.append({"n": "\n", "t": "\t", "r": "\r"}.get(char, char))
            escaped = False
        elif char == "\\":
            escaped = True
        elif char == '"':
            return "".join(value)
        else:
            value.append(char)
    raise ValueError("Unterminated Kotlin string literal")


def named_string(block: str, name: str) -> str:
    start = block.find(name)
    if start < 0:
        raise ValueError(f"Missing field {name}")
    equals = block.find("=", start + len(name))
    quote = block.find('"', equals + 1)
    if equals < 0 or quote < 0:
        raise ValueError(f"Malformed field {name}")
    return decode_string_at(block, quote)


def string_literals(block: str) -> list[str]:
    values: list[str] = []
    position = 0
    while position < len(block):
        quote = block.find('"', position)
        if quote < 0:
            break
        values.append(decode_string_at(block, quote))

        index = quote + 1
        escaped = False
        while index < len(block):
            char = block[index]
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                position = index + 1
                break
            index += 1
        else:
            raise ValueError("Unterminated Kotlin string literal")
    return values


def parse_legacy_topics(source: str) -> list[dict[str, object]]:
    topics: list[dict[str, object]] = []
    for topic_block in call_blocks(source, "RefTopic("):
        sections: list[dict[str, object]] = []
        for section_block in call_blocks(topic_block, "RefSection("):
            paragraphs: list[dict[str, str]] = []
            for paragraph_block in call_blocks(section_block, "RefParagraph("):
                values = string_literals(paragraph_block)
                if len(values) < 2:
                    raise ValueError("RefParagraph must contain Arabic and English text")
                paragraphs.append({"ar": values[0], "en": values[1]})

            sections.append(
                {
                    "id": named_string(section_block, "id"),
                    "titleAr": named_string(section_block, "titleAr"),
                    "titleEn": named_string(section_block, "titleEn"),
                    "paragraphs": paragraphs,
                }
            )

        topics.append(
            {
                "id": named_string(topic_block, "id"),
                "titleAr": named_string(topic_block, "titleAr"),
                "titleEn": named_string(topic_block, "titleEn"),
                "summaryAr": named_string(topic_block, "summaryAr"),
                "summaryEn": named_string(topic_block, "summaryEn"),
                "sections": sections,
            }
        )
    return topics


def comparable_asset_topic(topic: dict[str, object]) -> dict[str, object]:
    sections = []
    for raw_section in topic["sections"]:
        section = dict(raw_section)
        sections.append(
            {
                "id": section["id"],
                "titleAr": section["titleAr"],
                "titleEn": section["titleEn"],
                "paragraphs": [
                    {"ar": paragraph["ar"], "en": paragraph["en"]}
                    for paragraph in section["paragraphs"]
                ],
            }
        )
    return {
        "id": topic["id"],
        "titleAr": topic["titleAr"],
        "titleEn": topic["titleEn"],
        "summaryAr": topic["summaryAr"],
        "summaryEn": topic["summaryEn"],
        "sections": sections,
    }


def verify_chapter_coverage(
    corpus_name: str,
    book: dict[str, object],
    topic_ids: list[str],
) -> list[str]:
    failures: list[str] = []
    chapters = book.get("chapters")
    if not isinstance(chapters, list) or not chapters:
        return [f"{corpus_name}: reference asset must define at least one chapter."]

    chapter_topic_ids = [
        topic_id
        for chapter in chapters
        for topic_id in chapter.get("topicIds", [])
    ]
    if chapter_topic_ids != list(dict.fromkeys(chapter_topic_ids)):
        failures.append(f"{corpus_name}: a topic is assigned to more than one chapter.")

    if set(chapter_topic_ids) != set(topic_ids):
        missing = sorted(set(topic_ids) - set(chapter_topic_ids))
        unknown = sorted(set(chapter_topic_ids) - set(topic_ids))
        if missing:
            failures.append(
                f"{corpus_name}: topics missing from chapters: {', '.join(missing)}"
            )
        if unknown:
            failures.append(
                f"{corpus_name}: chapters reference unknown topics: {', '.join(unknown)}"
            )
    return failures


def verify_topic_metadata(
    corpus_name: str,
    topic: dict[str, object],
    legacy_ids: set[str],
) -> list[str]:
    failures: list[str] = []
    topic_id = topic.get("id")

    if topic.get("reviewStatus") not in {"Draft", "NeedsReview", "Reviewed"}:
        failures.append(f"{corpus_name}: invalid reviewStatus for topic {topic_id}.")

    citations = topic.get("citations")
    if not isinstance(citations, list):
        failures.append(f"{corpus_name}: topic {topic_id} lacks citations metadata.")
        citations = []
    elif topic_id not in legacy_ids and not citations:
        failures.append(
            f"{corpus_name}: expanded topic {topic_id} must declare at least one source."
        )

    citation_ids = {
        citation.get("id")
        for citation in citations
        if isinstance(citation, dict) and citation.get("id")
    }
    for section in topic.get("sections", []):
        referenced = list(section.get("citationIds", []))
        for paragraph in section.get("paragraphs", []):
            referenced.extend(paragraph.get("citationIds", []))
        for citation_id in referenced:
            if citation_id not in citation_ids:
                failures.append(
                    f"{corpus_name}: topic {topic_id} references missing citation "
                    f"{citation_id}."
                )

    if "relatedTopicIds" not in topic:
        failures.append(f"{corpus_name}: topic {topic_id} lacks relatedTopicIds metadata.")
    return failures


def load_books() -> dict[str, dict[str, object]]:
    books: dict[str, dict[str, object]] = {}
    for asset in sorted(RAW.glob("reference_*_v2.json")):
        pack = json.loads(asset.read_text(encoding="utf-8"))
        book = pack.get("book")
        if isinstance(book, dict) and isinstance(book.get("id"), str):
            books[book["id"]] = book
    return books


def verify_relations(books: dict[str, dict[str, object]]) -> list[str]:
    failures: list[str] = []
    topic_ids = {
        book_id: {topic["id"] for topic in book.get("topics", [])}
        for book_id, book in books.items()
    }

    for book_id, book in books.items():
        for topic in book.get("topics", []):
            for relation in topic.get("relatedTopicIds", []):
                if "/" in relation:
                    target_book, target_topic = relation.split("/", 1)
                else:
                    target_book, target_topic = book_id, relation
                if target_topic not in topic_ids.get(target_book, set()):
                    failures.append(
                        f"{book_id}/{topic.get('id')}: missing related topic {relation}."
                    )
    return failures


def verify_corpus(
    spec: CorpusSpec,
    books: dict[str, dict[str, object]],
) -> tuple[list[str], str]:
    failures: list[str] = []
    if not spec.asset.exists():
        return (
            [f"{spec.name}: missing reference asset."],
            f"{spec.name}: unavailable",
        )
    if spec.legacy is not None and not spec.legacy.exists():
        return (
            [f"{spec.name}: missing legacy source."],
            f"{spec.name}: unavailable",
        )

    legacy_topics = (
        parse_legacy_topics(spec.legacy.read_text(encoding="utf-8"))
        if spec.legacy is not None
        else []
    )
    pack = json.loads(spec.asset.read_text(encoding="utf-8"))
    if pack.get("schemaVersion") != 2:
        failures.append(f"{spec.name}: asset must use schemaVersion=2.")

    book = pack.get("book")
    if not isinstance(book, dict):
        return failures + [f"{spec.name}: asset must contain a book object."], spec.name

    asset_topics = book.get("topics")
    if not isinstance(asset_topics, list):
        return failures + [f"{spec.name}: book must contain a topics array."], spec.name

    asset_by_id = {
        topic["id"]: comparable_asset_topic(topic)
        for topic in asset_topics
        if isinstance(topic, dict) and isinstance(topic.get("id"), str)
    }
    legacy_ids = [topic["id"] for topic in legacy_topics]
    legacy_id_set = set(legacy_ids)
    migrated_ids = [
        topic["id"] for topic in asset_topics if topic.get("id") in legacy_id_set
    ]
    if migrated_ids != legacy_ids:
        failures.append(f"{spec.name}: legacy topic order changed during migration.")

    for legacy_topic in legacy_topics:
        topic_id = legacy_topic["id"]
        if topic_id not in asset_by_id:
            failures.append(f"{spec.name}: legacy topic missing from asset: {topic_id}.")
        elif asset_by_id[topic_id] != legacy_topic:
            failures.append(f"{spec.name}: migrated legacy topic changed: {topic_id}.")

    topic_ids = [
        topic["id"] for topic in asset_topics if isinstance(topic, dict)
    ]
    failures.extend(verify_chapter_coverage(spec.name, book, topic_ids))

    missing_required_topics = sorted(spec.required_topic_ids - set(topic_ids))
    if missing_required_topics:
        failures.append(
            f"{spec.name}: missing required topic coverage: "
            + ", ".join(missing_required_topics)
        )

    if len(asset_topics) < spec.min_topics:
        failures.append(
            f"{spec.name}: corpus regressed below {spec.min_topics} topics: "
            f"{len(asset_topics)}."
        )
    if len(book.get("chapters", [])) < spec.min_chapters:
        failures.append(
            f"{spec.name}: corpus must define at least {spec.min_chapters} chapters."
        )
    if book.get("contentRevision", 0) < spec.min_revision:
        failures.append(
            f"{spec.name}: contentRevision must be >= {spec.min_revision}."
        )

    for topic in asset_topics:
        failures.extend(verify_topic_metadata(spec.name, topic, legacy_id_set))

    section_count = sum(len(topic["sections"]) for topic in asset_topics)
    paragraph_count = sum(
        len(section["paragraphs"])
        for topic in asset_topics
        for section in topic["sections"]
    )
    summary = (
        f"{spec.name}: {len(asset_topics)} topics, "
        f"{section_count} sections, {paragraph_count} paragraphs"
    )
    if book.get("id") not in books:
        failures.append(f"{spec.name}: book is not discoverable by the asset loader.")
    return failures, summary


def main() -> int:
    books = load_books()
    failures: list[str] = []
    summaries: list[str] = []

    for spec in CORPORA:
        corpus_failures, summary = verify_corpus(spec, books)
        failures.extend(corpus_failures)
        summaries.append(summary)

    failures.extend(verify_relations(books))

    if failures:
        print("REFERENCE ASSET CHECK FAILED")
        for failure in failures:
            print(f"- {failure}")
        return 1

    print("REFERENCE ASSET CHECK PASSED")
    for summary in summaries:
        print(f"- {summary}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
