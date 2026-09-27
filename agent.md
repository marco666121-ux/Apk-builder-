# AGENTS.md / Antideploy Configuration

## Project: APK Builder

APK Builder is an Android application built with Kotlin, Jetpack Compose, and Android Gradle Plugin (AGP). It allows creators to configure native Android WebView wrapper applications for any website, test them in a live WebView sandbox, and package them into signed APKs.

### Architecture Overview

1. **Android App (Current Project)**:
   - **Framework**: Android (minSdk 24, targetSdk 36, compileSdk 36)
   - **Language**: Kotlin 2.2.10
   - **UI Engine**: Jetpack Compose with Material 3 (M3)
   - **Local Storage**: Room Database (SQLite)
   - **Build Tool**: Gradle (Kotlin DSL)

2. **Antideploy Build Server Compatibility**:
   - The app includes built-in REST API contracts in `SettingsScreen` and `AndroidProjectGenerator` to communicate with an external Gradle build server (`POST /api/v1/build`).
   - If deploying a standalone APK Builder build server to Antideploy, provide a Node.js or Python container with Android SDK / commandlinetools installed, exposing port 8080.

### Build and Test Instructions

- **Assemble Debug APK**:
  ```bash
  gradle :app:assembleDebug
  ```

- **Assemble Release APK**:
  ```bash
  gradle :app:assembleRelease
  ```

- **Run Local JVM / Robolectric Unit Tests**:
  ```bash
  gradle :app:testDebugUnitTest
  ```

### Key Project Paths

- Root Gradle Settings: `/settings.gradle.kts`
- App Module Build: `/app/build.gradle.kts`
- Version Catalog: `/gradle/libs.versions.toml`
- Android Manifest: `/app/src/main/AndroidManifest.xml`
- App Entry Activity: `/app/src/main/java/com/example/MainActivity.kt`
- Project Generator: `/app/src/main/java/com/example/generator/AndroidProjectGenerator.kt`
- Build Engine: `/app/src/main/java/com/example/builder/ApkBuildEngine.kt`
- Database Layer: `/app/src/main/java/com/example/data/local/`
