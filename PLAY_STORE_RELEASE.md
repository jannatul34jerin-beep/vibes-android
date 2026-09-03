# Vibes Play Store release checklist

- Package: `com.vibes.social`
- Release: 1.17.0 (`versionCode 19`)
- Target SDK: 36
- Upload format: Android App Bundle

Before release:

1. Add genuine Firebase configuration and test push notifications.
2. Deploy and test TURN from mobile and restricted networks.
3. Configure Facebook Page webhook credentials only as Supabase secrets.
4. Run account creation/deletion, private-message RLS, block/restrict/report and media tests.
5. Host the same Privacy Policy and Terms at stable public HTTPS URLs.
6. Complete Data safety for account/profile data, user content, messages, camera, microphone, push tokens, moderation and connected-platform data.
7. Increment `versionCode`, build a signed AAB and use Play App Signing.
8. Test Google Play's internal track; in-app updates only work for Play-installed eligible builds.
9. Create the Premium subscription product and connect server-side purchase verification before exposing a buy button.
10. Verify that the store listing does not imply that payment purchases identity verification.
11. Test `FLAG_SECURE` on supported Android versions: screenshots/recordings must be blocked and recent-app previews must not expose private content.
12. Confirm the privacy listing accurately says that separate physical-camera recording cannot be technically prevented.

The app can compile without Firebase configuration, but killed-app FCM behavior will not be available.
