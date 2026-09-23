#!/usr/bin/env python3
"""Verify the v2 Reference Library migration against the legacy Kotlin corpus."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LEGACY = (
    ROOT
    / "feature/feature-reference/src/main/java/org/muslim/app/feature/reference/domain/IslamIntroContent.kt"
)
ASSET = ROOT / "feature/feature-reference/src/main/res/raw/reference_islam_v2.json"


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


def verify_chapter_coverage(book: dict[str, object], topic_ids: list[str]) -> list[str]:
    failures: list[str] = []
    chapters = book.get("chapters")
    if not isinstance(chapters, list) or not chapters:
        return ["Reference asset must define at least one chapter."]

    chapter_topic_ids = [
        topic_id
        for chapter in chapters
        for topic_id in chapter.get("topicIds", [])
    ]
    if chapter_topic_ids != list(dict.fromkeys(chapter_topic_ids)):
        failures.append("A topic is assigned to more than one chapter.")
    if set(chapter_topic_ids) != set(topic_ids):
        missing = sorted(set(topic_ids) - set(chapter_topic_ids))
        unknown = sorted(set(chapter_topic_ids) - set(topic_ids))
        if missing:
            failures.append(f"Topics missing from chapters: {', '.join(missing)}")
        if unknown:
            failures.append(f"Chapters reference unknown topics: {', '.join(unknown)}")
    return failures


def main() -> int:
    failures: list[str] = []
    if not LEGACY.exists() or not ASSET.exists():
        print("REFERENCE ASSET CHECK FAILED")
        print(f"- Missing legacy source or asset: {LEGACY} / {ASSET}")
        return 1

    legacy_topics = parse_legacy_topics(LEGACY.read_text(encoding="utf-8"))
    pack = json.loads(ASSET.read_text(encoding="utf-8"))
    if pack.get("schemaVersion") != 2:
        failures.append("reference_islam_v2.json must use schemaVersion=2.")

    book = pack.get("book")
    if not isinstance(book, dict):
        failures.append("reference_islam_v2.json must contain a book object.")
        book = {}

    asset_topics = book.get("topics")
    if not isinstance(asset_topics, list):
        failures.append("Reference book must contain a topics array.")
        asset_topics = []

    asset_by_id = {
        topic["id"]: comparable_asset_topic(topic)
        for topic in asset_topics
        if isinstance(topic, dict) and isinstance(topic.get("id"), str)
    }
    legacy_ids = [topic["id"] for topic in legacy_topics]
    migrated_ids = [topic["id"] for topic in asset_topics if topic.get("id") in set(legacy_ids)]
    if migrated_ids != legacy_ids:
        failures.append("Legacy Introduction to Islam topic order changed during migration.")

    for legacy_topic in legacy_topics:
        topic_id = legacy_topic["id"]
        if topic_id not in asset_by_id:
            failures.append(f"Legacy topic missing from v2 asset: {topic_id}")
        elif asset_by_id[topic_id] != legacy_topic:
            failures.append(f"Migrated legacy topic changed unexpectedly: {topic_id}")

    topic_ids = [topic["id"] for topic in asset_topics if isinstance(topic, dict)]
    failures.extend(verify_chapter_coverage(book, topic_ids))

    for topic in asset_topics:
        if topic.get("reviewStatus") not in {"Draft", "NeedsReview", "Reviewed"}:
            failures.append(f"Invalid reviewStatus for topic {topic.get('id')}.")
        if "citations" not in topic:
            failures.append(f"Topic {topic.get('id')} lacks citations metadata.")
        if "relatedTopicIds" not in topic:
            failures.append(f"Topic {topic.get('id')} lacks relatedTopicIds metadata.")

    if failures:
        print("REFERENCE ASSET CHECK FAILED")
        for failure in failures:
            print(f"- {failure}")
        return 1

    section_count = sum(len(topic["sections"]) for topic in asset_topics)
    paragraph_count = sum(
        len(section["paragraphs"])
        for topic in asset_topics
        for section in topic["sections"]
    )
    print(
        "REFERENCE ASSET CHECK PASSED "
        f"({len(asset_topics)} topics, {section_count} sections, {paragraph_count} paragraphs)"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
