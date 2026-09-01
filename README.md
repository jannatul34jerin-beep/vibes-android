# Vibes self-hosted TURN

Deploy this folder on a public Linux server with a static public IP and DNS name.
Open UDP/TCP 3478, TCP 5349 and UDP 49160-49200. Set a long random shared secret in `turnserver.conf` and store the same value as Supabase Edge Function secret `TURN_SHARED_SECRET`. Set `TURN_HOST` in Supabase to the public DNS name.

Do not put the shared secret in the Android app. The deployed `turn-credentials` Edge Function generates one-hour credentials for signed-in Vibes users.
