package com.engineerfred.beststreamsug.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicBackground
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicPrimary
import com.engineerfred.beststreamsug.mobile.ui.theme.PremiumGold
import com.engineerfred.beststreamsug.mobile.ui.util.formatMinutes
import com.engineerfred.beststreamsug.mobile.ui.util.formatRating
import com.engineerfred.beststreamsug.mobile.ui.util.toSafeHttpsUrl

@Composable
fun ContentPosterCard(
    content: ContentSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    width: Dp = 120.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .width(width)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            AsyncImage(
                model = (content.thumbnailUrl ?: content.landscapeUrl).toSafeHttpsUrl(),
                contentDescription = content.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.6f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.55f),
                        ),
                    ),
            )
            if (content.isPremium) {
                PremiumTagBadge()
            }
            if (content.averageRating > 0) {
                RatingBadge(rating = content.averageRating)
            }
        }
        if (showTitle) {
            Text(
                text = content.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
fun ContentLandscapeCard(
    content: ContentSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
        ) {
            AsyncImage(
                model = (content.landscapeUrl ?: content.thumbnailUrl).toSafeHttpsUrl(),
                contentDescription = content.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0.8f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.35f),
                        ),
                    ),
            )
            if (content.isPremium) {
                Box(modifier = Modifier.align(Alignment.TopStart)) {
                    PremiumTagBadge()
                }
            }
            content.durationMillis?.let { duration ->
                if (duration > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(
                                Color.Black.copy(alpha = 0.65f),
                                RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = duration.formatMinutes(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }
        if (showTitle) {
            Text(
                text = content.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
fun ContentWideGridCard(
    content: ContentSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            AsyncImage(
                model = (content.landscapeUrl ?: content.thumbnailUrl).toSafeHttpsUrl(),
                contentDescription = content.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.55f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.6f),
                        ),
                    ),
            )
            if (content.isPremium) {
                Box(modifier = Modifier.align(Alignment.TopStart)) {
                    PremiumTagBadge()
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (content.averageRating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = CinematicPrimary,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = content.averageRating.formatRating(),
                                style = MaterialTheme.typography.labelSmall,
                                color = CinematicMutedText,
                            )
                        }
                    }
                    content.durationMillis?.let { duration ->
                        if (duration > 0) {
                            Text(
                                text = duration.formatMinutes(),
                                style = MaterialTheme.typography.labelSmall,
                                color = CinematicMutedText,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(CinematicPrimary.copy(alpha = 0.95f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            tint = CinematicBackground,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumTagBadge() {
    Box(
        modifier = Modifier
            .padding(7.dp)
            .background(PremiumGold, RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Text(
            text = "PREMIUM",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
            ),
            color = Color.Black,
        )
    }
}

@Composable
private fun BoxScope.RatingBadge(rating: Double) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(7.dp)
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            tint = CinematicPrimary,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = rating.formatRating(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
        )
    }
}