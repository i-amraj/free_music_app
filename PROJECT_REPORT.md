# Raj Music (InnerTune) - Project Detailed Report

Is report mein project ki working, integrated APIs, aur build pipeline ki step-by-step detail di gayi hai.

---

## 1. Project Architecture (Kese Work karta hai)

Raj Music ek **Material 3** based music player hai jo **YouTube Music** backend ka use karta hai. Iska development **Kotlin** aur **Jetpack Compose** (Modern Android UI framework) mein kiya gaya hai.

### Key Technical Stack:
- **UI Framework**: Jetpack Compose (Declarative UI).
- **Navigation**: Jetpack Compose Navigation.
- **Dependency Injection**: Hilt (Google's recommended DI).
- **Database**: Room (Offline storage ke liye).
- **Network**: Ktor (API calls ke liye).
- **Image Loading**: Coil.
- **Media Playback**: AndroidX Media3 (ExoPlayer).

---

## 2. APIs Identification (Integrated Services)

Project mein multiple service layers aur reverse-engineered APIs ka use kiya gaya hai:

### A. InnerTube API (Core YouTube Music)
Yeh sabse mahatvapurn API hai. Yeh koi public API nahi hai balki YouTube Music ke internal "InnerTube" system ko reverse-engineer karke banaya gaya hai.
- **Work**: Search, Browse (Home, Charts), Artist details, aur Player data fetch karna.
- **Modules**: `innertube` directory mein iski complete implementation hai.

### B. LRCLib API
- **Work**: Synced lyrics (lyrics jo gaane ke saath saath move hoti hain) provide karne ke liye.
- **Modules**: `lrclib` module mein iski logic hai.

### C. KuGou API
- **Work**: Secondary source for lyrics. Agar LRCLib par lyrics na milein, toh yeh fallback source ki tarah kaam karta hai.
- **Modules**: `kugou` module.

### D. Kizzy API
- **Work**: Discord Rich Presence support ke liye. Jab aap gaana sunte hain, toh aapke Discord status par "Listening to..." dikhane ke liye yeh use hota hai.
- **Modules**: `kizzy` module.

---

## 3. Music Fetching Pipeline (Gaana kese chalta hai)

App mein gaana fetch karne ke liye ek **Multi-Client Strategy** ka use kiya gaya hai taki koi bhi gaana block na ho:

1. **ANDROID_VR_NO_AUTH**: Sabse pehle bina login ke VR client ka use karke stream dhoondi jaati hai.
2. **IOS & ANDROID**: Agar VR fail hota hai, toh iOS ya standard Android client headers ka use hota hai.
3. **WEB_REMIX**: YouTube Music Web client ka fallback.
4. **TVHTML5**: Akhiri fallback jo lagbhag har baar kaam karta hai.

**Playback Engine**: AndroidX Media3 (ExoPlayer) ka use hota hai jo caching, silence skipping aur audio normalization handle karta hai.

---

## 4. Build Pipeline (CI/CD)

Project GitHub Actions ka use karta hai automatically APK build karne ke liye.

- **Workflow File**: `.github/workflows/build.yml`
- **Steps**:
    1. **Setup**: Ubuntu par JDK 17 (Zulu distribution) setup hoti hai.
    2. **Security**: Secrets se Keystore aur `google-services.json` decode kiya jaata hai.
    3. **Build**: `./gradlew assembleDebug` command se Debug APK generate hota hai.
    4. **Artifact**: Build hone ke baad APK ko GitHub par upload kar diya jaata hai taki user use download kar sake.

---

## 5. Project Structure

- **`app/`**: Main UI, ViewModels, aur Playback Service.
- **`innertube/`**: Core API logic for YouTube Music.
- **`lrclib/` / `kugou/`**: Lyrics providers.
- **`kizzy/`**: Discord integration.

---

**Summary**: Raj Music ek highly optimized app hai jo multiple fallback mechanisms ka use karke YouTube Music se track stream karta hai, bina kisi official API key ke.
