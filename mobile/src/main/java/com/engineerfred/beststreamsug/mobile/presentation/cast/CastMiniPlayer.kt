package com.engineerfred.beststreamsug.mobile.presentation.cast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.engineerfred.beststreamsug.mobile.core.cast.CastManager
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicPrimary
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicSurface
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicSurfaceVariant
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicText
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

data class CastMiniState(
    val streamUrl: String,
    val title: String,
    val deviceName: String,
    val isPlaying: Boolean,
)

@Composable
fun CastMiniPlayer(
    onOpenPlayer: (CastMiniState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val castManager = rememberCastManager()

    val isCasting by castManager.isCasting.collectAsStateWithLifecycle()
    val isPlaying by castManager.isCastPlaying.collectAsStateWithLifecycle()
    val deviceName by castManager.connectedDeviceName.collectAsStateWithLifecycle()
    val title by castManager.currentCastTitle.collectAsStateWithLifecycle()
    val streamUrl by castManager.currentCastStreamUrl.collectAsStateWithLifecycle()

    var current by remember {
        mutableStateOf(
            if (isCasting) {
                CastMiniState(streamUrl, title, deviceName ?: "Android TV", isPlaying)
            } else {
                null
            },
        )
    }

    LaunchedEffect(isCasting, streamUrl, title, deviceName, isPlaying) {
        if (isCasting) {
            current = CastMiniState(streamUrl, title, deviceName ?: "Android TV", isPlaying)
        }
    }

    AnimatedVisibility(
        visible = isCasting && current != null,
        modifier = modifier,
        enter = slideInVertically(animationSpec = tween(300)) { it } + fadeIn(),
        exit = slideOutVertically(animationSpec = tween(300)) { it } + fadeOut(),
    ) {
        current?.let { state ->
            CastMiniBar(
                state = state,
                castManager = castManager,
                onOpenPlayer = onOpenPlayer,
            )
        }
    }
}

@Composable
private fun CastMiniBar(
    state: CastMiniState,
    castManager: CastManager,
    onOpenPlayer: (CastMiniState) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(CinematicSurface)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.06f)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onOpenPlayer(state) },
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CinematicSurfaceVariant, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.CastConnected,
                    contentDescription = null,
                    tint = CinematicPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.title.ifBlank { "Now Casting" },
                    style = MaterialTheme.typography.labelLarge,
                    color = CinematicText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Casting to ${state.deviceName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinematicMutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (state.isPlaying) castManager.pause() else castManager.play()
                },
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface CastManagerEntryPoint {
    fun castManager(): CastManager
}

@Composable
internal fun rememberCastManager(): CastManager {
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        EntryPointAccessors.fromApplication(
            appContext,
            CastManagerEntryPoint::class.java,
        ).castManager()
    }
}