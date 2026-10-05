#!/usr/bin/env python3
"""Fetch Quran Foundation resource metadata without downloading religious text."""
from __future__ import annotations

import argparse
import base64
from collections import Counter
from datetime import datetime, timezone
import json
import os
from pathlib import Path
import sys
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, urlsplit
from urllib.request import Request, urlopen

API_BASE = "https://apis.quran.foundation/content/api/v4"
OAUTH_BASE = "https://oauth2.quran.foundation"
RESOURCE_COLLECTIONS = ("translations", "tafsirs")


def normalize_resources(payload: object, collection: str) -> list[dict[str, object]]:
    if collection not in RESOURCE_COLLECTIONS:
        raise ValueError(f"unsupported resource collection: {collection}")
    if not isinstance(payload, dict) or not isinstance(payload.get(collection), list):
        raise ValueError(f"Quran Foundation response must contain a {collection} array")

    records: list[dict[str, object]] = []
    seen_ids: set[int] = set()
    for index, raw in enumerate(payload[collection]):
        if not isinstance(raw, dict):
            raise ValueError(f"{collection} item {index} is not an object")
        resource_id = raw.get("id")
        name = raw.get("name")
        language_name = raw.get("language_name")
        if not isinstance(resource_id, int) or isinstance(resource_id, bool) or resource_id < 1:
            raise ValueError(f"{collection} item {index} has an invalid id")
        if resource_id in seen_ids:
            raise ValueError(f"{collection} contains duplicate resource id {resource_id}")
        if not isinstance(name, str) or not name.strip():
            raise ValueError(f"{collection} item {index} has no name")
        if not isinstance(language_name, str) or not language_name.strip():
            raise ValueError(f"{collection} item {index} has no language_name")
        translated_name = raw.get("translated_name")
        if not isinstance(translated_name, dict):
            translated_name = None
        else:
            translated_name = {
                key: translated_name[key]
                for key in ("name", "language_name")
                if isinstance(translated_name.get(key), str)
            }
        records.append({
            "id": resource_id,
            "name": name.strip(),
            "author_name": raw.get("author_name") if isinstance(raw.get("author_name"), str) else None,
            "slug": raw.get("slug") if isinstance(raw.get("slug"), str) else None,
            "language_name": language_name.strip().lower(),
            "translated_name": translated_name,
        })
        seen_ids.add(resource_id)
    return sorted(records, key=lambda row: (str(row["language_name"]), int(row["id"])))


def language_counts(records: list[dict[str, object]]) -> dict[str, int]:
    return dict(sorted(Counter(str(row["language_name"]) for row in records).items()))


def _read_json(url: str, headers: dict[str, str], data: bytes | None = None) -> object:
    request = Request(url, data=data, headers=headers, method="POST" if data is not None else "GET")
    try:
        with urlopen(request, timeout=30) as response:
            return json.loads(response.read())
    except HTTPError as error:
        error_type = "unknown"
        response_content_type = "unknown"
        if error.headers:
            content_type = error.headers.get("Content-Type", "").split(";", 1)[0].strip().lower()
            if content_type.replace("/", "").replace("-", "").isalnum():
                response_content_type = content_type
        try:
            error_payload = json.loads(error.read())
            candidate = (
                error_payload.get("type", error_payload.get("error"))
                if isinstance(error_payload, dict)
                else None
            )
            if isinstance(candidate, str) and candidate.replace("_", "").isalnum():
                error_type = candidate
        except (OSError, json.JSONDecodeError):
            pass
        # Report only a static path and allowlisted provider error type. Never
        # print the response body, query string, credentials, or access token.
        endpoint = urlsplit(url).path
        raise RuntimeError(
            "Quran Foundation request failed: "
            f"HTTP {error.code} ({error_type}) at {endpoint} "
            f"(response={response_content_type})"
        ) from None
    except URLError as error:
        raise RuntimeError(f"Quran Foundation request failed: {error.reason}") from None
    except json.JSONDecodeError:
        raise RuntimeError("Quran Foundation returned invalid JSON") from None


def request_access_token(client_id: str, client_secret: str) -> str:
    credentials = base64.b64encode(f"{client_id}:{client_secret}".encode("utf-8")).decode("ascii")
    payload = urlencode({"grant_type": "client_credentials", "scope": "content"}).encode("ascii")
    response = _read_json(
        f"{OAUTH_BASE}/oauth2/token",
        {"Authorization": f"Basic {credentials}", "Content-Type": "application/x-www-form-urlencoded"},
        payload,
    )
    token = response.get("access_token") if isinstance(response, dict) else None
    if not isinstance(token, str) or not token:
        raise RuntimeError("Quran Foundation OAuth response did not contain an access token")
    return token


def fetch_catalogues(client_id: str, client_secret: str) -> dict[str, object]:
    token = request_access_token(client_id, client_secret)
    headers = {"x-auth-token": token, "x-client-id": client_id, "Accept": "application/json"}
    resources: dict[str, list[dict[str, object]]] = {}
    for collection in RESOURCE_COLLECTIONS:
        url = f"{API_BASE}/resources/{collection}?language=en"
        resources[collection] = normalize_resources(_read_json(url, headers), collection)
    return {
        "schemaVersion": 1,
        "source": "Quran Foundation Content API v4",
        "fetchedAt": datetime.now(timezone.utc).isoformat(),
        "scope": "metadata_only",
        "contentDownloaded": False,
        "resources": resources,
        "languageCounts": {key: language_counts(value) for key, value in resources.items()},
        "reviewNote": (
            "The API catalogue is discovery metadata. A listed resource is not marked reviewed, "
            "licensed, complete, or installable without source-specific provenance and validation."
        ),
    }


def append_summary(path: str, report: dict[str, object]) -> None:
    resources = report["resources"]
    counts = report["languageCounts"]
    lines = [
        "## Quran Foundation source catalogue audit",
        "",
        "Metadata-only production API audit; no Quran or tafsir text was downloaded.",
        "",
        f"- Translation resources: {len(resources['translations'])}",
        f"- Tafsir resources: {len(resources['tafsirs'])}",
        f"- Translation languages listed: {len(counts['translations'])}",
        f"- Tafsir languages listed: {len(counts['tafsirs'])}",
        "- Resource entries remain candidates until their source metadata, review, rights, and 6,236-ayah coverage are verified.",
        "",
    ]
    with Path(path).open("a", encoding="utf-8") as summary:
        summary.write("\n".join(lines))


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args(argv)
    if os.environ.get("QF_ENV", "production").lower() != "production":
        parser.error("This audit requires the production Content API environment")
    client_id = os.environ.get("QF_CLIENT_ID", "")
    client_secret = os.environ.get("QF_CLIENT_SECRET", "")
    if not client_id or not client_secret:
        parser.error("QF_CLIENT_ID and QF_CLIENT_SECRET are required environment variables")
    try:
        report = fetch_catalogues(client_id, client_secret)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
        if summary_path:
            append_summary(summary_path, report)
        print(
            "Quran Foundation metadata audit complete: "
            f"{len(report['resources']['translations'])} translations, "
            f"{len(report['resources']['tafsirs'])} tafsirs, content downloaded: no."
        )
        return 0
    except (RuntimeError, ValueError, OSError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
