package com.engineerfred.beststreamsug.presentation.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.domain.model.CastMember
import com.engineerfred.beststreamsug.domain.model.ContentDetails
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.Episode
import com.engineerfred.beststreamsug.domain.model.Season
import com.engineerfred.beststreamsug.presentation.common.PosterCard
import com.engineerfred.beststreamsug.presentation.common.RailHeader

@Composable
fun ContentDetailsRoute(
    onBack: () -> Unit,
    onPlay: (String, ContentDetails) -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
    viewModel: ContentDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ContentDetailsScreen(
        state = state,
        onPlay = onPlay,
        onSeasonSelected = viewModel::selectSeason,
        onContentSelected = onContentSelected,
        onRetry = viewModel::refresh,
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContentDetailsScreen(
    state: ContentDetailsUiState,
    onPlay: (String, ContentDetails) -> Unit,
    onSeasonSelected: (Int) -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
    onRetry: () -> Unit = {},
) {
    when {
        state.isLoading -> DetailsLoadingSkeleton()
        state.details != null -> DetailsContent(state, onPlay, onSeasonSelected, onContentSelected)
        else -> DetailsError(state.error, onRetry)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DetailsContent(
    state: ContentDetailsUiState,
    onPlay: (String, ContentDetails) -> Unit,
    onSeasonSelected: (Int) -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
) {
    val details = state.details ?: return
    val listState = rememberLazyListState()
    var isHeroFocused by remember { mutableStateOf(false) }

    LaunchedEffect(isHeroFocused) {
        if (isHeroFocused) {
            listState.animateScrollToItem(0, 0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 56.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        // Immersive Above-The-Fold Cinematic Hero
        item {
            DetailsHero(
                details = details,
                modifier = Modifier.onFocusChanged { isHeroFocused = it.hasFocus },
                onPlay = onPlay,
            )
        }

        // Stats & Tag Badges Bar
        item {
            StatPills(details)
        }

        // Cast & Crew Rail
        if (details.cast.isNotEmpty()) {
            item {
                CastSection(cast = details.cast)
            }
        }

        // Seasons Tab Bar (for TV Shows / Series)
        if (details.seasons.isNotEmpty()) {
            item {
                SeasonsSection(
                    seasons = details.seasons,
                    selectedSeasonId = state.selectedSeasonId,
                    onSeasonSelected = onSeasonSelected,
                )
            }
        }

        // Episodes Shelf (for TV Shows / Series)
        if (details.seasons.isNotEmpty()) {
            item {
                EpisodesSection(
                    state = state,
                    details = details,
                    onPlay = onPlay,
                )
            }
        }

        // Related Content / "You May Also Like"
        when {
            state.isLoadingRelated -> item {
                Text(
                    text = "Loading related…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 48.dp),
                )
            }
            state.relatedContent.isNotEmpty() -> item {
                RelatedSection(
                    items = state.relatedContent,
                    onContentSelected = onContentSelected,
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DetailsHero(
    details: ContentDetails,
    modifier: Modifier = Modifier,
    onPlay: (String, ContentDetails) -> Unit,
) {
    val summary = details.summary
    val genres = details.categoryNames.take(4).joinToString(" • ")
    val mainSource = details.playback.sources.firstOrNull()?.url
    val trailerUrl = details.playback.trailer?.url?.takeIf { it.isNotBlank() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(460.dp),
    ) {
        // High Resolution Landscape Fan Art
        AsyncImage(
            model = summary.landscapeUrl ?: summary.thumbnailUrl,
            contentDescription = summary.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Broadcast Multi-Layer Gradient Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.96f),
                        0.45f to Color.Black.copy(alpha = 0.90f),
                        0.72f to Color.Black.copy(alpha = 0.45f),
                        1f to Color.Transparent,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.50f to Color.Transparent,
                        0.88f to MaterialTheme.colorScheme.background.copy(alpha = 0.90f),
                        1f to MaterialTheme.colorScheme.background,
                    ),
                ),
        )

        // Left-Aligned Hero Content
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 48.dp, end = 48.dp, top = 24.dp)
                .width(660.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Badges & Tagline Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                summary.vjName?.let { vj ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(4.dp),
                            )
                            .border(
                                width = 0.5.dp,
                                color = Color.White.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = vj,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                        )
                    }
                }

                if (summary.isPremium) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFE5A93C).copy(alpha = 0.95f),
                                shape = RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "PREMIUM",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black,
                        )
                    }
                }

                if (summary.averageRating > 0) {
                    Text(
                        text = "★ ${"%.1f".format(summary.averageRating)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFFC107),
                    )
                }

                summary.releaseDate?.takeIf { it.isNotBlank() }?.let { date ->
                    Text(
                        text = date.take(4),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }

                summary.durationMillis?.let {
                    Text(
                        text = "•  ${formatDuration(it)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.80f),
                    )
                }

                details.languageName?.takeIf { it.isNotBlank() }?.let { dub ->
                    Text(
                        text = "•  $dub",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Cinematic Title
            Text(
                text = summary.title,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            // Genre String
            if (genres.isNotEmpty()) {
                Text(
                    text = genres,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.80f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Prominent Synopsis Above The Fold
            summary.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.84f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Primary Action CTAs
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Card(
                    onClick = { mainSource?.let { onPlay(it, details) } },
                    shape = CardDefaults.shape(
                        shape = RoundedCornerShape(8.dp),
                        focusedShape = RoundedCornerShape(8.dp),
                    ),
                    scale = CardDefaults.scale(
                        scale = 1f,
                        focusedScale = 1.08f,
                    ),
                    border = CardDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(2.dp, Color.White),
                            shape = RoundedCornerShape(8.dp),
                        ),
                    ),
                    glow = CardDefaults.glow(
                        focusedGlow = Glow(
                            elevationColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            elevation = 12.dp,
                        ),
                    ),
                    colors = CardDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = if (summary.kind == ContentKind.TV_SHOW) "Watch Episode" else "Play Movie",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black,
                        )
                    }
                }

                if (trailerUrl != null) {
                    Card(
                        onClick = { onPlay(trailerUrl, details) },
                        shape = CardDefaults.shape(
                            shape = RoundedCornerShape(8.dp),
                            focusedShape = RoundedCornerShape(8.dp),
                        ),
                        scale = CardDefaults.scale(
                            scale = 1f,
                            focusedScale = 1.08f,
                        ),
                        border = CardDefaults.border(
                            border = Border(
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp),
                            ),
                            focusedBorder = Border(
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                            ),
                        ),
                        colors = CardDefaults.colors(
                            containerColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = Color.White.copy(alpha = 0.25f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = "Trailer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun StatPills(details: ContentDetails) {
    val summary = details.summary
    val stats = listOfNotNull(
        summary.viewCount.takeIf { it > 0 }?.let { "Views" to formatCount(it) },
        summary.likeCount.takeIf { it > 0 }?.let { "Likes" to formatCount(it) },
        summary.averageRating.takeIf { it > 0 }?.let { "Rating" to "★ ${"%.1f".format(it)}" },
        summary.reviewCount.takeIf { it > 0 }?.let { "Reviews" to formatCount(it) },
        details.commentCount.takeIf { it > 0 }?.let { "Comments" to formatCount(it) },
        summary.releaseDate?.takeIf { it.isNotBlank() }?.let { "Released" to it },
    )
    if (stats.isEmpty()) return

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 6.dp, bottom = 6.dp),
    ) {
        items(stats) { (label, value) ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CastSection(
    cast: List<CastMember>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RailHeader(
            title = "Cast & Crew",
            modifier = Modifier.padding(horizontal = 48.dp),
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 10.dp, bottom = 14.dp),
        ) {
            items(cast, key = { it.id }) { member ->
                CastCard(member)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CastCard(member: CastMember) {
    Column(
        modifier = Modifier.width(116.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Card(
            onClick = {},
            modifier = Modifier.size(104.dp),
            shape = CardDefaults.shape(
                shape = CircleShape,
                focusedShape = CircleShape,
            ),
            scale = CardDefaults.scale(
                scale = 1f,
                focusedScale = 1.08f,
            ),
            border = CardDefaults.border(
                border = Border(
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    shape = CircleShape,
                ),
                focusedBorder = Border(
                    border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary),
                    shape = CircleShape,
                ),
            ),
            colors = CardDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (member.imageUrl != null) {
                    AsyncImage(
                        model = member.imageUrl,
                        contentDescription = member.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = initials(member.name),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Text(
            text = member.name,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        member.personalInfo?.takeIf { it.isNotBlank() }?.let { role ->
            Text(
                text = role,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeasonsSection(
    seasons: List<Season>,
    selectedSeasonId: Int?,
    onSeasonSelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RailHeader(
            title = "Seasons",
            modifier = Modifier.padding(horizontal = 48.dp),
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 10.dp, bottom = 14.dp),
        ) {
            items(seasons.sortedBy { it.sortOrder }, key = { it.id }) { season ->
                val isSelected = season.id == selectedSeasonId
                Card(
                    onClick = { onSeasonSelected(season.id) },
                    shape = CardDefaults.shape(
                        shape = RoundedCornerShape(20.dp),
                        focusedShape = RoundedCornerShape(20.dp),
                    ),
                    scale = CardDefaults.scale(
                        scale = 1f,
                        focusedScale = 1.06f,
                    ),
                    border = CardDefaults.border(
                        border = if (isSelected) {
                            Border(
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(20.dp),
                            )
                        } else {
                            Border(
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(20.dp),
                            )
                        },
                        focusedBorder = Border(
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(20.dp),
                        ),
                    ),
                    colors = CardDefaults.colors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = season.name,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun EpisodesSection(
    state: ContentDetailsUiState,
    details: ContentDetails,
    onPlay: (String, ContentDetails) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RailHeader(title = "Episodes")
        when {
            state.isLoadingEpisodes -> Text(
                text = "Loading episodes…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            state.episodesError != null -> Text(
                text = state.episodesError.userMessage(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            state.episodes.isEmpty() -> Text(
                text = "No episodes available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.episodes.sortedBy { it.sortOrder }.forEach { episode ->
                    EpisodeCard(episode) {
                        episode.playback.sources.firstOrNull()?.url
                            ?.let { onPlay(it, details) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun EpisodeCard(
    episode: Episode,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.shape(
            shape = RoundedCornerShape(12.dp),
            focusedShape = RoundedCornerShape(12.dp),
        ),
        scale = CardDefaults.scale(
            scale = 1f,
            focusedScale = 1.03f,
        ),
        border = CardDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                shape = RoundedCornerShape(12.dp),
            ),
            focusedBorder = Border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
            ),
        ),
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(112.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp)),
            ) {
                AsyncImage(
                    model = episode.thumbnailUrl ?: episode.landscapeUrl,
                    contentDescription = episode.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.6f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.7f),
                            ),
                        ),
                )
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                episode.description?.let {
                    Text(
                        text = it,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            episode.durationMillis?.let {
                Text(
                    text = formatDuration(it),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RelatedSection(
    items: List<ContentSummary>,
    onContentSelected: (ContentSummary) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RailHeader(
            title = "You May Also Like",
            modifier = Modifier.padding(horizontal = 48.dp),
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 10.dp, bottom = 14.dp),
        ) {
            items(items, key = { it.id }) { item ->
                PosterCard(
                    content = item,
                    modifier = Modifier
                        .width(160.dp)
                        .height(240.dp),
                    onClick = { onContentSelected(item) },
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DetailsError(
    error: AppError?,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = error.userMessage(),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(20.dp))
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
                text = "Retry",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalMinutes = ms / 60_000
    if (totalMinutes <= 0) return "${ms / 1000}s"
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

private fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
}

private fun initials(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercaseChar() }.joinToString("")

private fun AppError?.userMessage(): String = when (this) {
    AppError.NetworkUnavailable -> "Network unavailable"
    AppError.Timeout -> "The service took too long to respond"
    is AppError.Http -> "The service is temporarily unavailable"
    is AppError.Serialization, is AppError.InvalidResponse -> "The service returned invalid data"
    AppError.EmptyResponse -> "No details are available"
    is AppError.Unknown, null -> "Unable to load details"
}