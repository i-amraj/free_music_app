# Task 06: Lightweight Release APK Build, Version Check Fix & Portfolio Deployment

## Date / Time
September 2026

## Issue Description
- Create an optimized lightweight release APK (`<30 MB`) for sharing.
- Update `myPortFolio` files with `0.6.1` version info and update JSON.
- Verify in-app update notification flow.

## Changes Implemented
1. **App Size Optimization (`app/build.gradle.kts`)**:
   - Configured R8 resource shrinking (`isShrinkResources = true`) & code minification (`isMinifyEnabled = true`).
   - Enabled ABI splits (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) plus universal APK generation.
   - Result:
     - `app-foss-arm64-v8a-release.apk`: **26 MB** (Lightweight release APK)
     - `app-foss-universal-release.apk`: **73 MB** (Universal release APK)
2. **Portfolio Deployment (`myPortFolio/raj_music/`)**:
   - Copied `app-foss-universal-release.apk` to `myPortFolio/raj_music/raj_music.apk`.
   - Copied `app-foss-arm64-v8a-release.apk` to `myPortFolio/raj_music/raj_music_arm64.apk`.
   - Updated `myPortFolio/raj_music/update.json`: `versionCode: 29`, `versionName: "0.6.1"`.
   - Updated `myPortFolio/projects/raj-music.html` with dual download options (Universal 73 MB & Light ARM64 26 MB).
3. **In-App Update Check Logic (`MainActivity.kt` & `Updater.kt`)**:
   - Verified automated version comparison (`update.versionCode > BuildConfig.VERSION_CODE`).
   - Shows pop-up dialog with release notes & Settings badge notification whenever server has a newer version.

## Outcome
- Users get lightweight 26 MB APK download options and automated in-app update notifications for version 0.6.1.
