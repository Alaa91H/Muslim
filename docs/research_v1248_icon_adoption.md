# v1.24.8 Icon Adoption Review

The user supplied two square 1024px emblems: a full-colour circular mosque mark for the official application identity and a black-and-white version for status-bar use. The supplied images were not modified semantically. The colour source was transformed deterministically into transparent Android resources by excluding only edge-connected near-white JPEG background and retaining the circular emblem.

| Resource | Canvas | Opaque artwork bounds | Purpose | Result |
|---|---:|---:|---|---|
| `ic_muslim_launcher_foreground.png` | 512 × 512 | 304 × 304, centered | Adaptive launcher foreground | Entire ornamental circle lies within the 66/108-equivalent safe zone; no ring clipping or white corners. |
| `ic_muslim_notification_large.png` | 512 × 512 | 465 × 464, centered | Large Adhan and next-prayer notification artwork | Full circular ring is preserved with a transparent guard margin. |
| `ic_muslim_notification.xml` | 24dp vector | 1.2–22.8 viewport ring | Status-bar notification glyph | Monochrome circular crescent-and-mosque mark remains inside the status-bar viewport. |

The adaptive launcher uses a navy background beneath the transparent colour foreground so masks never expose the source image's former white corners. Its separate monochrome layer is provided for Android themed-icon rendering. All legacy launcher artwork and the prior large notification vector are removed from the source tree in this change.
