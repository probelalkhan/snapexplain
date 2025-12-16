# SnapExplain - Security Notice

## 🔒 Before Committing to Public Repository

This project contains sensitive Firebase configuration. **IMPORTANT**: Ensure the following files are **NEVER** committed to version control:

### Critical Files to Exclude
1. **`app/google-services.json`** - Contains Firebase API keys and project configuration
2. **`local.properties`** - May contain local SDK paths
3. **Any keystore files** (`.jks`, `.keystore`) - Used for app signing
4. **`strings.xml`** - If it contains your `default_web_client_id`

### Setup for New Developers

1. **Firebase Configuration**:
   - Get `google-services.json` from Firebase Console
   - Place in `app/` directory (already gitignored)
   - Enable required services: Auth, Firestore, Storage, Vertex AI

2. **Web Client ID**:
   - Copy from Firebase Console → Authentication → Google Sign-In
   - Update in `app/src/main/res/values/strings.xml`:
     ```xml
     <string name="default_web_client_id">YOUR_ACTUAL_WEB_CLIENT_ID</string>
     ```

3. **Build Configuration**:
   - JDK 21 required
   - Kotlin 2.3.0
   - Min SDK: 26, Target SDK: 36

### What's Safe to Commit
✅ Source code files (`.kt`, `.xml` layouts)  
✅ Gradle configuration (excluding sensitive properties)  
✅ Resource files (drawables, etc.)  
✅ README and documentation  
✅ `.gitignore` file

### What's Excluded (see `.gitignore`)
❌ `google-services.json`  
❌ Keystore files  
❌ API keys and secrets  
❌ Build outputs  
❌ IDE-specific files

## Quick Start
See `QUICKSTART.md` and `README.md` for setup instructions.
