# Firebase/FCM setup for Vibes

Vibes v8 contains the Android FCM client code, but it intentionally does **not** contain a Firebase private key.

1. Create or open a Firebase project.
2. Add an Android app with package name `com.vibes.social`.
3. Download `google-services.json` and place it at `app/google-services.json`.
4. Build/install the app once. FCM will create a device token; Vibes saves the token in Android local storage and, after Supabase login, upserts it into `public.push_tokens`.
5. Server-side call pushes must be sent with Firebase HTTP v1 from a trusted server/Supabase Edge Function. Never put a service-account private key in the APK.

Expected high-priority data message keys:
- `type`: `incoming_call` or `incoming_group_call`
- `caller_name`
- `call_type`: `audio` or `video`
- `call_id` for 1:1 calls
- `room_id` for group calls
