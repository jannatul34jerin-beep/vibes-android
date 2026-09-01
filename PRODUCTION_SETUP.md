# Vibes production calling checklist

## Already implemented
- Supabase auth/social database and RLS
- 1:1 WebRTC audio/video calls
- Up to 4-person mesh group video calls
- Camera switch, mute/camera controls
- Background blur/virtual-background pipeline
- Native Android incoming-call notification bridge while the WebView process is alive
- `push_tokens` table in Supabase for future FCM device tokens
- TURN-ready `iceServers` configuration
- Play Store AAB GitHub Actions workflow

## External credentials still required
### TURN
Reliable calls across carrier NAT/firewalls require a real TURN relay account. Put short-lived TURN credentials in `app/src/main/assets/config.js` (`turnServers`) or fetch them from a protected Edge Function.

### Killed-app incoming-call push (FCM)
Android cannot reliably wake a killed app from Supabase Realtime alone. A Firebase project is required for Firebase Cloud Messaging (FCM). Add the app `com.vibes.social`, download `google-services.json`, and add it under `app/`. Then wire a `FirebaseMessagingService` to save tokens into `public.push_tokens` and show the same call notification. Server-side push should be sent by a protected Supabase Edge Function using Firebase HTTP v1 credentials stored as Supabase secrets.

Do not place Firebase service-account private keys or TURN admin secrets inside the APK.

## MediaPipe bundle
The current WebView background-effect pipeline still references MediaPipe Selfie Segmentation from its CDN. To fully vendor it, copy the official `@mediapipe/selfie_segmentation` package files into `app/src/main/assets/vendor/mediapipe/`, change `index.html` to load the local `selfie_segmentation.js`, and change `locateFile` in `app.js` to `vendor/mediapipe/${file}`. The package includes JS, WASM, and TFLite model files, so all files are needed.

## Build
Local debug APK: `gradle assembleDebug`
Release AAB: `gradle bundleRelease`
The included GitHub Actions workflow performs both when the project is pushed to a GitHub repository. Release signing must use your Play App Signing/upload key before Play Store publication.
