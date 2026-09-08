package com.engineerfred.beststreamsug.mobile.presentation.player

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val currentTitle: String = "",
    val currentStreamUrl: String = "",
    val error: String? = null,
    // Cast state
    val isCasting: Boolean = false,
    val castDeviceName: String? = null,
    // Resume position feedback
    val resumeTimestamp: Long? = null
)