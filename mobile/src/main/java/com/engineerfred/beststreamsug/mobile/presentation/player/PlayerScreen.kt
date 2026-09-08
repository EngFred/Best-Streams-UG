package com.engineerfred.beststreamsug.mobile.presentation.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.engineerfred.beststreamsug.mobile.ui.components.CastButton
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicBackground
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicPrimary
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

private const val AUTO_HIDE_MS = 4_000L
private const val SEEK_BUTTON_MS = 10_000L

enum class VideoScaleMode(
    val label: String,
    val resizeMode: Int,
    val icon: ImageVector,
) {
    Fit("Fit (Original)", AspectRatioFrameLayout.RESIZE_MODE_FIT, Icons.Rounded.FitScreen),
    Fill("Fill (Crop)", AspectRatioFrameLayout.RESIZE_MODE_ZOOM, Icons.Rounded.CropFree),
    Stretch("Stretch", AspectRatioFrameLayout.RESIZE_MODE_FILL, Icons.Rounded.AspectRatio),
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
    val viewModel: PlayerViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isControlsVisible by remember { mutableStateOf(true) }
    var scaleMode by rememberSaveable { mutableStateOf(VideoScaleMode.Fit) }
    var scaleBadgeText by remember { mutableStateOf<String?>(null) }

    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }

    var positionMs by remember { mutableLongStateOf(0L) }
    var castDurationMs by remember { mutableLongStateOf(0L) }

    val insetsController = remember(activity) {
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView)
        }
    }

    // ── System Bars visibility: hide in landscape, show in portrait ──────────
    DisposableEffect(isLandscape, insetsController) {
        insetsController?.let { controller ->
            if (isLandscape) {
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // ── Handle Landscape Back (System Back Gesture) ──────────────────────────
    BackHandler(enabled = isLandscape) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // ── Restore Portrait upon leaving Player ─────────────────────────────────
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // ── Screen Timeout (FLAG_KEEP_SCREEN_ON) ─────────────────────────────────
    // Keep screen awake ONLY when actively playing locally (not casting)
    val shouldKeepScreenOn = !state.isCasting && state.isPlaying
    DisposableEffect(shouldKeepScreenOn) {
        if (shouldKeepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // ── Track position ───────────────────────────────────────────────────────
    LaunchedEffect(state.isCasting, state.isPlaying) {
        while (true) {
            if (state.isCasting) {
                positionMs = viewModel.castManager.getCastPosition().coerceAtLeast(0L)
                castDurationMs = viewModel.castManager.getCastDuration().coerceAtLeast(0L)
            } else if (state.isPlaying) {
                positionMs = viewModel.player.currentPosition.coerceAtLeast(0L)
            }
            delay(500L)
        }
    }

    val effectiveDuration =
        if (state.isCasting && castDurationMs > 0) castDurationMs else state.duration

    // ── Auto hide controls ───────────────────────────────────────────────────
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(AUTO_HIDE_MS)
            isControlsVisible = false
        }
    }

    // ── Dismiss scale badge after 1.5s ───────────────────────────────────────
    LaunchedEffect(scaleBadgeText) {
        if (scaleBadgeText != null) {
            delay(1500L)
            scaleBadgeText = null
        }
    }

    val handleBack = {
        if (isLandscape) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            onBack()
        }
    }

    val handleToggleOrientation = {
        if (isLandscape) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    val handleCycleScaleMode = {
        val nextMode = when (scaleMode) {
            VideoScaleMode.Fit -> VideoScaleMode.Fill
            VideoScaleMode.Fill -> VideoScaleMode.Stretch
            VideoScaleMode.Stretch -> VideoScaleMode.Fit
        }
        scaleMode = nextMode
        playerViewRef?.resizeMode = nextMode.resizeMode
        scaleBadgeText = nextMode.label
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
        if (state.isCasting) {
            CastingOverlay(
                deviceName = state.castDeviceName,
                title = state.currentTitle.ifBlank { title.orEmpty() },
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = viewModel.player
                        useController = false
                        resizeMode = scaleMode.resizeMode
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        playerViewRef = this
                    }
                },
                update = { pv ->
                    pv.resizeMode = scaleMode.resizeMode
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Concentric buffering indicator when controls are hidden
        if (state.isBuffering && !isControlsVisible) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp),
                color = Color.White,
                strokeWidth = 3.5.dp,
            )
        }

        // Scale mode toast badge
        AnimatedVisibility(
            visible = scaleBadgeText != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            scaleBadgeText?.let { label ->
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isControlsVisible,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            PlayerControlsOverlay(
                isPlaying = state.isPlaying,
                isBuffering = state.isBuffering,
                isLandscape = isLandscape,
                scaleMode = scaleMode,
                positionMs = positionMs,
                durationMs = effectiveDuration,
                title = title,
                meta = meta,
                onBack = handleBack,
                onToggleOrientation = handleToggleOrientation,
                onCycleScaleMode = handleCycleScaleMode,
                onPlayPause = viewModel::togglePlayPause,
                onSeekBy = { deltaMs ->
                    if (deltaMs < 0) viewModel.seekBackward() else viewModel.seekForward()
                },
            )
        }

        state.error?.let { message ->
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
                    onClick = viewModel::retryPlayback,
                )
                PlayerErrorButton(
                    label = "Back",
                    onClick = handleBack,
                )
            }
        }
    }
}

@Composable
private fun CastingOverlay(
    deviceName: String?,
    title: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinematicBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.CastConnected,
                contentDescription = "Casting",
                tint = CinematicPrimary,
                modifier = Modifier.size(56.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Casting to ${deviceName ?: "Android TV"}",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(Modifier.height(4.dp))
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CinematicMutedText,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PlayerControlsOverlay(
    isPlaying: Boolean,
    isBuffering: Boolean,
    isLandscape: Boolean,
    scaleMode: VideoScaleMode,
    positionMs: Long,
    durationMs: Long,
    title: String?,
    meta: String?,
    onBack: () -> Unit,
    onToggleOrientation: () -> Unit,
    onCycleScaleMode: () -> Unit,
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
                            Color.Black.copy(alpha = 0.75f),
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
                            Color.Black.copy(alpha = 0.85f),
                        ),
                    ),
                ),
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onBack,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
            ) {
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                meta?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Aspect Ratio / Scale Mode button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCycleScaleMode,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = scaleMode.icon,
                    contentDescription = "Aspect Ratio: ${scaleMode.label}",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            CastButton(modifier = Modifier.size(36.dp))
        }

        // Center controls — strictly centered on screen without asymmetric navigation bar padding
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            PlayerIconButton(
                icon = Icons.Rounded.Replay10,
                onClick = { onSeekBy(-SEEK_BUTTON_MS) },
            )

            // Central Play/Pause button with concentric buffering indicator
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .size(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Background circle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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

                // Buffer indicator perfectly concentric around play/pause circle
                if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(72.dp),
                        color = Color.White,
                        strokeWidth = 3.5.dp,
                    )
                }
            }

            PlayerIconButton(
                icon = Icons.Rounded.Forward10,
                onClick = { onSeekBy(SEEK_BUTTON_MS) },
            )
        }

        // Bottom progress & controls bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = formatPlayerTime(positionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 11.sp,
                    )
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                    )
                    Text(
                        text = formatPlayerTime(durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                    )
                }

                // Screen Rotation button
                val rotationInteractionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .clickable(
                            interactionSource = rotationInteractionSource,
                            indication = null,
                            onClick = onToggleOrientation,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isLandscape) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                        contentDescription = if (isLandscape) "Exit Fullscreen" else "Fullscreen / Rotate",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
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

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
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