# Vibes changelog

## 1.17.0

- Added rich profiles, private direct conversations, account deletion and Ideas Lab voting.
- Added authorized Instagram professional-account import and Facebook Page sync/webhook foundations.
- Added Stories, saves, reactions and complete live RLS-backed social tables.
- Added human-reviewed authenticity requests and active-account enforcement; profile photos remain optional.
- Added a separate Premium entitlement/badge foundation without client-side self-granting or identity-badge sales.
- Added stable/early-access channels, data-only feature flags and Google Play flexible updates.
- Added calm Comfort UI, larger touch targets, system light/dark themes and reduced-motion support.
- Added Android `FLAG_SECURE`, private lock-screen call notifications and personalized in-call watermarking.
- Updated privacy, terms, deployment, security and Play release documentation.

## Important boundaries

- Facebook and Instagram data is imported only through official APIs with administrator/user authorization.
- Stored messages are member-only through RLS and TLS-protected, but are not marketed as end-to-end encrypted.
- Standard Android capture paths are blocked; no app can reliably detect a separate physical camera.
- Identity verification requires human review. Premium cannot purchase an identity decision.
