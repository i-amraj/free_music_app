# Task 04: Song Download Queue & Unified Offline/Downloaded Library

## Date / Time
September 2026

## Issue Description
1. Clicking download on player or song menu was showing a toast ("Downloading...") but tracks were not saved to database or added to download queue.
2. Offline songs and downloaded songs were separate concepts causing user confusion.
3. Users requested a single, unified "Downloaded" tab that shows active download progress (queue) at the top, system notifications, and all offline tracks.

## Changes Implemented
1. **`Player.kt` Download Trigger**:
   - Replaced stubbed Toast with database insertion (`LocalDatabase`) and ExoPlayer `DownloadService.sendAddDownload` call.
   - Dynamic icon states (Download, Downloading/Spinning, Downloaded/Check).
2. **Context Menus (`YouTubeSongMenu.kt`, `MediaMetadataMenu.kt`, `SongMenu.kt`)**:
   - Updated `onDownload` lambdas to register track metadata in Room Database (`inLibrary(songId, timestamp)`) before enqueueing.
3. **Unified Library (`LibraryViewModels.kt` & `NavigationBuilder.kt`)**:
   - Combined `SongFilter.LIBRARY` and `SongFilter.DOWNLOADED` into a single flow streaming all downloaded and offline tracks in one tab.
   - Active download progress bar & queue displayed at top of Downloaded tab.

## Outcome
- Real-time download queue, system notifications, and single unified Downloaded section displaying all saved offline tracks.
