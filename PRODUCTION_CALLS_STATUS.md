# Vibes production call status — v1.5.0

Implemented:
- Supabase Realtime call signaling.
- 1:1 audio/video calls and small group video calls.
- Call filters/background effects and camera switching.
- Android FCM receiver and incoming-call notification bridge.
- Push token storage in `public.push_tokens`.
- Deployed authenticated `send-call-push` Edge Function.
- Deployed authenticated `turn-credentials` Edge Function.
- App automatically invokes push when a call starts.
- App automatically requests short-lived TURN credentials before creating peer connections.
- Self-hosted coturn deployment template.
- Conditional release signing and GitHub Actions APK/AAB build workflow.

External credentials still required for real production operation:
1. Firebase Android `google-services.json`.
2. Firebase service-account JSON stored server-side as `FIREBASE_SERVICE_ACCOUNT_JSON` in Supabase Edge Function secrets.
3. A public TURN hostname/server plus `TURN_HOST` and `TURN_SHARED_SECRET` stored as Supabase secrets.
4. A private Android release upload keystore for Play Store signing.

Never put Firebase service-account private keys, TURN shared secrets, or keystore passwords in the APK or repository.
