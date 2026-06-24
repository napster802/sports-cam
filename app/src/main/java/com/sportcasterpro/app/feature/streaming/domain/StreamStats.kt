package com.sportcasterpro.app.feature.streaming.domain

enum class ConnectionStatus { IDLE, CONNECTING, STREAMING, FAILED, DISCONNECTED }

data class StreamStats(
    val connectionStatus: ConnectionStatus = ConnectionStatus.IDLE,
    val bitrateBps: Long = 0L,
    val isRecording: Boolean = false,
    val elapsedMillis: Long = 0L,
    val errorMessage: String? = null,
) {
    val isHealthy: Boolean get() = connectionStatus == ConnectionStatus.STREAMING && bitrateBps > 0
}
