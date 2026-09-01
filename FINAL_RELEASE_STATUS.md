# Vibes v10 Final Release Status

## Created and installed in this package
- Original Android upload signing key (`release/vibes-upload-key.jks`)
- Local signing credentials (`release/SIGNING_CREDENTIALS.txt`)
- Gradle release signing from `keystore.properties` or CI environment variables
- Original coturn shared secret inserted into the coturn server configuration
- Target SDK 36 and version 1.6.0 (versionCode 7)
- Release APK/AAB build workflow

## External identities that cannot be fabricated
- Firebase `google-services.json` (issued by Google for a real Firebase project)
- A public TURN server IP/DNS and TLS certificate

The app can be built and signed without Firebase. FCM killed-app notifications remain inactive until the genuine Firebase file is supplied. STUN calling remains available; reliable relay calling requires the coturn server to be deployed publicly.
