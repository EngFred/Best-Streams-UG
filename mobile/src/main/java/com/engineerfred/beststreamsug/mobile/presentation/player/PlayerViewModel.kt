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
import com.engineerfred.beststreamsug.domain.usecase.ClearPlaybackProgressUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetPlaybackProgressUseCase
import com.engineerfred.beststreamsug.domain.usecase.SavePlaybackProgressUseCase
import com.engineerfred.beststreamsug.mobile.core.cast.CastManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    val castManager: CastManager,
    savedStateHandle: SavedStateHandle,
    private val getPlaybackProgressUseCase: GetPlaybackProgressUseCase,
    private val savePlaybackProgressUseCase: SavePlaybackProgressUseCase,
    private val clearPlaybackProgressUseCase: ClearPlaybackProgressUseCase,
) : AndroidViewModel(application) {

    private val initialStreamUrl: String = savedStateHandle.get<String>("url") ?: ""
    val movieTitle: String = savedStateHandle.get<String>("title").takeUnless { it.isNullOrBlank() } ?: "Now Playing"
    private val initialPosterUrl: String = savedStateHandle.get<String>("poster") ?: ""
    private val initialMeta: String = savedStateHandle.get<String>("meta") ?: ""
    private val mediaKey: String = initialStreamUrl.ifBlank { movieTitle }

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
                    if (!isPlaying) {
                        saveCurrentProgress()
                    }
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
                    if (playbackState == Player.STATE_ENDED) {
                        viewModelScope.launch {
                            clearPlaybackProgressUseCase(mediaKey)
                        }
                    }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                if (!castManager.isCasting.value) {
                    _state.update { it.copy(error = "Playback failed. Please try again.") }
                }
            }
        })

        // Initial setup with Resume logic based on current Cast & Progress state
        viewModelScope.launch {
            val savedProgress = getPlaybackProgressUseCase(mediaKey)
            val startPositionMs = if (savedProgress != null && savedProgress.positionMs >= 5_000L) {
                savedProgress.positionMs
            } else {
                0L
            }

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
                // Connected to Cast, but user clicked a NEW video: start casting it with saved start position
                _state.update {
                    it.copy(
                        isCasting = true,
                        castDeviceName = castManager.connectedDeviceName.value,
                        isBuffering = true,
                        isPlaying = true,
                        resumeTimestamp = if (startPositionMs > 0L) startPositionMs else null
                    )
                }
                castManager.loadMedia(
                    streamUrl = initialStreamUrl,
                    title = movieTitle,
                    posterUrl = initialPosterUrl,
                    overview = initialMeta,
                    startPositionMs = startPositionMs,
                    autoPlay = true
                )
            } else {
                // Normal phone playback: load stream locally and resume if position saved
                if (startPositionMs > 0L) {
                    _state.update { it.copy(resumeTimestamp = startPositionMs) }
                }
                loadStreamLocally(initialStreamUrl, startPositionMs)
            }
            initialSessionChecked = true
        }

        // Periodic progress saving loop (every 2.5s)
        viewModelScope.launch {
            while (isActive) {
                delay(2500L)
                saveCurrentProgress()
            }
        }

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
                    loadStreamLocally(_state.value.currentStreamUrl, castPos.coerceAtLeast(0L))
                }
            }
        }.launchIn(viewModelScope)

        // Observe Cast playback state when casting
        castManager.isCastPlaying.onEach { isPlaying ->
            if (castManager.isCasting.value) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (!isPlaying) {
                    saveCurrentProgress()
                }
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

    private fun loadStreamLocally(url: String, startPositionMs: Long = 0L) {
        if (url.isBlank()) return
        try {
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            if (startPositionMs > 0L) {
                player.seekTo(startPositionMs)
            }
            player.prepare()
            player.playWhenReady = true
        } catch (e: Exception) {
            _state.update { it.copy(error = "Failed to load stream: ${e.message}") }
        }
    }

    fun retryPlayback() {
        _state.update { it.copy(error = null, isBuffering = true) }
        val currentPos = if (castManager.isCasting.value) {
            castManager.getCastPosition()
        } else {
            player.currentPosition
        }.coerceAtLeast(0L)

        if (castManager.isCasting.value) {
            castManager.loadMedia(
                streamUrl = _state.value.currentStreamUrl,
                title = _state.value.currentTitle,
                posterUrl = initialPosterUrl,
                overview = initialMeta,
                startPositionMs = currentPos,
                autoPlay = true
            )
        } else {
            loadStreamLocally(_state.value.currentStreamUrl, currentPos)
        }
    }

    fun togglePlayPause() {
        if (castManager.isCasting.value) {
            if (castManager.isCastPlaying.value) {
                castManager.pause()
                saveCurrentProgress()
            } else {
                castManager.play()
            }
            return
        }

        if (player.isPlaying) {
            player.pause()
            saveCurrentProgress()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        if (castManager.isCasting.value) {
            castManager.seekTo(positionMs)
            val dur = castManager.getCastDuration()
            saveProgressInternal(positionMs, dur)
        } else {
            player.seekTo(positionMs)
            val dur = player.duration
            saveProgressInternal(positionMs, dur)
        }
    }

    fun seekForward() {
        if (castManager.isCasting.value) {
            val current = castManager.getCastPosition()
            val duration = castManager.getCastDuration()
            val target = (current + 10_000L).coerceAtMost(duration.coerceAtLeast(0L))
            castManager.seekTo(target)
            saveProgressInternal(target, duration)
        } else {
            val duration = player.duration.coerceAtLeast(0L)
            val target = if (duration > 0L) {
                (player.currentPosition + 10_000L).coerceAtMost(duration)
            } else {
                player.currentPosition + 10_000L
            }
            player.seekTo(target)
            saveProgressInternal(target, duration)
        }
    }

    fun seekBackward() {
        if (castManager.isCasting.value) {
            val current = castManager.getCastPosition()
            val duration = castManager.getCastDuration()
            val target = (current - 10_000L).coerceAtLeast(0L)
            castManager.seekTo(target)
            saveProgressInternal(target, duration)
        } else {
            val duration = player.duration.coerceAtLeast(0L)
            val target = (player.currentPosition - 10_000L).coerceAtLeast(0L)
            player.seekTo(target)
            saveProgressInternal(target, duration)
        }
    }

    fun saveCurrentProgress() {
        if (castManager.isCasting.value) {
            val pos = castManager.getCastPosition()
            val dur = castManager.getCastDuration()
            saveProgressInternal(pos, dur)
        } else {
            val pos = player.currentPosition
            val dur = player.duration
            if (player.playbackState != Player.STATE_IDLE) {
                saveProgressInternal(pos, dur)
            }
        }
    }

    private fun saveProgressInternal(positionMs: Long, durationMs: Long) {
        if (positionMs <= 0L) return
        viewModelScope.launch {
            if (durationMs > 0L && (positionMs >= (durationMs - 30_000L) || (positionMs.toFloat() / durationMs.toFloat()) >= 0.95f)) {
                clearPlaybackProgressUseCase(mediaKey)
            } else if (positionMs >= 5_000L) {
                savePlaybackProgressUseCase(mediaKey, positionMs, durationMs)
            }
        }
    }

    fun onResumeBadgeShown() {
        _state.update { it.copy(resumeTimestamp = null) }
    }

    override fun onCleared() {
        saveCurrentProgress()
        super.onCleared()
        player.release()
    }
}