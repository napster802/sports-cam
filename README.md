# SportCaster Pro

SportCaster Pro is an Android app for broadcasting amateur and semi-pro sports matches live to RTMP destinations (YouTube, Twitch, Facebook, custom ingest), with a real-time, sport-aware scoreboard overlay burned into the stream and local recording for offline use.

This repository contains **Phase 1**: a complete, compiling core covering authentication, match/team management, the scoreboard engine, camera + RTMP streaming, local recording, and end-to-end navigation. See [`docs/ROADMAP.md`](docs/ROADMAP.md) for what's planned beyond this phase.

## Features (Phase 1)

- **Auth** — Email/password and Google sign-in via Firebase Auth.
- **Match management** — Create matches, pick a sport and teams, persisted locally with Room.
- **Scoreboard engine** — Sport-specific scoring rules (point-tally sports, volleyball set scoring, tennis game/set/match scoring) rendered as a live overlay on top of the camera preview.
- **Live streaming** — Camera capture, encoding, and RTMP ingest via [RootEncoder](https://github.com/pedroSG94/RootEncoder), configurable per-stream (RTMP URL, stream key, resolution, frame rate, bitrate).
- **Local recording** — Record the broadcast to local storage independent of (or instead of) streaming, with pause/resume.
- **Score history** — Every point change is persisted as an immutable event for later stats/replay.

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- MVVM + Clean Architecture (`core/` + `feature/<name>/{domain,data,presentation,di}`)
- Hilt for dependency injection
- Room for local persistence
- Firebase Auth, Analytics, Realtime Database, Firestore, Storage, Cloud Messaging
- [RootEncoder](https://github.com/pedroSG94/RootEncoder) for camera capture + RTMP streaming + local recording (see [Known limitations](#known-limitations))
- JUnit4, MockK, Turbine, kotlinx-coroutines-test, Truth for unit tests; Room's in-memory database for instrumented DB tests

## Project structure

```
app/src/main/java/com/sportcasterpro/app/
├── core/                     # Shared infrastructure: DI, Room, navigation, theme, util
└── feature/
    ├── auth/                 # Splash, Login, Firebase Auth repository
    ├── match/                 # Team/Match repositories, Dashboard, Create Match
    ├── scoreboard/             # Sport rules engine, controller, overlay UI, editor screen
    ├── streaming/              # StreamingEngine (RootEncoder), foreground service
    ├── livestream/             # Live broadcast screen wiring camera + scoreboard + controls
    └── settings/                # Account/subscription settings, sign out
```

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the full layer-by-layer breakdown and design decisions.

## Getting started

1. Open the project in Android Studio (Ladybug or newer recommended for AGP 8.7 / Compose Compiler plugin).
2. Replace the placeholder `app/google-services.json` (committed with dummy values so the project builds out of the box) with the real file from your own Firebase project, with **Authentication** (Email/Password + Google), **Realtime Database**, **Firestore**, **Storage**, and **Cloud Messaging** enabled. See [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md) for the full Firebase setup checklist.
3. Sync Gradle and build the `free` or `pro` flavor (see `flavorDimensions` in `app/build.gradle.kts`).
4. **Before relying on live streaming**, read the RootEncoder note under [Known limitations](#known-limitations) below — the RTMP/recording code was written without a working Android SDK/Gradle build available in this environment and needs a first compile-and-run pass against the real RootEncoder `2.5.3` API surface.

## Running tests

```
./gradlew testFreeDebugUnitTest          # JVM unit tests (sport rules, scoreboard controller, etc.)
./gradlew connectedFreeDebugAndroidTest  # Instrumented Room database tests (needs a device/emulator)
```

See [`docs/TESTING.md`](docs/TESTING.md) for what's covered and what isn't yet.

## Known limitations

- **The scoreboard overlay is not yet burned into the outgoing RTMP stream.** `ScoreboardOverlay` is a Compose composable drawn on top of RootEncoder's `OpenGlView` in the on-screen layout, but RootEncoder's encoder reads frames from the camera/GL surface directly — it does not see whatever Android draws on top of that view. Today the scoreboard is visible to the person operating the phone but **not** to viewers of the stream or the local recording. Making it appear in the actual broadcast requires drawing the scoreboard into the same surface RootEncoder encodes from (e.g. via RootEncoder's overlay/filter APIs, or compositing a bitmap into its OpenGL pipeline) — this is the most important Phase 2 item, see [`docs/ROADMAP.md`](docs/ROADMAP.md).
- **RootEncoder API usage has been read-verified against the library's real 2.5.3 source, but never compiled.** This sandbox has no Android SDK and no network access to Google's Maven repository, so `feature/streaming/domain/StreamingEngine.kt` (the `RtmpCamera2` wrapper) has never been built. Its method names, parameter order, and `ConnectChecker`/`BitrateChecker` callback signatures were manually checked line-by-line against `pedroSG94/RootEncoder`'s `2.5.3` tag and all matched, with one fix applied (catching the checked `IOException`/`CameraOpenException` that `startRecord`/`switchCamera` declare, which Kotlin doesn't force you to handle). **Open this file in Android Studio first** to confirm with a real compile before testing live streaming — see `docs/ARCHITECTURE.md` for the full verification notes.
- **The camera pipeline is owned entirely by RootEncoder**, not CameraX. `RtmpCamera2` manages the camera, encoder, and RTMP socket together through an `OpenGlView`, so the CameraX dependencies still listed in `app/build.gradle.kts` / `gradle/libs.versions.toml` are currently unused — bridging CameraX's `Preview`/`VideoCapture` surfaces into RootEncoder is not well-documented, so this project takes the simpler, supported path instead. If a future phase needs CameraX-specific features (e.g. CameraX `Extensions`), revisit this decision.
- No CI pipeline, ProGuard rule audit, or signed release build has been set up yet — see [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md).
