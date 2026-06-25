# Testing Strategy

## What's covered today

**JVM unit tests** (`app/src/test/...`, run with `./gradlew testFreeDebugUnitTest`):

| File | Covers |
|---|---|
| `feature/scoreboard/domain/rules/GenericPointRulesTest.kt` | Default point/period rules: scoring, score floor at zero, period advance → match completion. |
| `feature/scoreboard/domain/rules/VolleyballRulesTest.kt` | Set win at 25/15 points with win-by-2, no premature set win without a 2-point lead, possession tracking, match completion at 3 sets, no-op once match is complete. |
| `feature/scoreboard/domain/rules/TennisRulesTest.kt` | Game win at 4 points win-by-2, deuce, game/set cascading completion, the documented 6-6→7-games simplification, match completion at 2 sets (best-of-3), no-op once complete. |
| `feature/scoreboard/domain/ScoreboardStateTest.kt` | Tennis point-label formatting (`0/15/30/40`, `Deuce`, `AD`), non-tennis raw score display, period label formatting. |
| `feature/scoreboard/domain/SportRulesFactoryTest.kt` | Correct `SportRules` implementation resolved per `Sport`. |
| `feature/scoreboard/domain/ScoreboardControllerTest.kt` | `start`/`addPoint`/`removePoint`/`swapTeams`/`resetScore`/`setPossession` delegate correctly, and the countdown timer (using `kotlinx-coroutines-test`'s virtual time) counts down, stops at zero, and pauses without resetting remaining time. |

**Instrumented tests** (`app/src/androidTest/...`, run with `./gradlew connectedFreeDebugAndroidTest` against a device/emulator):

| File | Covers |
|---|---|
| `core/data/local/AppDatabaseTest.kt` | `TeamDao`/`MatchDao` joins (`MatchWithTeams`), `updateStatus`, `observeMatches` ordering, `ScoreEventDao` history ordering and per-match scoping, and the `ScoreEventEntity` → `MatchEntity` foreign-key **cascade delete**. Uses an in-memory Room database (`Room.inMemoryDatabaseBuilder`), so no real device storage is touched. |

This deliberately targets the parts of the codebase with the most non-trivial branching logic (the multi-sport scoring engine) and the part most likely to silently corrupt data if wrong (Room foreign keys/cascades) — these were the highest-value, lowest-risk-to-verify targets in this sandbox, which has no Android SDK and therefore could not compile a real instrumented build to confirm these tests actually pass.

## What isn't covered yet

- **ViewModels** (`LiveStreamViewModel`, `ScoreboardEditorViewModel`, `CreateMatchViewModel`, `DashboardViewModel`, `LoginViewModel`, `SettingsViewModel`) — no tests yet. These are good candidates for MockK + Turbine tests once the repositories they depend on are confirmed to compile; `LiveStreamViewModel` in particular pulls in `StreamingEngine`, which depends on the unverified RootEncoder integration (see `docs/ARCHITECTURE.md`), so writing tests for it now risks encoding assumptions about an API that hasn't been confirmed against the real library.
- **Repository implementations** (`MatchRepositoryImpl`, `TeamRepositoryImpl`, `AuthRepositoryImpl`, `ScoreEventRepositoryImpl`) — thin wrappers over Room/Firebase; worth a pass once `AppDatabaseTest` confirms the schema is solid, using the same in-memory-DB approach for the Room-backed ones and MockK for the Firebase-backed `AuthRepositoryImpl`.
- **Compose UI tests** — none yet. `androidx.compose.ui:ui-test-junit4` and `ui-test-manifest` are already in the version catalog/build file for when this is picked up.
- **`StreamingEngine` and `ScoreboardOverlayRenderer`** — not unit tested at all. Both are thin wrappers around third-party camera/encoder/GL APIs (`RtmpCamera2`, `SurfaceFilterRender`); the highest-value test here is a manual, on-device smoke test — for `ScoreboardOverlayRenderer` specifically, recording a short clip and confirming the scoreboard actually appears, correctly positioned, in the output file — not a unit test mocking RootEncoder's GL internals.
- **None of this has been run.** Every test above is believed correct by careful reading of the production code it tests, but this sandbox has no Android SDK and no network path to Google's Maven repository (confirmed by attempting `./gradlew testFreeDebugUnitTest`, which failed to even resolve the Android Gradle Plugin). Run the suite for real in Android Studio as the first verification step.

## Conventions

- Test names use backtick-quoted, sentence-style descriptions (e.g. `` `winning the third set completes the match` ``) rather than camelCase, for readability in test reports.
- Pure-Kotlin domain logic (sport rules, `ScoreboardState`, `SportRulesFactory`, `ScoreboardController`) is tested at the JVM level with no Android dependencies — these run fast and don't need an emulator.
- Anything touching Room's generated implementation (foreign keys, query correctness, `Flow` emission ordering) is tested as an instrumented test against a real (in-memory) SQLite database rather than mocked, since Room's SQL generation is exactly what needs verifying.
