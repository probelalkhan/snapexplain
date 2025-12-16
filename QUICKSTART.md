# Quick Setup Guide

## Firebase Configuration (REQUIRED)

1. **Replace `google-services.json`** with your actual Firebase config file (already partially done)
2. **Update Web Client ID**: Open Firebase Console → Authentication → Sign-in method → Google → Copy Web Client ID
3. **Update `strings.xml`**:
   ```xml
   <string name="default_web_client_id">YOUR_ACTUAL_WEB_CLIENT_ID</string>
   ```
4. **Enable Services in Firebase Console**:
   - Authentication (Google + Email/Password)
   - Firestore Database
   - Storage
   - Vertex AI (for Gemini)

## Run the App

```bash
./gradlew assembleDebug
# or just click Run in Android Studio
```

## Features Implemented

✅ Google Sign-In
✅ Email/Password Sign-In & Sign-Up
✅ Camera + Gemini AI Code Explanation
✅ Favorites, History, Profile
✅ Material Design 3 UI with Compose Previews
✅ MVI Architecture + Hilt DI

See README.md for full documentation.
