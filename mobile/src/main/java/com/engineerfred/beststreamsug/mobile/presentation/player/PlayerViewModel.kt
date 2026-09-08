package com.engineerfred.beststreamsug.mobile.presentation.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.engineerfred.beststreamsug.mobile.core.cast.CastManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    val castManager: CastManager,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val initialStreamUrl: String = savedStateHandle.get<String>("url") ?: ""
    val movieTitle: String = savedStateHandle.get<String>("title").takeUnless { it.isNullOrBlank() } ?: "Now Playing"
    private val initialPosterUrl: String = savedStateHandle.get<String>("poster") ?: ""
    private val initialMeta: String = savedStateHandle.get<String>("meta") ?: ""

    private val _state = MutableStateFlow(
        PlayerState(
            currentTitle = movieTitle,
            currentStreamUrl = initialStreamUrl,
            isCasting = castManager.isCasting.value,
            castDeviceName = castManager.connectedDeviceName.value
        )
    )
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    val player: ExoPlayer = ExoPlayer.Builder(application)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(
                DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true),
            ),
        )
        .build()

    private var initialSessionChecked = false

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!castManager.isCasting.value) {
                    _state.update { it.copy(isPlaying = isPlaying) }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (!castManager.isCasting.value) {
                    val isBuffering = playbackState == Player.STATE_BUFFERING
                    val dur = if (player.duration > 0) player.duration else 0L
                    _state.update {
                        it.copy(
                            isBuffering = isBuffering,
                            duration = dur
                        )
                    }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                if (!castManager.isCasting.value) {
                    _state.update { it.copy(error = "Playback failed. Please try again.") }
                }
            }
        })

        // Initial setup based on current Cast state
        val currentlyCastingThis = castManager.isCurrentlyCasting(initialStreamUrl)
        if (currentlyCastingThis) {
            // Re-entering an actively casting video: preserve state without reloading
            _state.update {
                it.copy(
                    isCasting = true,
                    castDeviceName = castManager.connectedDeviceName.value,
                    isPlaying = castManager.isCastPlaying.value,
                    isBuffering = castManager.isCastBuffering.value,
                    currentPosition = castManager.getCastPosition(),
                    duration = castManager.getCastDuration()
                )
            }
        } else if (castManager.isCasting.value && initialStreamUrl.isNotBlank()) {
            // Connected to Cast, but user clicked a NEW video: start casting it with poster & meta
            _state.update {
                it.copy(
                    isCasting = true,
                    castDeviceName = castManager.connectedDeviceName.value,
                    isBuffering = true,
                    isPlaying = true
                )
            }
            castManager.loadMedia(
                streamUrl = initialStreamUrl,
                title = movieTitle,
                posterUrl = initialPosterUrl,
                overview = initialMeta,
                startPositionMs = 0L,
                autoPlay = true
            )
        } else {
            // Normal phone playback
            loadStreamLocally(initialStreamUrl)
        }
        initialSessionChecked = true

        // Observe Cast connection transitions for Phone <-> TV handoff
        combine(
            castManager.isCasting,
            castManager.connectedDeviceName
        ) { isCasting, deviceName ->
            Pair(isCasting, deviceName)
        }.onEach { (isCasting, deviceName) ->
            val wasCasting = _state.value.isCasting
            _state.update {
                it.copy(
                    isCasting = isCasting,
                    castDeviceName = deviceName
                )
            }

            if (initialSessionChecked) {
                if (isCasting && !wasCasting) {
                    // Transitioned from Phone -> TV: Hand off playback
                    val currentPos = player.currentPosition.coerceAtLeast(0L)
                    player.stop()

                    castManager.loadMedia(
                        streamUrl = _state.value.currentStreamUrl,
                        title = _state.value.currentTitle,
                        posterUrl = initialPosterUrl,
                        overview = initialMeta,
                        startPositionMs = currentPos,
                        autoPlay = true
                    )
                } else if (!isCasting && wasCasting) {
                    // Transitioned from TV -> Phone: Resume locally
                    val castPos = castManager.getCastPosition()
                    loadStreamLocally(_state.value.currentStreamUrl)
                    if (castPos > 0) {
                        player.seekTo(castPos)
                    }
                }
            }
        }.launchIn(viewModelScope)

        // Observe Cast playback state when casting
        castManager.isCastPlaying.onEach { isPlaying ->
            if (castManager.isCasting.value) {
                _state.update { it.copy(isPlaying = isPlaying) }
            }
        }.launchIn(viewModelScope)

        castManager.isCastBuffering.onEach { isBuffering ->
            if (castManager.isCasting.value) {
                _state.update { it.copy(isBuffering = isBuffering) }
            }
        }.launchIn(viewModelScope)

        castManager.castError.onEach { error ->
            if (castManager.isCasting.value) {
                _state.update { it.copy(error = error) }
            }
        }.launchIn(viewModelScope)
    }

    private fun loadStreamLocally(url: String) {
        if (url.isBlank()) return
        try {
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
        } catch (e: Exception) {
            _state.update { it.copy(error = "Failed to load stream: ${e.message}") }
        }
    }

    fun retryPlayback() {
        _state.update { it.copy(error = null, isBuffering = true) }
        if (castManager.isCasting.value) {
            castManager.loadMedia(
                streamUrl = _state.value.currentStreamUrl,
                title = _state.value.currentTitle,
                posterUrl = initialPosterUrl,
                overview = initialMeta,
                startPositionMs = 0L,
                autoPlay = true
            )
        } else {
            loadStreamLocally(_state.value.currentStreamUrl)
        }
    }

    fun togglePlayPause() {
        if (castManager.isCasting.value) {
            if (castManager.isCastPlaying.value) {
                castManager.pause()
            } else {
                castManager.play()
            }
            return
        }

        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        if (castManager.isCasting.value) {
            castManager.seekTo(positionMs)
        } else {
            player.seekTo(positionMs)
        }
    }

    fun seekForward() {
        if (castManager.isCasting.value) {
            val current = castManager.getCastPosition()
            val duration = castManager.getCastDuration()
            val target = (current + 10_000L).coerceAtMost(duration.coerceAtLeast(0L))
            castManager.seekTo(target)
        } else {
            val duration = player.duration.coerceAtLeast(0L)
            val target = if (duration > 0L) {
                (player.currentPosition + 10_000L).coerceAtMost(duration)
            } else {
                player.currentPosition + 10_000L
            }
            player.seekTo(target)
        }
    }

    fun seekBackward() {
        if (castManager.isCasting.value) {
            val current = castManager.getCastPosition()
            val target = (current - 10_000L).coerceAtLeast(0L)
            castManager.seekTo(target)
        } else {
            val target = (player.currentPosition - 10_000L).coerceAtLeast(0L)
            player.seekTo(target)
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}