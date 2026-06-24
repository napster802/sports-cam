package com.sportcasterpro.app.feature.streaming.domain

data class StreamSettings(
    val rtmpUrl: String = "",
    val streamKey: String = "",
    val videoWidth: Int = 1280,
    val videoHeight: Int = 720,
    val fps: Int = 30,
    val videoBitrateBps: Int = 4_000_000,
    val audioBitrateBps: Int = 128_000,
) {
    val fullRtmpEndpoint: String
        get() = if (streamKey.isBlank()) rtmpUrl else "${rtmpUrl.trimEnd('/')}/$streamKey"

    companion object {
        val RESOLUTION_PRESETS = listOf(640 to 360, 1280 to 720, 1920 to 1080)
        val FPS_PRESETS = listOf(24, 30, 60)
        val BITRATE_PRESETS_BPS = listOf(1_500_000, 2_500_000, 4_000_000, 6_000_000)
    }
}
