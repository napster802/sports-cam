# Build & Deployment Guide

## Prerequisites

- Android Studio Ladybug (2024.2) or newer — needed for the AGP 8.7 / Kotlin Compose Compiler plugin combination this project uses.
- JDK 17 (the project's `compileOptions`/`kotlinOptions` target `JavaVersion.VERSION_17`).
- A Firebase project (see below).
- Network access to Google's Maven repository and Maven Central — this sandbox did not have that, so this project has never actually been built. **The first thing to do after cloning is open it in Android Studio, let Gradle sync, and fix whatever the first compile pass surfaces** (see `README.md` → Known limitations for where problems are most likely: `StreamingEngine.kt`).

## Firebase setup

1. Create a Firebase project at https://console.firebase.google.com.
2. Add an Android app with package name `com.sportcasterpro.app` (the `free` flavor) and, if you want the `pro` flavor to also authenticate against the same project, add a second Android app with package name `com.sportcasterpro.app.pro` (see `applicationIdSuffix = ".pro"` in `app/build.gradle.kts`).
3. Download `google-services.json` and overwrite the placeholder already committed at `app/google-services.json` (it ships with dummy IDs/keys purely so the project builds before you've set up Firebase — never commit your real one over it without checking your `.gitignore`/repo visibility).
4. Enable the products this app uses:
   - **Authentication** → Sign-in method → Email/Password and Google.
   - **Realtime Database** (used by `FirebaseModule.provideFirebaseDatabase`).
   - **Firestore** (dependency is present; not yet wired to a repository in Phase 1 — see roadmap).
   - **Cloud Messaging** (`SportCasterMessagingService` is registered in the manifest).
   - **Storage** (dependency is present, for future logo/thumbnail uploads).
5. For Google Sign-In specifically, add your app's SHA-1 (debug and release) fingerprints in the Firebase console, and create an OAuth Web Client ID if `LoginViewModel`'s Google sign-in flow needs a server client ID — confirm against `AuthRepositoryImpl`/`LoginViewModel` for the exact client ID it expects.

## Build variants

The app has one flavor dimension, `tier`, with two flavors (`app/build.gradle.kts`):

| Flavor | Application ID | `BuildConfig.IS_PRO` |
|---|---|---|
| `free` | `com.sportcasterpro.app` | `false` |
| `pro` | `com.sportcasterpro.app.pro` | `true` |

Crossed with the two build types (`debug`, `release`), Gradle produces `freeDebug`, `freeRelease`, `proDebug`, `proRelease`. `SettingsScreen` reads `BuildConfig.IS_PRO` to show "Free" vs "Pro" — there is no in-app purchase/billing wiring yet (see roadmap).

```
./gradlew assembleFreeDebug
./gradlew assembleProRelease
```

## Signing

`app/build.gradle.kts` has a release signing scaffold wired up already — it's optional, so a fresh clone with no keystore still builds (just unsigned). To produce a signed release build:

1. Generate an upload keystore: `keytool -genkey -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias sportcaster-upload`.
2. Copy `keystore.properties.example` (repo root) to `keystore.properties` and fill in `storeFile` (absolute path to the `.jks` from step 1), `storePassword`, `keyAlias`, `keyPassword`. `keystore.properties` is gitignored — **never commit it**.
3. Alternatively (e.g. in CI, where a properties file is awkward), set the `SPORTCASTER_KEYSTORE_PATH` / `SPORTCASTER_KEYSTORE_PASSWORD` / `SPORTCASTER_KEY_ALIAS` / `SPORTCASTER_KEY_PASSWORD` environment variables — `app/build.gradle.kts` falls back to these if `keystore.properties` isn't present.
4. Once all four values resolve (from either source), `app/build.gradle.kts` registers a `release` signing config and wires it into `buildTypes.release` automatically — no further Gradle changes needed. Run `./gradlew assembleProRelease` and confirm the output APK is signed (`apksigner verify` or check for a `Signed by` entry in Android Studio's APK Analyzer).
5. Keep `isMinifyEnabled = true` / `isShrinkResources = true` (already set for `release`) and audit `app/proguard-rules.pro` once you've confirmed the app actually runs, since R8 can be unforgiving with reflection-based libraries (Firebase, Hilt, Room — Hilt and Room both ship their own consumer ProGuard rules, but RootEncoder's rules have not been verified here).

## Release checklist

- [ ] Confirm `StreamingEngine.kt` compiles against the real RootEncoder `2.5.3` AAR (first Android Studio build) — its API usage has been read-verified against the library's source (see `docs/ARCHITECTURE.md`) but never compiled.
- [ ] Confirm the scoreboard overlay (`ScoreboardOverlayRenderer`) actually appears, correctly positioned, in the outgoing RTMP stream and recorded file — implemented and read-verified against RootEncoder's source but never run on a device (see `docs/ARCHITECTURE.md`).
- [ ] Done: a release signing config (`app/build.gradle.kts`, `keystore.properties.example`) is wired up — still needs a real keystore and a verification pass that the resulting signed, minified build installs and runs.
- [ ] Run the full test suite: `./gradlew testFreeDebugUnitTest connectedFreeDebugAndroidTest`. (`testFreeDebugUnitTest` and `lintFreeDebug`/`detekt` already run in CI — see `.github/workflows/ci.yml` — but `connectedFreeDebugAndroidTest` needs a real device/emulator and isn't in CI yet.)
- [ ] Manually test the full flow on a real device with a real RTMP destination (an emulator's virtual camera and network characteristics are not representative of live streaming): sign in → create match → go live → score points → end stream → verify the recording file under app-specific `Movies/`.
- [ ] Verify all permissions in `AndroidManifest.xml` (`CAMERA`, `RECORD_AUDIO`, `POST_NOTIFICATIONS`, foreground service types `camera|microphone`) are requested/granted correctly on a target-SDK-35 device.
- [ ] Set up CI (not present yet) to run unit tests and lint on every push — see `docs/ROADMAP.md`.
