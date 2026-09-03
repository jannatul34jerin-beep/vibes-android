# Vibes security model

## Conversations

Direct-message rows are readable only when the signed-in user is a member of that conversation. Message creation checks the sender identity, conversation membership, account status, block state and the recipient's message preference. Anonymous database access is revoked.

Realtime delivery follows the same database RLS rules. Transport to Supabase uses TLS. WebRTC calls use encrypted transport and TURN credentials should be short-lived in production.

The Android window uses `FLAG_SECURE`, so standard Android screenshots, screen recording/casting and non-secure displays cannot receive the app window. Call notifications are private on the lock screen, and active calls show a personalized watermark to deter external-camera copying. A separate physical camera or compromised device cannot be reliably detected or blocked, so Vibes does not promise impossible capture prevention.

This release does not claim end-to-end encryption for stored direct messages. Supabase project administrators and trusted service-role systems can technically access server-side data. Do not market stored chats as E2EE until an audited protocol, device key lifecycle, multi-device recovery, key verification and abuse-report design are implemented.

## Accounts and trust

- Moderation fields and Premium entitlements are not writable by the Android client.
- Restricted accounts are blocked by restrictive RLS even if an older login token remains valid.
- Reports require human review; a report alone does not disable an account.
- Profile photos are optional and no hidden face recognition is performed.

## External sites

Facebook and Instagram imports use official APIs for explicitly authorized accounts only. Tokens remain in Edge Function secrets. Provider outages, token expiry, App Review or API changes may interrupt imports; the daily update watch and release process identify changes, but integrations must still be tested before deployment.
