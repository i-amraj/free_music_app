# PKMusic Android - Work Process Documentation

## 📱 Project Overview

**PKMusic** is a free music streaming Android application built with modern Android development practices. The app allows users to search for songs, play music, and manage a playlist queue.

---

## 🏗️ Project Architecture

### Architecture Pattern: MVVM (Model-View-ViewModel)

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI Layer                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │   SearchScreen  │  │   PlayerScreen  │  │   MainActivity  │  │
│  │   (Compose)     │  │   (Compose)     │  │   (Compose)     │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
└───────────┼────────────────────┼────────────────────┼───────────┘
            │                    │                    │
            ▼                    ▼                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ViewModel Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐                       │
│  │  SearchViewModel│  │  PlayerViewModel│                       │
│  │  (StateFlow)    │  │  (StateFlow)    │                       │
│  └────────┬────────┘  └────────┬────────┘                       │
└───────────┼────────────────────┼────────────────────────────────┘
            │                    │
            ▼                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Repository Layer                            │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                  MusicRepository                         │    │
│  │  - Search tracks                                         │    │
│  │  - Get streaming URLs                                    │    │
│  │  - Cache management                                      │    │
│  └────────┬────────────────────────────┬───────────────────┘    │
└───────────┼────────────────────────────┼────────────────────────┘
            │                            │
            ▼                            ▼
┌───────────────────────────┐  ┌─────────────────────────────────┐
│     Network Layer         │  │      Player Layer               │
│  ┌───────────────────┐    │  │  ┌─────────────────────────┐    │
│  │ InnerTubeService  │    │  │  │   MusicPlayerManager    │    │
│  │ (iTunes API)      │    │  │  │   (Media3 ExoPlayer)    │    │
│  └───────────────────┘    │  │  └─────────────────────────┘    │
│  ┌───────────────────┐    │  │  ┌─────────────────────────┐    │
│  │ OkHttp Client     │    │  │  │   MediaSession          │    │
│  └───────────────────┘    │  │  └─────────────────────────┘    │
└───────────────────────────┘  └─────────────────────────────────┘
```

---

## 📁 Project Structure

```
pkmusic-android2/
├── app/
│   ├── build.gradle                 # App-level Gradle configuration
│   ├── proguard-rules.pro           # ProGuard rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml  # App manifest
│           ├── java/com/pkmusic/app/
│           │   ├── MainActivity.kt           # Main entry point
│           │   ├── PKMusicApp.kt             # Application class
│           │   │
│           │   ├── innertube/                # Network Layer
│           │   │   ├── InnerTubeService.kt   # iTunes API service
│           │   │   └── Models.kt             # Data models (Track, etc.)
│           │   │
│           │   ├── player/                   # Player Layer
│           │   │   └── MusicPlayerManager.kt # ExoPlayer management
│           │   │
│           │   ├── repository/               # Repository Layer
│           │   │   └── MusicRepository.kt    # Data repository
│           │   │
│           │   ├── viewmodel/                # ViewModel Layer
│           │   │   ├── SearchViewModel.kt    # Search state management
│           │   │   └── PlayerViewModel.kt    # Player state management
│           │   │
│           │   └── ui/                       # UI Layer
│           │       ├── theme/                # Material Design 3 theme
│           │       │   ├── Theme.kt
│           │       │   ├── Color.kt
│           │       │   └── Type.kt
│           │       └── screens/              # Compose screens
│           │           ├── SearchScreen.kt   # Search UI
│           │           └── PlayerScreen.kt   # Player UI
│           │
│           └── res/
│               ├── drawable/
│               │   └── pk_bg.jpg             # Background image
│               ├── mipmap-*/                 # App icons
│               └── values/
│                   ├── strings.xml
│                   └── themes.xml
│
├── build.gradle                     # Project-level Gradle config
├── gradle.properties                # Gradle properties
├── settings.gradle                  # Project settings
└── WORK_PROCESS.md                  # This documentation
```

---

## 🔧 Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Language** | Kotlin | Primary development language |
| **UI Framework** | Jetpack Compose | Declarative UI with Material Design 3 |
| **Architecture** | MVVM | Separation of concerns |
| **Async** | Kotlin Coroutines & Flow | Asynchronous operations |
| **Networking** | OkHttp | HTTP client for API calls |
| **Image Loading** | Coil | Async image loading for Compose |
| **Media Playback** | Media3 ExoPlayer | Audio streaming and playback |
| **Music API** | iTunes Search API | Free music search and streaming |
| **Dependency Injection** | Manual DI | Simple dependency management |

---

## 🎵 Music API Integration

### iTunes Search API

The app uses the iTunes Search API for music search and streaming:

**Base URL:** `https://itunes.apple.com`

**Endpoints:**
- **Search:** `/search?term={query}&media=music&limit=20`
- **Lookup:** `/lookup?id={id}`

**Response Format:**
```json
{
  "resultCount": 1,
  "results": [
    {
      "trackId": 123456789,
      "trackName": "Song Title",
      "artistName": "Artist Name",
      "collectionName": "Album Name",
      "previewUrl": "https://audio-ssl.itunes.apple.com/...",
      "artworkUrl100": "https://is1-ssl.mzstatic.com/...",
      "trackTimeMillis": 30000
    }
  ]
}
```

### Data Flow

```
User Search Query
       │
       ▼
┌──────────────────┐
│  SearchViewModel │
│  updateQuery()   │
│  search()        │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ MusicRepository  │
│  searchTracks()  │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐     ┌─────────────────┐
│ InnerTubeService │────▶│  iTunes API     │
│  search()        │     │  (HTTP GET)     │
└────────┬─────────┘     └─────────────────┘
         │
         ▼
┌──────────────────┐
│ List<Track>      │
│  - title         │
│  - artist        │
│  - thumbnailUrl  │
│  - downloadUrl   │
│  - duration      │
└──────────────────┘
```

---

## 🎵 Music Player Implementation

### MusicPlayerManager

The [`MusicPlayerManager`](app/src/main/java/com/pkmusic/app/player/MusicPlayerManager.kt) handles all audio playback:

```kotlin
class MusicPlayerManager(private val context: Context) {
    private val exoPlayer: ExoPlayer
    
    // State flows
    val isPlaying: StateFlow<Boolean>
    val currentTrack: StateFlow<Track?>
    val position: StateFlow<Long>
    val duration: StateFlow<Long>
    
    // Core functions
    suspend fun playTrack(track: Track)
    fun pause()
    fun resume()
    fun seekTo(position: Long)
    fun release()
}
```

### Playback Flow

```
User clicks track
       │
       ▼
┌──────────────────┐
│  PlayerViewModel │
│  playTrack()     │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│MusicPlayerManager│
│  playTrack()     │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│    ExoPlayer     │
│  setMediaItem()  │
│  prepare()       │
│  play()          │
└──────────────────┘
         │
         ▼
┌──────────────────┐
│  Audio Output    │
│  (Device Speaker)│
└──────────────────┘
```

---

## 🎨 UI Implementation

### SearchScreen

The [`SearchScreen`](app/src/main/java/com/pkmusic/app/ui/screens/SearchScreen.kt) displays:
- Colorful gradient header with app branding
- Search bar with Hindi placeholder text
- Background image with blur effect
- Track list with colorful cards
- Recent searches section

### PlayerScreen

The [`PlayerScreen`](app/src/main/java/com/pkmusic/app/ui/screens/PlayerScreen.kt) displays:
- Full-screen player with blurred album art background
- Album artwork
- Track title and artist
- Progress bar with seek functionality
- Playback controls (play/pause, next, previous)
- Shuffle and repeat controls
- Queue management

---

## 🔄 Build Process

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17+
- Android SDK (API 24+)
- Gradle 9.2.1

### Build Commands

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Build Output

```
app/build/outputs/apk/
├── debug/
│   └── app-debug.apk        # Debug build
└── release/
    └── app-release.apk      # Release build (if configured)
```

---

## 🐛 Debugging

### Logcat Tags

```bash
# Filter app logs
adb logcat -s "InnerTube" "PKMusic" "ExoPlayer" "PlayerViewModel" "MusicRepository"
```

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| App crashes on Android 11 | NewPipe Extractor uses API 33+ methods | Use iTunes API instead |
| Songs don't play | Missing streaming URL | Check `downloadUrl` in Track model |
| Protocol errors | HTTP/2 issues with some APIs | Use stable APIs like iTunes |
| Network errors | No internet permission | Check `INTERNET` permission in manifest |

---

## 📝 Development Workflow

### 1. Feature Development

```bash
# 1. Create feature branch
git checkout -b feature/new-feature

# 2. Make changes
# Edit files...

# 3. Build and test
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 4. Commit changes
git add .
git commit -m "Add new feature"

# 5. Push to remote
git push origin feature/new-feature
```

### 2. Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Add KDoc comments for public functions
- Keep functions small and focused

### 3. Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

---

## 🚀 Future Enhancements

### Planned Features

1. **Offline Mode**
   - Download songs for offline playback
   - Local database caching

2. **Playlists**
   - Create and manage playlists
   - Add/remove songs from playlists

3. **Favorites**
   - Mark songs as favorites
   - Quick access to favorite songs

4. **Lyrics**
   - Display song lyrics
   - Synced lyrics display

5. **Equalizer**
   - Audio equalizer settings
   - Preset sound profiles

6. **Background Playback**
   - Continue playback when app is minimized
   - Lock screen controls

---

## 📱 Device Compatibility

| Requirement | Minimum | Target |
|-------------|---------|--------|
| Android Version | API 24 (Android 7.0) | API 35 (Android 15) |
| Screen Size | Small (320dp) | Large (600dp+) |
| Architecture | ARM64-v8a | All |

---

## 🔐 Permissions

```xml
<!-- Required permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

---

## 📚 Key Files Reference

| File | Purpose |
|------|---------|
| [`MainActivity.kt`](app/src/main/java/com/pkmusic/app/MainActivity.kt) | Main entry point, sets up Compose UI |
| [`InnerTubeService.kt`](app/src/main/java/com/pkmusic/app/innertube/InnerTubeService.kt) | iTunes API integration |
| [`MusicPlayerManager.kt`](app/src/main/java/com/pkmusic/app/player/MusicPlayerManager.kt) | ExoPlayer audio playback |
| [`MusicRepository.kt`](app/src/main/java/com/pkmusic/app/repository/MusicRepository.kt) | Data layer abstraction |
| [`SearchViewModel.kt`](app/src/main/java/com/pkmusic/app/viewmodel/SearchViewModel.kt) | Search state management |
| [`PlayerViewModel.kt`](app/src/main/java/com/pkmusic/app/viewmodel/PlayerViewModel.kt) | Player state management |
| [`SearchScreen.kt`](app/src/main/java/com/pkmusic/app/ui/screens/SearchScreen.kt) | Search UI with Compose |
| [`PlayerScreen.kt`](app/src/main/java/com/pkmusic/app/ui/screens/PlayerScreen.kt) | Player UI with Compose |
| [`Models.kt`](app/src/main/java/com/pkmusic/app/innertube/Models.kt) | Data models |

---

## 🎯 Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd pkmusic-android2
   ```

2. **Open in Android Studio**
   - File → Open → Select project directory

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

5. **Run the app**
   - Open PKMusic on your device
   - Search for songs
   - Tap to play!

---

## 📧 Support

For issues or feature requests, please create an issue in the project repository.

---

**Made with ❤️ for music lovers**
