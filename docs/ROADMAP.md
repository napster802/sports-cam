# Phase 2 Roadmap

Phase 1 (this repository, as of now) delivers a compiling core: auth, match/team management, the scoreboard engine, camera + RTMP streaming, and local recording, wired together end-to-end through navigation. It is one slice of the original, much larger SportCaster Pro spec. This document tracks what's deliberately deferred.

## Immediate priorities (do these first)

1. **Verify the RootEncoder integration compiles and runs.** `feature/streaming/domain/StreamingEngine.kt` has been read-verified line-by-line against RootEncoder 2.5.3's real source (see `docs/ARCHITECTURE.md`) — no API mismatches found, and one robustness fix applied (checked-exception handling on `startRecord`/`switchCamera`). What remains is a real compile-and-run pass in Android Studio, which this sandbox cannot do (no Android SDK, no network access to Google's Maven repo).
2. **Burn the scoreboard into the actual outgoing stream/recording, not just the local preview.** Done: `feature/streaming/domain/ScoreboardOverlayRenderer.kt` draws the scoreboard onto a RootEncoder `SurfaceFilterRender` added to the camera's `GlInterface`, composited into the encoded frame via alpha blending (read-verified against RootEncoder's real shader source, see `docs/ARCHITECTURE.md`). Still needs a real on-device pass to confirm the visual layout/proportions look right in an actual recorded clip — the Canvas-drawn design is a from-scratch reimplementation of the Compose `ScoreboardOverlay`, not a literal copy.
3. **Add a release signing config.** Done: `app/build.gradle.kts` conditionally registers a `release` signingConfig from `keystore.properties` (gitignored, template at `keystore.properties.example`) or `SPORTCASTER_*` env vars, falling back to an unsigned-but-buildable release if neither is present. Still needs a real keystore and a verification pass that the signed build installs and runs (see `docs/DEPLOYMENT.md`).
4. **Stand up CI** (GitHub Actions or similar) running `./gradlew testFreeDebugUnitTest detekt lint` on every push, since this codebase has not yet been verified by any compiler.

## Feature gaps vs. the original spec

- **More sports**: badminton/table tennis/cricket/rugby currently fall back to `GenericPointRules`, which is functionally fine but doesn't model sport-specific quirks (cricket overs/wickets, rugby tries vs. conversions, badminton's 21-point rally scoring with 2-point-lead caps). Add dedicated `SportRules` implementations as needed.
- **Multi-camera / camera switching UI**: `StreamingEngine.switchCamera()` exists but `LiveStreamScreen` has no UI control wired to it yet.
- **Stream health/diagnostics UI**: `StreamStats` already tracks bitrate and connection status; a dedicated diagnostics panel (dropped frames, latency, reconnect history) would help operators trust the stream.
- **WebRTC**: mentioned in the original spec for low-latency preview/monitoring; not started. Likely a second, parallel `StreamingEngine`-style component rather than a replacement for RTMP.
- **Cloud backup / cross-device sync of matches and team rosters**: Firestore dependency is present but no repository uses it yet — Room is currently the only persistence layer. Decide whether Firestore becomes the source of truth (with Room as an offline cache) or stays Realtime-Database-only.
- **VOD / cloud DVR**: recordings currently stay in local app-specific storage (`getExternalFilesDir(DIRECTORY_MOVIES)`); no upload-to-cloud-storage or playback-in-app flow exists. `media3-exoplayer`/`media3-ui` are already dependencies, presumably for this.
- **Social sharing / stream destinations management**: no UI for saving multiple named RTMP destinations (currently one ad-hoc `StreamSettings` entered per session in `LiveStreamScreen`'s settings dialog) or for posting clips/scores to social platforms.
- **Monetization / subscriptions**: the `free`/`pro` flavor split exists at the build level (`BuildConfig.IS_PRO`), but there's no Play Billing integration gating any actual feature — right now it's purely cosmetic in `SettingsScreen`.
- **Push notification content**: `SportCasterMessagingService` is registered but only receives messages; no notification-triggered UX (e.g. "match starting soon", "you're live") has been built.
- **Player/roster management UI**: `PlayerEntity`/`PlayerDao` exist in the Room schema but no domain model, repository, or screen consumes them yet — useful for box-score-style stats per player.
- **Stats/replay UI from `ScoreEvent` history**: events are persisted (`ScoreEventRepository`) but nothing reads them back yet — a post-match timeline/box-score screen is a natural next feature.
- **Accessibility and localization pass**: strings are centralized in `strings.xml` (good foundation) but no additional locales or accessibility audit (TalkBack labels beyond the basics, contrast checks) has been done.
- **Compose UI tests and ViewModel unit tests**: see `docs/TESTING.md` for the specific gaps.

## Suggested sequencing

1. Get a real build green (Immediate priorities #1, #3, #4) — this also gives the first real-device confirmation that the overlay burn-in (#2) actually looks right, since none of this has been compiled yet.
2. Round out sport rules and add the diagnostics/camera-switch UI (low risk, high polish).
3. Pick one cloud-sync story (Firestore vs. Realtime DB) deliberately rather than depending on both ambiguously.
4. Layer in monetization, social, and stats features once the broadcast path itself is trusted in production.
