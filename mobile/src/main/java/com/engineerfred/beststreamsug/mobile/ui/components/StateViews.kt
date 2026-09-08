package com.engineerfred.beststreamsug.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicPrimary

@Composable
fun ErrorState(
    error: AppError?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = when (error) {
        is AppError.NetworkUnavailable -> "Network connection lost. Check your internet and retry."
        is AppError.Timeout -> "The request timed out. Check your connection and retry."
        is AppError.Http -> "Something went wrong on our side (${error.statusCode}). Please try again."
        is AppError.Serialization -> "We couldn't reach this content right now."
        is AppError.EmptyResponse -> "We couldn't reach this content right now."
        is AppError.InvalidResponse -> "We couldn't reach this content right now."
        is AppError.Unknown -> "Something unexpected happened. Please try again."
        null -> "Something went wrong. Please try again."
    }
    StateMessage(
        icon = Icons.Rounded.CloudOff,
        title = "Can't load content",
        message = message,
        actionLabel = "Retry",
        onAction = onRetry,
        modifier = modifier,
    )
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    StateMessage(
        icon = Icons.Rounded.Inbox,
        title = title,
        message = message,
        modifier = modifier,
    )
}

@Composable
fun StateMessage(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFF1B1E24), RoundedCornerShape(36.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CinematicMutedText,
                    modifier = Modifier.size(34.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = CinematicMutedText,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinematicPrimary,
                        contentColor = Color(0xFF08090B),
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}