# Architecture

## Layering

The app follows Clean Architecture split by feature, with a shared `core/` module for cross-cutting infrastructure:

```
core/
├── data/local/          Room database, DAOs, entities (+ entity <-> domain mappers)
├── di/                  App-wide Hilt modules (DatabaseModule, FirebaseModule, DispatcherModule)
├── domain/model/        Plain Kotlin domain models shared across features (Match, Team, Sport, ScoreEvent, ...)
├── domain/util/         DispatcherProvider, Resource<T>
├── messaging/           FCM service
├── navigation/          Screen routes + NavHost graph
└── ui/                  Theme, shared composables (SportCasterTopBar, FullScreenLoading, ...)

feature/<name>/
├── domain/              Repository interfaces, use cases, pure business logic
├── data/                Repository implementations (Room/Firebase-backed)
├── presentation/        ViewModels + Compose screens
└── di/                  Hilt @Binds modules wiring domain interfaces to data impls
```

Dependency direction is always `presentation -> domain <- data`; `presentation` and `data` never depend on each other directly, and `domain` has no Android framework dependencies (the one exception is `StreamingEngine`, discussed below, which is unavoidably tied to the Android camera/encoder stack).

## Key patterns

### MVVM
Every screen has a `@HiltViewModel` exposing a single `StateFlow<XyzUiState>` built with `combine(...).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), XyzUiState())`. Screens are stateless Composables that read `uiState` and call ViewModel functions; no business logic lives in Composables.

### Repository pattern
Each feature exposes a domain-layer interface (e.g. `MatchRepository`, `ScoreEventRepository`) and a single Room-backed implementation, bound via a Hilt `@Binds` module (e.g. `MatchModule`, `ScoreboardModule`). This keeps Room/Firebase types out of `domain` and `presentation`.

### Strategy pattern for scoring
Different sports score fundamentally differently (running point totals vs. volleyball's set-based win-by-2 vs. tennis's game/set/match cascade). Rather than one branching scoring class, `feature/scoreboard/domain/SportRules.kt` defines the contract:

```kotlin
interface SportRules {
    fun initialState(sport: Sport, homeTeam: Team, awayTeam: Team): ScoreboardState
    fun addPoint(state: ScoreboardState, team: ScoringTeam, value: Int = 1): ScoreboardState
    fun removePoint(state: ScoreboardState, team: ScoringTeam, value: Int = 1): ScoreboardState
    fun nextPeriod(state: ScoreboardState): ScoreboardState
}
```

`SportRulesFactory` resolves the right implementation per `Sport`:

- `GenericPointRules` — running point/run total with a manually-advanced period. Used by basketball, football, futsal, badminton, table tennis, baseball, cricket, rugby, and hockey.
- `VolleyballRules` — best-of-5 sets, 25 (15 in set 5) points to win a set, win-by-2, auto-advances sets and detects match completion at 3 sets won.
- `TennisRules` — best-of-3 sets with game → set → match cascading completion logic.

`ScoreboardController` (one instance per live match, created via Hilt's `@Inject constructor` and *not* a singleton) owns the live `MutableStateFlow<ScoreboardState>`, delegates every mutation to the resolved `SportRules`, and additionally owns the match timer (`startTimer`/`pauseTimer`, ticking once per second via a cancellable `Job`).

Each point change is also written to Room as an immutable `ScoreEvent` (via `ScoreEventRepository`) purely for history/stats replay — this is a deliberate split from the in-memory `ScoreboardState`, which only reflects the *current* score, not its history.

### Dependency injection
Hilt modules are scoped per feature (`AuthModule`, `MatchModule`, `ScoreboardModule`) plus app-wide modules in `core/di`. `DispatcherProvider` is injected everywhere coroutines are launched from a ViewModel, rather than hardcoding `Dispatchers.IO`, to keep that code testable.

## Streaming pipeline

`feature/streaming/domain/StreamingEngine.kt` wraps RootEncoder's `RtmpCamera2`, which owns the camera, hardware encoder, and RTMP socket together via a `OpenGlView` surface (hosted in Compose through `AndroidView` in `LiveStreamScreen.kt`). `StreamingForegroundService` is a plain `Service` (not bound to the camera) that exists purely to keep the process alive and visible to the user via a notification while streaming/recording — `LiveStreamViewModel.startBroadcast()`/`endBroadcast()` start/stop it alongside the actual `StreamingEngine` calls.

**Design decision, not yet validated by a real build**: RootEncoder was substituted for the now-retired FFmpegKit (approved substitution). Because `RtmpCamera2` already owns the full camera→encoder→network pipeline, this project lets it own the camera directly instead of bridging a separate CameraX pipeline into it — CameraX→RootEncoder frame bridging is not a well-documented, supported configuration. The CameraX dependencies declared in `app/build.gradle.kts` are consequently unused by the streaming path; they were left in place from earlier scaffolding in case a future phase needs CameraX-specific capture features.

**This file has never been compiled, but its API usage has been read-verified against RootEncoder 2.5.3's actual source** (`RtmpCamera2.kt`, `Camera2Base.java`, `ConnectChecker.kt`, `BitrateChecker.java` on the `2.5.3` tag of [pedroSG94/RootEncoder](https://github.com/pedroSG94/RootEncoder)). `RtmpCamera2`'s constructor, the `prepareVideo`/`prepareAudio` overloads, `startStream`/`stopStream`, `startRecord`/`stopRecord`/`pauseRecord`/`resumeRecord`, `switchCamera`, `setZoom`, `isStreaming`/`isRecording`, and every `ConnectChecker`/`BitrateChecker` callback (`onConnectionStarted`, `onConnectionSuccess`, `onConnectionFailed`, `onNewBitrate`, `onDisconnect`, `onAuthError`, `onAuthSuccess`) all match the names, parameter order, and types used in `StreamingEngine.kt`. One real issue this review did surface: `startRecord` and `switchCamera` declare checked exceptions (`IOException`, `CameraOpenException`) that Kotlin doesn't force callers to handle — `StreamingEngine` now wraps both in `try`/`catch` and reports failures via `StreamStats.errorMessage` instead of letting them crash the app. This is still not the same guarantee as a real compile — open the file in Android Studio as the first build step and resolve anything this manual review missed (e.g. a point release difference, or a Gradle dependency resolution issue unrelated to the Kotlin source itself).

## Burning the scoreboard into the outgoing stream

`ScoreboardOverlay` (the Compose composable in `feature/scoreboard/presentation/`) is preview-only: it's drawn on top of RootEncoder's `OpenGlView` in the Compose view hierarchy, but RootEncoder reads frames from the camera/GL surface directly and never sees anything Android draws on top of that view. So a second, independent renderer — `feature/streaming/domain/ScoreboardOverlayRenderer.kt` — draws the scoreboard *into* RootEncoder's own GL pipeline:

1. `StreamingEngine.attachPreview` creates the `RtmpCamera2` and immediately calls `scoreboardOverlayRenderer.attach(rtmpCamera.glInterface)`, which adds a RootEncoder `SurfaceFilterRender` to the camera's filter chain via `GlInterface.addFilter`.
2. `SurfaceFilterRender` hands back a plain Android `Surface` once its GL texture is initialized (sized to the *full* encoded frame — its `getWidth`/`getHeight` come from the stream resolution, not from any bitmap of its own).
3. `ScoreboardOverlayRenderer.render(state)` locks that `Surface`'s `Canvas`, clears it to fully transparent, draws the scoreboard card with plain `Canvas`/`Paint` calls (mirroring `ScoreboardOverlay`'s design: team names/scores, period, timer, possession dot, sets row for tennis/volleyball) at a position derived from the canvas's own width/height (so it scales correctly across resolution presets), then posts it.
4. RootEncoder's `object_fragment.glsl` shader composites that texture over the camera frame with `mix(cameraPixel, scoreboardPixel, scoreboardPixel.a * uAlpha)` — confirmed by reading the actual shader source — so transparent canvas regions show pure camera frame and the scoreboard card blends at whatever alpha it's drawn with. This is what makes burn-in actually appear in the RTMP stream and any local recording, not just the preview.
5. `LiveStreamViewModel` drives this by collecting its own `uiState` in `init` and calling `streamingEngine.updateScoreboardOverlay(it.scoreboard)` on every emission — `viewModelScope`'s default `Dispatchers.Main.immediate` dispatcher satisfies RootEncoder's requirement that this `Surface` only be touched from the main thread.

This mechanism (`SurfaceFilterRender` + `GlInterface.addFilter`) and the shader's alpha-blending behavior have been read-verified against RootEncoder 2.5.3's actual source, but — like the rest of `feature/streaming/` — never compiled or run on a device. The Canvas-drawn visual design is a from-scratch reimplementation of `ScoreboardOverlay`'s look, not a literal screenshot of it, so expect to tweak proportions/colors once it's actually visible in a recorded clip.

## Data flow example: scoring a point during a broadcast

1. User taps "+1" for the home team in `LiveStreamControls` (`LiveStreamScreen.kt`).
2. `LiveStreamViewModel.addPoint(ScoringTeam.HOME)` is called.
3. It calls `scoreboardController.addPoint(team, value)`, which delegates to the resolved `SportRules.addPoint`, updating the in-memory `MutableStateFlow<ScoreboardState>`.
4. It also persists a `ScoreEvent` row via `ScoreEventRepository.recordEvent` (Room) for history/stats.
5. `LiveStreamViewModel.uiState` (a `combine` of the controller's state flow and others) emits the new state, which both recomposes the Compose `ScoreboardOverlay` preview and (per the section above) drives `ScoreboardOverlayRenderer` to redraw the burned-in version RootEncoder actually encodes.
