# Project Detailed Documentation

## Project Overview
**Raj Music** (InnerTune clone) is a Material 3 Music Player/Downloader tailored for YouTube Music. It is a feature-rich, open-source Android application built with Jetpack Compose.

## Versions & Dependencies
The project relies on a modern Android tech stack, managed via Version Catalog (`libs.versions.toml`).

*   **Language**: Kotlin `2.0.10`
*   **UI Framework**: Jetpack Compose `1.6.8`
*   **Material Design**: Material3 `1.3.0-rc01`
*   **Media Playback**: AndroidX Media3 (ExoPlayer) `1.4.1`
*   **Networking**: Ktor `2.3.12`
*   **Dependency Injection**: Hilt `2.51.1`
*   **Database**: Room `2.6.1`
*   **Image Loading**: Coil `2.6.0`

## APIs & Logic

### Music Source (API)
The app doesn't use a standard public API but reverse-engineers the internal **YouTube Music API (InnerTube)**.
This logic is encapsulated in the `innertube` module.

*   **Core Class**: `com.zionhuang.innertube.YouTube`
*   **Networking**: Uses `Ktor` to send HTTP requests to YouTube endpoints.
*   **Logic**:
    *   **Browsing**: Fetches Home, Explore, New Releases, Charts.
    *   **Searching**: Supports searching for Songs, Videos, Albums, Artists, and Playlists (`search()`, `searchSuggestions()`).
    *   **Metadata**: Extracts detailed metadata (Title, Artist, Album, Duration, Lyrics, Related items).

### Music Fetching & Playback
To **fetch** and **play** music, the app employs a multi-client strategy to ensure reliability and bypass restrictions.

*   **Fetching Logic** (`YouTube.player` in `YouTube.kt`):
    *   It attempts to fetch playable streams using multiple internal YouTube clients in a specific fallback order:
        1.  **ANDROID_VR_NO_AUTH**: Used first as it often works without login (borrowed from OuterTune).
        2.  **IOS**: High success rate for some content.
        3.  **ANDROID**: The standard YouTube Android client context.
        4.  **WEB_REMIX**: The web client for YouTube Music.
        5.  **ANDROID_MUSIC**: Used if the user is logged in (cookies present).
        6.  **TVHTML5**: Final fallback if others fail.
    *   **Audio Quality**: It selects the best available audio stream (webm/opus preferred) based on user preference (Auto/High/Low) and network conditions.

### Playback Engine
The app uses **ExoPlayer** (Media3) for robust audio playback.

*   **Service**: `com.zionhuang.music.playback.MusicService` is the central hub.
*   **Player Setup**:
    *   **ExoPlayer**: Configured with `OkHttpDataSource` (supporting proxy) and `MatroskaExtractor`/`FragmentedMp4Extractor`.
    *   **Audio Processors**:
        *   `SilenceSkippingAudioProcessor`: Skips silent parts of songs.
        *   `SonicAudioProcessor`: Controls playback speed/pitch.
    *   **Normalization**: Implements audio normalization to keep volume consistent across tracks.
*   **Caching**: Uses `SimpleCache` (`playerCache` and `downloadCache`) to save bandwidth and enable offline play.
*   **Features**:
    *   **Gapless Playback**: Attempted via preloading.
    *   **Discord RPC**: Integrates with Discord to show "Now Playing" status.
    *   **Sleep Timer**: Integrated directly into the player service.

## Database & Persistence
*   **Database**: Room Database (`MusicDatabase`).
*   **Entities**: Stores `Song`, `Album`, `Artist`, `Playlist`, `Lyrics`, and `Format` (cached stream URLs).
*   **Preferences**: Uses `DataStore` for user settings (Theme, Audio Quality, etc.).

## File Structure & Organization
The project is organized by feature/layer within `app/src/main/java/com/zionhuang/music`.

### Core Directories
*   **`MainActivity.kt`**: The entry point of the application. Sets up the main UI theme and navigation.
*   **`App.kt`**: The Application class, initializing Hilt (dependency injection) and global configs (Coil, timber).

### Feature Packages
*   **`constants/`**: Holds global constants and Enum classes (e.g., `AudioQuality`, `AppConstants`).
*   **`db/`**: Room Database implementation.
    *   `entities/`: Defines database tables (`SongEntity`, `AlbumEntity`, `ArtistEntity`).
    *   `MusicDatabase.kt`: Abstract database class.
*   **`di/`**: Hilt modules for Dependency Injection (`AppModule`, `DatabaseModule`).
*   **`extensions/`**: Kotlin extension functions to simplify code (e.g., `PlayerExt.kt` for ExoPlayer extensions).
*   **`lyrics/`**: Logic for fetching and parsing lyrics (`LyricsHelper`).
*   **`models/`**: Data classes used across the app (mapping between Database, API, and UI).
*   **`playback/`**: The heart of the music player.
    *   `MusicService.kt`: Background service managing ExoPlayer.
    *   `queues/`: Logic for managing playback queues (playlist, radio, etc.).
    *   `PlayerConnection.kt`: Interface between UI and Service.
*   **`ui/`**: All Jetpack Compose UI code.
    *   `component/`: Reusable UI components (Buttons, Dialogs, Music Cards).
    *   `screens/`: Individual screens of the app.
        *   `HomeScreen.kt`: Main dashboard.
        *   `PlayerScreen.kt`: Full-screen player UI.
        *   `search/`: Search input and results.
        *   `settings/`: App configuration screens.
        *   `library/`: User's saved songs, albums, artists.
    *   `theme/`: App styling (Colors, Typography) using Material 3.
*   **`utils/`**: Utility helper classes (e.g., `ConnectivtyUtils`, `ImageUtils`).
*   **`viewmodels/`**: logic holders for UI screens (MVVM pattern), interacting with Repositories/Database.
