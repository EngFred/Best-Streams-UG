package com.engineerfred.beststreamsug.mobile.ui.components.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class ShimmerColors(
    val base: Color,
    val highlight: Color,
)

@Composable
fun rememberShimmerColors(
    baseColor: Color = Color(0xFF1E222A),
    highlightColor: Color = Color(0xFF2E343E),
): ShimmerColors = ShimmerColors(baseColor, highlightColor)

@Composable
private fun shimmerBrush(colors: ShimmerColors): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, 0, LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    return Brush.linearGradient(
        colors = listOf(colors.base, colors.highlight, colors.base),
        start = Offset(
            x = translateAnim - 600f,
            y = translateAnim - 600f,
        ),
        end = Offset(
            x = translateAnim,
            y = translateAnim,
        ),
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    colors: ShimmerColors = rememberShimmerColors(),
) {
    val brush = shimmerBrush(colors)
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush),
    )
}

@Composable
fun ShimmerCircle(
    modifier: Modifier = Modifier,
    colors: ShimmerColors = rememberShimmerColors(),
) {
    val brush = shimmerBrush(colors)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(brush),
    )
}

@Composable
fun ShimmerRect(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    colors: ShimmerColors = rememberShimmerColors(),
) {
    val brush = shimmerBrush(colors)
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush),
    )
}

