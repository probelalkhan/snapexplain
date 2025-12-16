# SnapExplain - AI-Powered Code Learning App

A professional Android application built for engineering students to learn code by capturing images and getting AI-powered explanations using Google's Gemini AI.

## Features

### 🚀 Core Functionality
- **Camera Integration**: Capture code snippets from books, screens, or whiteboards
- **AI-Powered Explanations**: Gemini AI analyzes code and provides detailed, beginner-friendly explanations
- **Smart Detection**: Automatically detects programming language
- **Roast Mode**: Funny, educational roasts when non-code images are uploaded
- **Offline Storage**: All explanations saved to Firebase Firestore for offline access

### 📱 Screens
1. **Splash Screen**: Animated welcome with auto-navigation based on auth state
2. **Login**: Google Sign-In with Firebase Authentication
3. **Home**: Camera capture, AI analysis, and explanation display with favorite/share actions
4. **Favorites**: View all favorited explanations with real-time updates
5. **History**: Chronological list of all analyzed code snippets
6. **Profile**: User statistics (total explanations, favorites, learning time, student score)

## Architecture

### Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (100% declarative UI)
- **Architecture**: MVI (Model-View-Intent) with Single Activity
- **Dependency Injection**: Hilt
- **Backend**: Firebase (Auth, Firestore, Storage, Vertex AI)
- **Image Loading**: Coil
- **Camera**: CameraX
- **Navigation**: Jetpack Navigation Compose

### Project Structure
```
app/src/main/java/dev/belalkhan/snapexplain/
├── core/base/              # Base MVI classes and Resource wrapper
├── data/
│   ├── model/              # Data models (Explanation, UserProfile)
│   └── repository/         # Repository pattern implementations
│       ├── AuthRepository.kt
│       ├── ExplanationRepository.kt
│       └── GeminiRepository.kt
├── di/                     # Hilt dependency injection modules
├── navigation/             # Navigation setup
├── ui/
│   ├── auth/               # Login screen + ViewModel
│   ├── favorites/          # Favorites screen + ViewModel
│   ├── history/            # History screen + ViewModel
│   ├── home/               # Home screen + ViewModel (main feature)
│   ├── main/               # Main screen with bottom navigation
│   ├── profile/            # Profile screen + ViewModel
│   ├── splash/             # Splash screen
│   └── theme/              # Material Design 3 theme
└── SnapExplainApplication.kt
```

## Firebase Setup

### Prerequisites
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your Firebase project

### Configuration Steps

#### 1. Download google-services.json
- In Firebase Console, go to Project Settings
- Download `google-services.json`
- Place it in the `app/` directory

#### 2. Enable Firebase Services
In Firebase Console, enable:
- **Authentication** → Google Sign-In method
- **Firestore Database** → Create database in production mode
- **Storage** → Create default bucket
- **Vertex AI** → Enable Vertex AI API in Google Cloud Console

#### 3. Configure OAuth 2.0
- Go to Google Cloud Console → APIs & Services → Credentials
- Find your Web Client ID
- Update `/app/src/main/res/values/strings.xml`:
  ```xml
  <string name="default_web_client_id">YOUR_ACTUAL_WEB_CLIENT_ID</string>
  ```

#### 4. Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /explanations/{explanationId} {
      allow read, write: if request.auth != null && 
        resource.data.userId == request.auth.uid;
      allow create: if request.auth != null && 
        request.resource.data.userId == request.auth.uid;
    }
  }
}
```

#### 5. Storage Security Rules
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /code_images/{userId}/{imageId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## Build & Run

### Requirements
- Android Studio Hedgehog or newer
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36
- Kotlin 2.0.21
- Gradle 8.11.2

### Build Steps
```bash
# Clone the repository
git clone <repository-url>
cd SnapExplain

# Add google-services.json to app/ directory

# Build the project
./gradlew build

# Run on device/emulator
./gradlew installDebug
```

## Dependencies

All dependencies are managed in `gradle/libs.versions.toml`:

- **Jetpack Compose**: UI framework
- **Hilt**: Dependency injection
- **Firebase**: Backend services (Auth, Firestore, Storage, Vertex AI)
- **CameraX**: Camera integration
- **Coil**: Image loading
- **Navigation Compose**: Navigation
- **Accompanist**: Permissions
- **Kotlin Coroutines**: Async operations

## Firestore Data Structure

### Users Collection
```
users/{userId}
  - uid: String
  - email: String
  - displayName: String
  - photoUrl: String
  - totalExplanations: Int
  - favoriteCount: Int
  - dailyLearningTime: Long
  - studentScore: Int
  - joinedDate: Timestamp
```

### Explanations Collection
```
explanations/{explanationId}
  - id: String
  - userId: String
  - imageUrl: String
  - codeSnippet: String
  - explanation: String
  - timestamp: Timestamp
  - isFavorite: Boolean
  - tags: List<String>
  - language: String
```

## Features for Workshop

This app demonstrates professional Android development best practices:

✅ **Clean Architecture**: Separation of concerns with data, domain, and presentation layers  
✅ **MVI Pattern**: Unidirectional data flow for predictable state management  
✅ **Dependency Injection**: Hilt for scalable and testable code  
✅ **Modern UI**: 100% Jetpack Compose with Material Design 3  
✅ **Reactive Programming**: Kotlin Flows for real-time updates  
✅ **Repository Pattern**: Clean data access abstraction  
✅ **Coroutines**: Async operations with structured concurrency  
✅ **Firebase Integration**: Authentication, Database, Storage, and AI  
✅ **Type Safety**: Sealed classes for state and navigation  
✅ **Professional Structure**: Production-ready codebase organization

## License

This project is created for educational purposes as part of an Engineering workshop.

## Notes

- Replace `YOUR_WEB_CLIENT_ID_HERE` in strings.xml with your actual Firebase Web Client ID
- Ensure all Firebase services are properly enabled in your Firebase project
- Test Google Sign-In thoroughly on both debug and release builds
- Add proper error handling and edge cases as needed for production use

---

**Built with ❤️ for Engineering Students**
