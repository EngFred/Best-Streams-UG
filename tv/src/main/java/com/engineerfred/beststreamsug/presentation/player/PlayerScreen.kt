package com.engineerfred.beststreamsug.presentation.player

import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.OptIn
import com.engineerfred.beststreamsug.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

private const val AUTO_HIDE_MS = 4_000L
private const val SEEK_BUTTON_MS = 10_000L
private const val SEEK_BAR_STEP = 0.03f
private const val RANGE_NOT_SATISFIABLE = 416

private enum class AspectMode(val label: String) {
    Fit("Fit"),
    Fill("Fill"),
    Stretch("Stretch"),
}

private fun AspectMode.toResizeMode(): Int = when (this) {
    AspectMode.Fit -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    AspectMode.Fill -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    AspectMode.Stretch -> AspectRatioFrameLayout.RESIZE_MODE_FILL
}

@OptIn(markerClass = [UnstableApi::class])
@Composable
fun PlayerRoute(
    url: String,
    title: String?,
    meta: String?,
    poster: String?,
    onBack: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var useFullFileFallback by remember(url) { mutableStateOf(false) }
    var retryToken by remember(url) { mutableStateOf(0) }
    var playbackError by remember(url) { mutableStateOf<String?>(null) }
    var aspectMode by remember { mutableStateOf(AspectMode.Fit) }
    val player = remember(url, useFullFileFallback, retryToken) {
        ExoPlayer.Builder(
            context,
            DefaultMediaSourceFactory(
                if (useFullFileFallback) fullFileDataSourceFactory() else DefaultHttpDataSource.Factory(),
            ),
        ).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var isReady by remember { mutableStateOf(false) }
    var isEnded by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var controlsVisible by remember { mutableStateOf(true) }
    var justRevealedControls by remember { mutableStateOf(false) }
    var activityTick by remember { mutableIntStateOf(0) }

    val playButtonFocus = remember { FocusRequester() }
    val videoFocus = remember { FocusRequester() }

    DisposableEffect(player) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(value: Boolean) {
                isPlaying = value
            }

            override fun onPlaybackStateChanged(state: Int) {
                isEnded = state == androidx.media3.common.Player.STATE_ENDED
                isBuffering = state == androidx.media3.common.Player.STATE_BUFFERING
                isReady = state == androidx.media3.common.Player.STATE_READY
            }

            override fun onPlayerError(error: PlaybackException) {
                if (!useFullFileFallback && error.hasResponseCode(RANGE_NOT_SATISFIABLE)) {
                    playbackError = null
                    useFullFileFallback = true
                } else {
                    playbackError = "Unable to play this video"
                    Log.e("BestStreamsPlayer", "Playback failed for $url", error)
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.clearVideoSurface()
            player.release()
        }
    }

    LaunchedEffect(player) {
        while (true) {
            position = player.currentPosition.coerceAtLeast(0L)
            duration = player.duration.takeIf { it > 0L } ?: 0L
            bufferedPosition = player.bufferedPosition.coerceAtLeast(0L)
            delay(250)
        }
    }

    LaunchedEffect(controlsVisible, isPlaying, isBuffering, isEnded, activityTick) {
        if (controlsVisible && isPlaying && !isBuffering && !isEnded) {
            delay(AUTO_HIDE_MS)
            controlsVisible = false
        }
    }

    LaunchedEffect(controlsVisible, isPlaying, isBuffering, isEnded) {
        if (controlsVisible) {
            playButtonFocus.requestFocus()
        } else {
            videoFocus.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { event ->
                if (!controlsVisible) {
                    if (event.key != Key.Back) {
                        if (event.type == KeyEventType.KeyDown) {
                            activityTick += 1
                            controlsVisible = true
                            justRevealedControls = true
                        }
                        return@onPreviewKeyEvent true
                    }
                    return@onPreviewKeyEvent false
                }

                if (justRevealedControls) {
                    if (event.type == KeyEventType.KeyUp) {
                        justRevealedControls = false
                    }
                    return@onPreviewKeyEvent true
                }

                if (event.type == KeyEventType.KeyDown) {
                    activityTick += 1
                    if (event.key == Key.Back) {
                        controlsVisible = false
                        return@onPreviewKeyEvent true
                    }
                }
                false
            },
    ) {
        AndroidView(
            factory = { viewContext ->
                val playerView = LayoutInflater.from(viewContext)
                    .inflate(R.layout.player_view, null, false) as PlayerView
                playerView.keepScreenOn = true
                playerView.isFocusable = false
                playerView.player = player
                playerView.setResizeMode(aspectMode.toResizeMode())
                playerView
            },
            update = { view ->
                view.setResizeMode(aspectMode.toResizeMode())
            },
            modifier = Modifier
                .fillMaxSize()
                .focusable()
                .focusRequester(videoFocus),
        )

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.72f),
                            0.24f to Color.Black.copy(alpha = 0f),
                            0.60f to Color.Black.copy(alpha = 0f),
                            1f to Color.Black.copy(alpha = 0.80f),
                        ),
                    ),
            ) {
                PlayerControls(
                    isPlaying = isPlaying,
                    position = position,
                    duration = duration,
                    bufferedPosition = bufferedPosition,
                    posterUrl = poster,
                    title = title,
                    meta = meta,
                    aspectLabel = aspectMode.label,
                    playButtonFocus = playButtonFocus,
                    onBack = onBack,
                    onCycleAspect = {
                        aspectMode = AspectMode.entries[(aspectMode.ordinal + 1) % AspectMode.entries.size]
                    },
                    onTogglePlayback = {
                        if (player.isPlaying) player.pause() else player.play()
                    },
                    onSeekBack = {
                        player.seekTo((player.currentPosition - SEEK_BUTTON_MS).coerceAtLeast(0L))
                    },
                    onSeekForward = {
                        val target = player.currentPosition + SEEK_BUTTON_MS
                        player.seekTo(if (duration > 0) target.coerceAtMost(duration) else target)
                    },
                    onSeekByFraction = { deltaFraction ->
                        if (duration > 0) {
                            val target = player.currentPosition + (deltaFraction * duration).toLong()
                            player.seekTo(target.coerceIn(0L, duration))
                        }
                    },
                )
            }
        }

        if ((isBuffering || !isReady) && !isEnded && playbackError == null) {
            BufferingOverlay()
        }

        if (isEnded && playbackError == null) {
            EndedOverlay(
                onReplay = {
                    isEnded = false
                    player.seekTo(0)
                    player.play()
                },
                onBack = onBack,
            )
        }

        if (playbackError != null) {
            PlaybackErrorOverlay(
                message = playbackError.orEmpty(),
                onRetry = {
                    playbackError = null
                    retryToken += 1
                },
                onBack = onBack,
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    bufferedPosition: Long,
    posterUrl: String?,
    title: String?,
    meta: String?,
    aspectLabel: String,
    playButtonFocus: FocusRequester?,
    onBack: () -> Unit,
    onCycleAspect: () -> Unit,
    onTogglePlayback: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekByFraction: (Float) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 48.dp, end = 48.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Top Row: Poster Artwork Thumbnail + Title + Metadata + Quality Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                if (posterUrl?.isNotBlank() == true) {
                    NowPlayingPoster(
                        posterUrl = posterUrl,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = title ?: "Now Playing",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp),
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(4.dp),
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "1080p HD",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White.copy(alpha = 0.9f),
                            )
                        }
                    }

                    if (meta?.isNotBlank() == true) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Timeline Scrubber Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = formatTime(position),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.85f),
                )
                SeekBar(
                    modifier = Modifier.weight(1f),
                    fraction = if (duration > 0) {
                        position.toFloat() / duration.toFloat()
                    } else {
                        0f
                    },
                    bufferedFraction = if (duration > 0) {
                        (bufferedPosition.toFloat() / duration.toFloat())
                    } else {
                        0f
                    },
                    onSeekByFraction = onSeekByFraction,
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.85f),
                )
            }

            // Bottom Playback Actions Cluster
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                    AspectPill(
                        label = aspectLabel,
                        onClick = onCycleAspect,
                    )
                }
                Box(modifier = Modifier.align(Alignment.Center)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                    ) {
                        ControlButton(
                            onClick = onSeekBack,
                            size = 46.dp,
                            containerColor = Color.White.copy(alpha = 0.12f),
                            contentColor = Color.White,
                            content = { Text("−10s", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)) },
                        )
                        ControlButton(
                            onClick = onTogglePlayback,
                            size = 58.dp,
                            containerColor = Color.White.copy(alpha = 0.12f),
                            contentColor = Color.White,
                            focusRequester = playButtonFocus,
                            content = {
                                if (isPlaying) {
                                    PauseGlyph(color = Color.White)
                                } else {
                                    PlayGlyph(color = Color.White)
                                }
                            },
                        )
                        ControlButton(
                            onClick = onSeekForward,
                            size = 46.dp,
                            containerColor = Color.White.copy(alpha = 0.12f),
                            contentColor = Color.White,
                            content = { Text("+10s", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NowPlayingPoster(
    posterUrl: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(74.dp)
            .height(108.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(8.dp),
            ),
    ) {
        AsyncImage(
            model = posterUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AspectPill(
    label: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = CardDefaults.shape(
            shape = RoundedCornerShape(6.dp),
            focusedShape = RoundedCornerShape(6.dp),
        ),
        border = CardDefaults.border(
            border = Border.None,
            focusedBorder = Border(
                border = BorderStroke(2.dp, Color.White),
                shape = RoundedCornerShape(6.dp),
            ),
        ),
        scale = CardDefaults.scale(
            scale = 1f,
            focusedScale = 1.08f,
        ),
        colors = CardDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.10f),
            focusedContainerColor = Color.White.copy(alpha = 0.25f),
            contentColor = Color.White,
            focusedContentColor = Color.White,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AspectGlyph()
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun AspectGlyph() {
    Canvas(modifier = Modifier.size(width = 18.dp, height = 18.dp)) {
        val inset = 3.dp.toPx()
        val stroke = Stroke(width = 2.dp.toPx())
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = stroke,
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ControlButton(
    onClick: () -> Unit,
    size: Dp,
    content: @Composable () -> Unit,
    containerColor: Color = Color.White.copy(alpha = 0.10f),
    contentColor: Color = Color.White,
    focusRequester: FocusRequester? = null,
) {
    Card(
        onClick = onClick,
        shape = CardDefaults.shape(
            shape = CircleShape,
            focusedShape = CircleShape,
            pressedShape = CircleShape,
        ),
        border = CardDefaults.border(
            border = Border.None,
            focusedBorder = Border(
                border = BorderStroke(2.5.dp, Color.White),
                shape = CircleShape,
            ),
        ),
        scale = CardDefaults.scale(
            scale = 1f,
            focusedScale = 1.10f,
        ),
        colors = CardDefaults.colors(
            containerColor = containerColor,
            focusedContainerColor = Color.White.copy(alpha = 0.25f),
            contentColor = contentColor,
            focusedContentColor = contentColor,
        ),
        modifier = Modifier
            .size(size)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun PlayGlyph(color: Color = Color.White) {
    Canvas(modifier = Modifier.size(width = 24.dp, height = 27.dp)) {
        val w = size.width
        val h = size.height
        val triangle = Path().apply {
            moveTo(w * 0.30f, 0f)
            lineTo(w * 0.30f, h)
            lineTo(w, h / 2f)
            close()
        }
        drawPath(triangle, color = color)
    }
}

@Composable
private fun PauseGlyph(color: Color = Color.White) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(24.dp)
                .background(color, RoundedCornerShape(2.dp)),
        )
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(24.dp)
                .background(color, RoundedCornerShape(2.dp)),
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeekBar(
    fraction: Float,
    bufferedFraction: Float,
    onSeekByFraction: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val progress = fraction.coerceIn(0f, 1f)
    val buffered = bufferedFraction.coerceIn(0f, 1f)

    Row(
        modifier = modifier
            .height(44.dp)
            .focusable(interactionSource = interactionSource)
            .onKeyEvent { event ->
                when {
                    event.type == KeyEventType.KeyDown &&
                        event.key == Key.DirectionLeft -> {
                        onSeekByFraction(-SEEK_BAR_STEP)
                        true
                    }

                    event.type == KeyEventType.KeyDown &&
                        event.key == Key.DirectionRight -> {
                        onSeekByFraction(SEEK_BAR_STEP)
                        true
                    }

                    else -> false
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
        ) {
            val trackWidth = maxWidth
            val trackHeight = if (isFocused) 7.dp else 5.dp
            val thumbSize = if (isFocused) 18.dp else 14.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .background(Color.White.copy(alpha = 0.20f), RoundedCornerShape(4.dp)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(buffered)
                    .height(trackHeight)
                    .background(Color.White.copy(alpha = 0.38f), RoundedCornerShape(4.dp)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(trackHeight)
                    .background(
                        if (isFocused) MaterialTheme.colorScheme.primary else Color.White,
                        RoundedCornerShape(4.dp),
                    ),
            )
            Box(
                modifier = Modifier
                    .offset(x = (trackWidth - thumbSize) * progress)
                    .size(thumbSize)
                    .background(
                        if (isFocused) MaterialTheme.colorScheme.primary else Color.White,
                        CircleShape,
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape),
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun BufferingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = Color.White,
                strokeWidth = 4.dp,
                trackColor = Color.White.copy(alpha = 0.15f),
            )
            Text(
                text = "Preparing stream…",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f),
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun EndedOverlay(
    onReplay: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "Playback ended",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Card(
                onClick = onReplay,
                shape = CardDefaults.shape(
                    shape = RoundedCornerShape(8.dp),
                    focusedShape = RoundedCornerShape(8.dp),
                ),
                scale = CardDefaults.scale(
                    scale = 1f,
                    focusedScale = 1.08f,
                ),
                colors = CardDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = "↻  Play Again",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlaybackErrorOverlay(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Text(
                text = "Check your connection and try again",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                onClick = onRetry,
                shape = CardDefaults.shape(
                    shape = RoundedCornerShape(8.dp),
                    focusedShape = RoundedCornerShape(8.dp),
                ),
                scale = CardDefaults.scale(
                    scale = 1f,
                    focusedScale = 1.08f,
                ),
                colors = CardDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = "↻  Retry",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@OptIn(markerClass = [UnstableApi::class])
private fun fullFileDataSourceFactory(): DataSource.Factory =
    ResolvingDataSource.Factory(DefaultHttpDataSource.Factory()) { dataSpec ->
        if (dataSpec.position == 0L) {
            dataSpec.buildUpon()
                .setPosition(0L)
                .setLength(C.LENGTH_UNSET.toLong())
                .build()
        } else {
            dataSpec
        }
    }

private fun PlaybackException.hasResponseCode(responseCode: Int): Boolean {
    var cause: Throwable? = this
    while (cause != null) {
        if (cause is HttpDataSource.InvalidResponseCodeException &&
            cause.responseCode == responseCode
        ) {
            return true
        }
        cause = cause.cause
    }
    return false
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1_000).coerceAtLeast(0L)
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}