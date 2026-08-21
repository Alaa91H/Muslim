# Contributing to Muslim

Thank you for your interest in contributing! **Muslim** is an open-source project that lives through community contributions — code, translations, and content.

## Ways to contribute

1. **Code** — report bugs (Issues), pick a task from the roadmap in [`PROJECT_PROMPT.md`](PROJECT_PROMPT.md), or propose an improvement via a Pull Request.
2. **Translation** — the UI text is fully separated from religious content and lives in per-language resource files (190+ locales). A community translation platform (Weblate/Crowdin) is planned.
3. **Religious content** — any content (adhkar, hadith, rulings) must come from a documented source and undergo specialist religious review. It is not accepted otherwise.

## Before opening a Pull Request

- Read [`PROJECT_PROMPT.md`](PROJECT_PROMPT.md) — the single source of truth for the project.
- Make sure the build passes:

  ```bash
  ./gradlew :app:assembleDebug testDebugUnitTest lintDebug
  ```

- Write unit tests for new logic — especially astronomical calculations, where accuracy is a hard requirement.
- Follow the module boundaries: feature modules depend only on core modules, never on each other.
- Document new code with KDoc where appropriate.
- Keep every user-facing string in the module's `values/strings.xml` (English) and `values-en/strings.xml`; new keys are propagated to all locales by `scripts/localize.py`.

## Code style

- 100% Kotlin with Jetpack Compose.
- Follow the existing naming and structure of the module you touch.
- Prefer small, focused commits with descriptive messages.
- Run `./gradlew lintDebug` on your module before submitting — the project gates on zero lint issues.

## Religious content review

Because this is a religious app, content review is separate from code review:

- Quran text must come from a documented source (the project uses the Tanzil Uthmani script).
- Hadith must be sourced and their grading preserved.
- Adhkar and rulings must be attributed to their sources.

Any contribution that adds or changes religious content should say so in the PR description so it can be routed to the review channel.

## Communication channels

- **Issues & PRs:** this repository.
- **Developer:** [alahus2591@gmail.com](mailto:alahus2591@gmail.com) · [t.me/Alaa91h](https://t.me/Alaa91h)
- A community contributor channel (Discord/Telegram/GitHub Discussions) will be announced.
