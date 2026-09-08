package com.engineerfred.beststreamsug.mobile.presentation.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.domain.model.ContentDetails
import com.engineerfred.beststreamsug.domain.model.Episode
import com.engineerfred.beststreamsug.mobile.presentation.details.DetailsUiState
import com.engineerfred.beststreamsug.mobile.ui.components.ErrorState
import com.engineerfred.beststreamsug.mobile.ui.components.ShimmerImage
import com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicPrimary
import com.engineerfred.beststreamsug.mobile.ui.util.formatMinutes
import com.engineerfred.beststreamsug.mobile.ui.util.toSafeHttpsUrl

@Composable
fun SeasonsSection(
    state: DetailsUiState,
    details: ContentDetails,
    onSelectSeason: (Int) -> Unit,
    onPlay: (url: String, title: String?, meta: String?, poster: String?) -> Unit,
) {
    val seasons = details.seasons.sortedBy { it.sortOrder }
    val selectedSeasonId = state.selectedSeasonId ?: seasons.firstOrNull()?.id

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        Text(
            text = "Episodes",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 10.dp),
        )

        if (seasons.size > 1) {
            SeasonSelector(
                seasons = seasons,
                selectedSeasonId = selectedSeasonId,
                onSelectSeason = onSelectSeason,
            )
        }

        when {
            state.isLoadingEpisodes -> {
                Spacer(modifier = Modifier.height(8.dp))
                EpisodesSkeleton()
            }
            state.episodesError != null -> {
                val lastSeasonId = selectedSeasonId
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                ) {
                    ErrorState(
                        error = state.episodesError,
                        onRetry = { if (lastSeasonId != null) onSelectSeason(lastSeasonId) },
                    )
                }
            }
            state.episodes.isEmpty() -> {
                Text(
                    text = "No episodes available yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinematicMutedText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    state.episodes.sortedBy { it.sortOrder }.forEach { episode ->
                        EpisodeRow(
                            episode = episode,
                            index = episode.sortOrder,
                            onPlay = onPlay,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonSelector(
    seasons: List<com.engineerfred.beststreamsug.domain.model.Season>,
    selectedSeasonId: Int?,
    onSelectSeason: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        seasons.forEach { season ->
            val selected = season.id == selectedSeasonId
            val interactionSource = remember { MutableInteractionSource() }
            Text(
                text = season.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Color(0xFF08090B) else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        color = if (selected) CinematicPrimary else Color(0xFF2B2F37),
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onSelectSeason(season.id) },
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            )
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: Episode,
    index: Int,
    onPlay: (url: String, title: String?, meta: String?, poster: String?) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val url = episode.playback.sources.firstOrNull()?.url
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (url != null) {
                        onPlay(
                            url,
                            episode.title,
                            "Episode ${episode.sortOrder}",
                            episode.landscapeUrl ?: episode.thumbnailUrl,
                        )
                    }
                },
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1B1E24)),
        ) {
            (episode.thumbnailUrl ?: episode.landscapeUrl)?.let { img ->
                ShimmerImage(
                    model = img.toSafeHttpsUrl(),
                    contentDescription = episode.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(17.dp))
                    .padding(7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = episode.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                episode.durationMillis?.let { duration ->
                    if (duration > 0) {
                        Text(
                            text = duration.formatMinutes(),
                            style = MaterialTheme.typography.labelSmall,
                            color = CinematicMutedText,
                        )
                    }
                }
                if (episode.isPremium) {
                    Text(
                        text = "PREMIUM",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinematicPrimary,
                    )
                }
            }
            episode.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = CinematicMutedText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodesSkeleton() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        repeat(4) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(16f / 9f),
                    shape = RoundedCornerShape(8.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(180.dp)
                            .height(14.dp),
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .width(120.dp)
                            .height(10.dp),
                    )
                }
            }
        }
    }
}