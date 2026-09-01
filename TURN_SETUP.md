# TURN setup for Vibes

Production WebRTC needs TURN for users behind strict carrier NAT/firewalls.

Use a TURN provider that supports time-limited credentials. Do not embed long-lived TURN admin secrets in the app.

In `app/src/main/assets/config.js`, the existing `turnServers` array accepts entries shaped like:

```js
{ urls: ['turn:YOUR_TURN_HOST:3478?transport=udp','turn:YOUR_TURN_HOST:3478?transport=tcp'], username: 'SHORT_LIVED_USERNAME', credential: 'SHORT_LIVED_PASSWORD' }
```

For a public release, fetch short-lived credentials from a protected backend/Edge Function after the user signs in.
