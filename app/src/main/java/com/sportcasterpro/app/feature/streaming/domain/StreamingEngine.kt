package com.sportcasterpro.app.feature.streaming.domain

import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.library.rtmp.RtmpCamera2
import com.pedro.library.view.OpenGlView
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException
import javax.inject.Inject

private const val DEFAULT_IFRAME_INTERVAL_SECONDS = 2
private const val AUDIO_SAMPLE_RATE_HZ = 44_100

/**
 * Thin wrapper around RootEncoder's [RtmpCamera2], the library that replaced FFmpegKit (retired
 * in 2025) for RTMP ingest on this project. RootEncoder owns the camera, encoder, and RTMP socket
 * together, so this engine needs a live [OpenGlView] before [startStream] or [startRecording] can
 * be called — wire it up via [attachPreview] once the Compose `AndroidView` is created.
 */
class StreamingEngine @Inject constructor(
    private val scoreboardOverlayRenderer: ScoreboardOverlayRenderer,
) : ConnectChecker {

    private var camera: RtmpCamera2? = null

    private val _stats = MutableStateFlow(StreamStats())
    val stats: StateFlow<StreamStats> = _stats.asStateFlow()

    fun attachPreview(view: OpenGlView) {
        val rtmpCamera = RtmpCamera2(view, this)
        camera = rtmpCamera
        scoreboardOverlayRenderer.attach(rtmpCamera.glInterface)
    }

    /**
     * Pushes the current scoreboard into the actual RTMP/recording GL pipeline (not just the
     * Compose preview) — see [ScoreboardOverlayRenderer].
     */
    fun updateScoreboardOverlay(state: ScoreboardState?) {
        scoreboardOverlayRenderer.render(state)
    }

    fun startStream(settings: StreamSettings) {
        val rtmpCamera = camera ?: return
        if (rtmpCamera.isStreaming) return

        val videoReady = rtmpCamera.prepareVideo(
            settings.videoWidth,
            settings.videoHeight,
            settings.fps,
            settings.videoBitrateBps,
            DEFAULT_IFRAME_INTERVAL_SECONDS,
            0,
        )
        val audioReady = rtmpCamera.prepareAudio(settings.audioBitrateBps, AUDIO_SAMPLE_RATE_HZ, true)

        if (videoReady && audioReady) {
            rtmpCamera.startStream(settings.fullRtmpEndpoint)
            _stats.update { it.copy(connectionStatus = ConnectionStatus.CONNECTING, errorMessage = null) }
        } else {
            _stats.update { it.copy(connectionStatus = ConnectionStatus.FAILED, errorMessage = "Camera or microphone unavailable") }
        }
    }

    fun stopStream() {
        camera?.takeIf { it.isStreaming }?.stopStream()
        _stats.update { it.copy(connectionStatus = ConnectionStatus.IDLE, bitrateBps = 0L) }
    }

    fun startRecording(outputFilePath: String) {
        val rtmpCamera = camera ?: return
        if (rtmpCamera.isRecording) return
        try {
            rtmpCamera.startRecord(outputFilePath)
            _stats.update { it.copy(isRecording = true) }
        } catch (e: IOException) {
            _stats.update { it.copy(errorMessage = "Could not start recording: ${e.message}") }
        }
    }

    fun stopRecording() {
        camera?.takeIf { it.isRecording }?.stopRecord()
        _stats.update { it.copy(isRecording = false) }
    }

    fun pauseRecording() {
        camera?.takeIf { it.isRecording }?.pauseRecord()
    }

    fun resumeRecording() {
        camera?.takeIf { it.isRecording }?.resumeRecord()
    }

    fun switchCamera() {
        try {
            camera?.switchCamera()
        } catch (e: CameraOpenException) {
            _stats.update { it.copy(errorMessage = "Could not switch camera: ${e.message}") }
        }
    }

    fun setZoom(level: Float) {
        camera?.setZoom(level)
    }

    fun release() {
        scoreboardOverlayRenderer.detach()
        scoreboardOverlayRenderer.release()
        camera?.apply {
            if (isStreaming) stopStream()
            if (isRecording) stopRecord()
            stopPreview()
        }
        camera = null
    }

    override fun onConnectionStarted(url: String) {
        _stats.update { it.copy(connectionStatus = ConnectionStatus.CONNECTING) }
    }

    override fun onConnectionSuccess() {
        _stats.update { it.copy(connectionStatus = ConnectionStatus.STREAMING, errorMessage = null) }
    }

    override fun onConnectionFailed(reason: String) {
        _stats.update { it.copy(connectionStatus = ConnectionStatus.FAILED, errorMessage = reason) }
    }

    override fun onNewBitrate(bitrate: Long) {
        _stats.update { it.copy(bitrateBps = bitrate) }
    }

    override fun onDisconnect() {
        _stats.update { it.copy(connectionStatus = ConnectionStatus.DISCONNECTED, bitrateBps = 0L) }
    }

    override fun onAuthError() {
        _stats.update { it.copy(connectionStatus = ConnectionStatus.FAILED, errorMessage = "Authentication failed") }
    }

    override fun onAuthSuccess() = Unit
}
