package com.sportcasterpro.app.feature.livestream.presentation

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.library.view.OpenGlView
import com.sportcasterpro.app.core.domain.model.MatchStatus
import com.sportcasterpro.app.core.domain.model.ScoreEvent
import com.sportcasterpro.app.feature.match.domain.usecase.ObserveMatchUseCase
import com.sportcasterpro.app.feature.match.domain.usecase.UpdateMatchStatusUseCase
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreEventRepository
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreboardController
import com.sportcasterpro.app.feature.scoreboard.domain.ScoringTeam
import com.sportcasterpro.app.feature.streaming.domain.StreamSettings
import com.sportcasterpro.app.feature.streaming.domain.StreamingEngine
import com.sportcasterpro.app.feature.streaming.service.StreamingForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LiveStreamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context,
    observeMatch: ObserveMatchUseCase,
    private val updateMatchStatus: UpdateMatchStatusUseCase,
    private val scoreEventRepository: ScoreEventRepository,
    val scoreboardController: ScoreboardController,
    val streamingEngine: StreamingEngine,
) : ViewModel() {

    private val matchId: String = checkNotNull(savedStateHandle["matchId"])
    private val hasScoreboardStarted = MutableStateFlow(false)
    private val streamSettings = MutableStateFlow(StreamSettings())

    val uiState: StateFlow<LiveStreamUiState> = combine(
        observeMatch(matchId),
        scoreboardController.state,
        hasScoreboardStarted,
        streamSettings,
        streamingEngine.stats,
    ) { match, scoreboard, started, settings, stats ->
        LiveStreamUiState(
            match = match,
            scoreboard = if (started) scoreboard else null,
            streamSettings = settings,
            streamStats = stats,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LiveStreamUiState())

    init {
        viewModelScope.launch {
            val match = observeMatch(matchId).filterNotNull().first()
            scoreboardController.start(match.sport, match.homeTeam, match.awayTeam)
            hasScoreboardStarted.value = true
        }
    }

    fun onPreviewReady(view: OpenGlView) = streamingEngine.attachPreview(view)

    fun onStreamSettingsChanged(settings: StreamSettings) {
        streamSettings.value = settings
    }

    fun startBroadcast() {
        streamingEngine.startStream(streamSettings.value)
        ContextCompat.startForegroundService(appContext, Intent(appContext, StreamingForegroundService::class.java))
    }

    fun endBroadcast() {
        streamingEngine.stopStream()
        appContext.stopService(Intent(appContext, StreamingForegroundService::class.java))
        viewModelScope.launch { updateMatchStatus(matchId, MatchStatus.COMPLETED) }
    }

    fun toggleRecording() {
        if (uiState.value.streamStats.isRecording) {
            streamingEngine.stopRecording()
        } else {
            streamingEngine.startRecording(buildRecordingFilePath())
        }
    }

    fun pauseRecording() = streamingEngine.pauseRecording()
    fun resumeRecording() = streamingEngine.resumeRecording()
    fun switchCamera() = streamingEngine.switchCamera()
    fun setZoom(level: Float) = streamingEngine.setZoom(level)

    fun addPoint(team: ScoringTeam, value: Int = 1) {
        scoreboardController.addPoint(team, value)
        recordScoreEvent(team, value)
    }

    fun removePoint(team: ScoringTeam, value: Int = 1) = scoreboardController.removePoint(team, value)
    fun nextPeriod() = scoreboardController.nextPeriod()
    fun swapTeams() = scoreboardController.swapTeams()
    fun resetScore() = scoreboardController.resetScore()
    fun startTimer() = scoreboardController.startTimer(viewModelScope)
    fun pauseTimer() = scoreboardController.pauseTimer()
    fun setTimerMinutes(minutes: Int) = scoreboardController.setTimer(minutes * 60_000L)

    private fun recordScoreEvent(team: ScoringTeam, pointsDelta: Int) {
        val match = uiState.value.match ?: return
        val state = scoreboardController.state.value
        val teamId = if (team == ScoringTeam.HOME) match.homeTeam.id else match.awayTeam.id
        viewModelScope.launch {
            scoreEventRepository.recordEvent(
                ScoreEvent(
                    id = UUID.randomUUID().toString(),
                    matchId = matchId,
                    teamId = teamId,
                    pointsDelta = pointsDelta,
                    periodLabel = state.periodLabel,
                    homeScoreAfter = state.homeScore,
                    awayScoreAfter = state.awayScore,
                    timestampEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    private fun buildRecordingFilePath(): String {
        val moviesDir = appContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "${moviesDir?.absolutePath}/SportCasterPro_${matchId.take(8)}_$timestamp.mp4"
    }

    override fun onCleared() {
        streamingEngine.release()
    }
}
