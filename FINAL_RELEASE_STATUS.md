# Vibes 1.17.0 release status

## Implemented

- Rich profile cover, avatar, bio, nickname, location, work, education, relationship and website fields
- Private realtime direct messages with block/message-policy enforcement
- Account deletion Edge Function and in-app confirmation
- Authorized Facebook Page feed sync plus signed Page webhook receiver
- Automatic app-open Instagram/Facebook fallback sync and realtime imported-feed refresh
- Ideas Lab submissions, voting, status tracking and realtime updates
- Account-status enforcement, authenticity review/appeal requests and identity badge state
- Premium entitlement foundation with a separate optional badge and no advertising SDK
- Stable/Early-access channels, release notices and server-controlled data-only feature flags
- Comfort UI refresh with calm light/dark palettes, larger touch targets and reduced motion
- Google Play flexible in-app update flow
- WebView origin/navigation/permission hardening and Android backup disabled
- Global Android secure-window capture blocking, private call notifications and personalized call watermarking
- Full in-app Privacy Policy and Terms
- Version 1.17.0 / code 19

## Live external setup still required

- Facebook Page ID/access token, Meta App Secret and webhook verify token
- Meta webhook subscription and any required App Review/Business Verification
- Firebase `google-services.json` and server credentials for killed-app push
- Public TURN host/TLS/shared secret for reliable relay calling
- Play Console listing, Data safety answers and signed production AAB
- A real Play/Stripe product plus server-side receipt verification before paid Premium is enabled
- Supabase Auth leaked-password protection must be enabled in the dashboard

Instagram import can operate with the already configured server token. Facebook auto-import stays safely inactive until genuine Page credentials are configured; no credential is included in this source package.

Authenticity review is functional at the request/status level. A moderator or trusted review service must make approval/disable decisions. No hidden face recognition is included, and accounts without a profile photo remain usable.
