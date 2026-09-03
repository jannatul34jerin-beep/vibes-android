# Vibes production setup

1. For a new project, apply `schema.sql` first and then every file in `supabase/migrations` in filename order. For the linked existing project, apply only migrations not already recorded.
2. Deploy all functions under `supabase/functions`. Keep JWT verification enabled except for `facebook-page-webhook`, which authenticates Meta using `x-hub-signature-256`.
3. Configure server-only secrets in Supabase; never paste them into `config.js`, Android resources, screenshots, commits or the APK.
4. Complete `FACEBOOK_AUTO_IMPORT_SETUP.md`, `FIREBASE_SETUP.md` and `TURN_SETUP.md`.
5. Review Supabase security/performance advisors, Auth URL/rate-limit settings, Storage policies and function logs.
6. Test deletion, RLS isolation, blocked-message behavior, report handling, webhook signatures, expired Meta tokens and media upload on a staging account.
7. Complete Play Console Data safety, content rating, privacy URL and account-deletion declarations.
8. Enable Auth leaked-password protection in Supabase Dashboard → Authentication → Security.
9. Keep identity verification and Premium billing separate. A trusted backend may write moderation and entitlement fields; the Android client cannot.

Backend/realtime changes appear without reinstalling the app. Android code changes are delivered only through a signed Google Play release; the included Play update flow offers and completes an available flexible update.

Remote feature flags in this release contain booleans, rollout percentages and version gates only. They never deliver executable code. This supports staged future rollouts without creating a self-modifying-app risk.
