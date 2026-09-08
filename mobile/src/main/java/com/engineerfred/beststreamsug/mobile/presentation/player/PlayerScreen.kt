package com.engineerfred.beststreamsug.mobile.presentation.player

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

private const val AUTO_HIDE_MS = 4_000L
private const val SEEK_BUTTON_MS = 10_000L

@OptIn(markerClass = [UnstableApi::class])
@Composable
fun PlayerRoute(
    url: String,
    title: String?,
    meta: String?,
    poster: String?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var playbackError by remember(url) { mutableStateOf<String?>(null) }
    var isControlsVisible by remember { mutableStateOf(true) }

    val player = remember(url) {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(
                    DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true),
                ),
            )
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = true
            }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isBuffering by remember { mutableStateOf(false) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
            }
            override fun onPlayerError(error: PlaybackException) {
                playbackError = "Playback failed. Please try again."
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(isPlaying, isControlsVisible) {
        if (isPlaying) {
            while (true) {
                positionMs = player.currentPosition
                durationMs = player.duration
                delay(500L)
            }
        }
    }

    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(AUTO_HIDE_MS)
            isControlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    isControlsVisible = !isControlsVisible
                }
            },
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    keepScreenOn = true
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp),
                color = Color.White,
            )
        }

        AnimatedVisibility(
            visible = isControlsVisible,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            PlayerControlsOverlay(
                isPlaying = isPlaying,
                positionMs = positionMs,
                durationMs = durationMs,
                title = title,
                meta = meta,
                onBack = onBack,
                onPlayPause = {
                    if (player.isPlaying) player.pause() else player.play()
                },
                onSeekBy = { deltaMs ->
                    player.seekTo((player.currentPosition + deltaMs).coerceIn(0L, player.duration))
                },
            )
        }

        playbackError?.let { message ->
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
                PlayerErrorButton(
                    label = "Try Again",
                    onClick = {
                        player.seekTo(0L)
                        player.playWhenReady = true
                        playbackError = null
                    },
                )
                PlayerErrorButton(
                    label = "Back",
                    onClick = onBack,
                )
            }
        }
    }
}

@Composable
private fun PlayerControlsOverlay(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    title: String?,
    meta: String?,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(160.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f),
                        ),
                    ),
                ),
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onBack,
                    )
                    .padding(12.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            ) {
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
                meta?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                    )
                }
            }
        }

        // Center controls
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            PlayerIconButton(
                icon = Icons.Rounded.Replay10,
                onClick = { onSeekBy(-SEEK_BUTTON_MS) },
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .size(72.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPlayPause,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier
                        .size(if (isPlaying) 34.dp else 40.dp)
                        .padding(start = if (isPlaying) 0.dp else 4.dp),
                )
            }
            PlayerIconButton(
                icon = Icons.Rounded.Forward10,
                onClick = { onSeekBy(SEEK_BUTTON_MS) },
            )
        }

        // Bottom progress
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            LinearProgressIndicator(
                progress = {
                    if (durationMs <= 0) 0f else (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
                },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.3f),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatPlayerTime(positionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontSize = 11.sp,
                )
                Text(
                    text = formatPlayerTime(durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun PlayerIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier
            .size(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(8.dp),
    )
}

@Composable
private fun PlayerErrorButton(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.Black,
        )
    }
}

private fun formatPlayerTime(millis: Long): String {
    if (millis <= 0L) return "0:00"
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(
            java.util.Locale.getDefault(),
            "%d:%02d:%02d",
            hours,
            minutes,
            seconds,
        )
    } else {
        String.format(java.util.Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}