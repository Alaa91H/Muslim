# Islamic Finance Feature Sources

## Educational grounding

- **Qur'an 2:275** — trade is permitted and riba is prohibited. Source: <https://quran.com/al-baqarah/275>.
- **Qur'an 2:282** — deferred debts should be documented fairly; this supports the local debt-ledger feature. Source: <https://quran.com/al-baqarah/282>.
- **Jami` at-Tirmidhi 1209** — educational reference on truthfulness and trustworthiness in trade. Source: <https://sunnah.com/tirmidhi:1209>. The displayed grading should be checked by religious review before publication.

## Shariah-compliance data providers

- **Zoya API** — provides documented Shariah compliance data with a developer portal and sandbox. Its site states that a usable API requires a key and suitable personal/commercial licence. Source: <https://zoya.finance/api> and <https://blog.zoya.finance/introducing-the-zoya-api/>.
- **Musaffa API** — offers enterprise Shariah-compliance data for stocks and ETFs, including screening reports and status data; access is commercial and requires provider onboarding. Source: <https://musaffa.com/for-business/>.
- **Islamicly** — exposes Shariah screening as a consumer product but no public developer API was verified during this implementation. Source: <https://www.islamicly.com/>.

## Implementation decision

The app will provide provider-aware links and an honest “external verification required” result until the project owner supplies and approves a licensed API credential. It will not calculate a proprietary halal verdict from incomplete public data, and it will label results as information rather than investment advice.

Religious content must undergo the project’s specialist religious review before official release.
