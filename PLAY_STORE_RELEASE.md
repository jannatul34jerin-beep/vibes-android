# Vibes Play Store release checklist

- Package: `com.vibes.social`
- Target SDK: 36
- Minimum SDK: 26
- Release version: 1.4.0 (versionCode 5)
- Play upload format: Android App Bundle (`.aab`)

Before release:
1. Add `app/google-services.json` for FCM.
2. Configure production TURN credentials through a protected backend.
3. Test sign-up/login, image/video permissions, 1:1 audio/video call, group call, background push, report/block, and logout on real devices and mobile networks.
4. Create a Play App Signing/upload key and keep it outside source control.
5. Publish a privacy policy and complete Play Console Data safety declarations for account/profile data, user content, messages, camera, microphone, push token, and moderation/report data actually collected by the shipping build.
6. Build with `gradle bundleRelease` or the included GitHub Actions workflow.
