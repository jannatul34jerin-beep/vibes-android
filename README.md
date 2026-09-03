# Vibes Android 1.17

Vibes is a Bengali-first social Android app built as a native WebView shell with a Supabase backend.

## Release

- Application ID: `com.vibes.social`
- Version: `1.17.0` (`versionCode 19`)
- minSdk 26, target/compile SDK 36, Java 17
- Android Gradle Plugin 9.3.1

## Included

- Email authentication, rich customizable profiles, user search and follow/unfollow
- Native posts, Stories, Reels, saved posts, nested comments and reactions
- Resumable media upload, lazy images and off-screen video pausing
- Private direct messages protected by conversation-member RLS
- WebRTC audio/video and small group calls
- Android secure-window capture blocking, private lock-screen call notifications and personalized call watermarking
- Block, restrict, reports, privacy/message preferences and permanent account deletion
- Official Instagram professional-account import
- Official authorized Facebook Page import with signed webhook refresh
- Ideas Lab: feature proposals, community voting and transparent delivery status
- Human-reviewed authenticity requests, moderator-controlled account restrictions and appeals
- Optional Premium entitlement/badge foundation, kept separate from identity verification
- Stable/Early-access channels, data-only feature flags and release notices
- Calm Comfort UI with system light/dark theme, accessible focus and reduced-motion support
- Google Play flexible in-app updates
- Bengali/English and persistent appearance/accessibility settings

## Build

Open the folder in Android Studio with SDK 36, then build an APK or Android App Bundle. The GitHub Actions workflow builds a debug APK and unsigned release AAB after a push.

Before Play publication:

1. Add the genuine Firebase `app/google-services.json`.
2. Configure a public TURN relay for reliable calls.
3. Configure approved Meta Page credentials as described in `FACEBOOK_AUTO_IMPORT_SETUP.md`.
4. Test sign-up, deletion, media, message/call, report/block and update flows on real devices.
5. Use Play App Signing and keep signing, Firebase, TURN and Meta secrets outside source control.
6. Connect a payment provider with server-side receipt verification before enabling paid Premium.
7. Enable Supabase Auth leaked-password protection in the dashboard.

## Security boundaries

The browser-safe Supabase publishable key may be bundled in the client; secret/service-role and Meta access tokens must remain in Edge Function secrets. RLS is enabled for user data, webhook requests are HMAC-verified, WebView navigation is restricted to packaged assets, Android backup is disabled, standard Android screen capture is blocked, lock-screen call notifications are private and web camera/microphone grants are limited to the local app origin.

No online service can promise to be unhackable. Keep dependencies and Android releases current, rotate leaked credentials, review Supabase advisors/logs and respond to abuse reports.

`FLAG_SECURE` prevents standard Android screenshots, recording/casting and non-secure displays from receiving the app window. It cannot detect or stop a separate physical camera; active calls therefore include a personalized deterrence watermark without claiming impossible protection.

Profile photos are optional. Vibes never treats a missing photo or a user report alone as proof of impersonation. Identity badges require a human-reviewed authenticity process; Premium purchases use a different badge and cannot buy an identity decision.

## Meta boundary

Vibes does not scrape Facebook or Instagram and does not bypass Meta permissions. Only accounts/Pages explicitly connected by an authorized administrator can be imported through official APIs. Private profiles, arbitrary users and unsupported Meta features are intentionally unavailable.
