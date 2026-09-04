# Task 05: App Version Update v0.6.1 & Emulator Installation

## Date / Time
September 2026

## Issue Description
- Update app version to `0.6.1` (`versionCode = 29`).
- Ensure emulator running app reflects new version `0.6.1` under Settings -> About screen.

## Changes Implemented
1. **`app/build.gradle.kts`**:
   - `versionCode = 29`
   - `versionName = "0.6.1"`
2. **`myPortFolio/raj_music/update.json`**:
   - Updated version code to 29, versionName to "0.6.1", and updated release notes.
3. **`myPortFolio/projects/raj-music.html`**:
   - Updated website download badge & text to v0.6.1.
4. **Gradle Clean & Rebuild**:
   - Executed `./gradlew clean assembleFossDebug` to purge old `BuildConfig` cache.
   - Installed `app-foss-debug.apk` on `emulator-5554`.
5. **Verification**:
   - Verified Settings -> About screen displaying `0.6.1 FOSS DEBUG`.

## Outcome
- App version updated to `0.6.1` and verified running on emulator.
