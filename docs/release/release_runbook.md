# Android CI and release runbook

All Android pull request checks, APK builds, and production releases run from
`.github/workflows/ci.yml`. No helper release script is required.

## Normal CI runs

Pull requests, pushes to `main`, and manual workflow runs execute the
localization-diff gate, module/content checks, unit tests, lint, Detekt, and the
API 26/36 emulator matrices. Once these pass, CI builds and verifies signed
phone and Wear OS APKs. The workflow retains only the APK files as a private,
seven-day Actions artifact; it does not create a GitHub Release.

Fork pull requests do not receive repository signing secrets. Their debug APKs
are signed by Android's debug key for build validation and cannot be used as
production updates.

## Production release

1. Merge the reviewed release commit into `main` and verify the complete CI run.
2. Confirm `CHANGELOG.md` contains a non-empty `## Muslim vX.Y.Z` section in
   English.
3. Create and push an annotated tag in the exact format `vMAJOR.MINOR.PATCH`,
   pointing to the intended commit already merged into `main`.
4. The tag workflow verifies the production content manifest and signing
   identity, then builds the phone and Wear APKs with the version derived from
   the tag.
5. CI verifies both APK signatures and publishes a draft GitHub Release with
   exactly the two versioned APK files. It checks the remote names and byte
   sizes before making the release public.
6. Inspect the completed run and the published release assets before announcing
   the release.

The workflow does not publish on branch pushes, pull requests, or manual runs.
Use a new version tag for corrections to an already published release.

## Required signing secrets

Configure these repository Actions secrets using the stable application key:

| Secret | Value |
|---|---|
| `SIGNING_KEYSTORE` | Base64 encoded JKS/PKCS12 keystore |
| `SIGNING_STORE_PASSWORD` | Keystore password |
| `SIGNING_KEY_ALIAS` | Signing key alias |
| `SIGNING_KEY_PASSWORD` | Key password |

Never put the keystore, passwords, or encoded key in Git or workflow logs.
Tag builds fail closed when the production signing identity is unavailable.

## Release assets

The public GitHub Release contains only:

- `Muslim-vX.Y.Z.apk` — phone and tablet build.
- `Muslim-Wear-vX.Y.Z.apk` — Wear OS build.

App Bundles, manifests, checksums, and build metadata are not uploaded to the
release. Play Store publication remains a separate process and requires its own
review and Play Console upload.

## Localization gate

CI strictly checks the resource keys changed by each commit. Source changes
must update the matching entries in each existing locale catalog, while
translation changes must preserve placeholders and avoid empty or copied
source text. `python scripts/localize.py --check` remains the full repository
audit and reports the older catalog backlog; its historical findings do not
prevent unrelated changes from building.
