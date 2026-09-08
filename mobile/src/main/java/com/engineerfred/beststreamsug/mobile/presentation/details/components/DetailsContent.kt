package com.engineerfred.beststreamsug.mobile.presentation.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.domain.model.ContentDetails
import com.engineerfred.beststreamsug.mobile.presentation.details.DetailsUiState
import com.engineerfred.beststreamsug.mobile.ui.components.ShimmerImage
import com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicBackground
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicPrimary
import com.engineerfred.beststreamsug.mobile.ui.util.formatMinutes
import com.engineerfred.beststreamsug.mobile.ui.util.formatRating
import com.engineerfred.beststreamsug.mobile.ui.util.releaseYear
import com.engineerfred.beststreamsug.mobile.ui.util.toSafeHttpsUrl

private val BackdropHeight = 260.dp
private val ContentOverlap = 24.dp // how much the content card rises over the backdrop

@Composable
fun DetailsContent(
    state: DetailsUiState,
    details: ContentDetails,
    onBack: () -> Unit,
    onSelectSeason: (Int) -> Unit,
    onPlay: (url: String, title: String?, meta: String?, poster: String?) -> Unit,
    onContentSelected: (com.engineerfred.beststreamsug.domain.model.ContentSummary) -> Unit,
) {
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Fixed backdrop image ─────────────────────────────────────────────
        ShimmerImage(
            model = (details.summary.landscapeUrl ?: details.summary.thumbnailUrl).toSafeHttpsUrl(),
            contentDescription = details.summary.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(BackdropHeight),
            contentScale = ContentScale.Crop,
        )

        // Bottom-to-top gradient fading backdrop into background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BackdropHeight)
                .background(
                    Brush.verticalGradient(
                        0.35f to Color.Transparent,
                        1f to CinematicBackground,
                    ),
                ),
        )

        // ── Scrollable content ───────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Spacer that "reserves" space for the backdrop; content slides up over it
            item {
                Spacer(modifier = Modifier.height(BackdropHeight - ContentOverlap))
            }

            // Content card that rises over the backdrop
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(CinematicBackground),
                ) {
                    DetailsMetaSection(details = details, onPlay = onPlay)
                }
            }

            // Collapsible description — Sub-task A
            val description = details.summary.description
            if (!description.isNullOrBlank()) {
                item {
                    ExpandableDescription(
                        description = description,
                        modifier = Modifier
                            .background(CinematicBackground)
                            .padding(horizontal = 20.dp)
                            .padding(top = 4.dp, bottom = 8.dp),
                    )
                }
            }

            if (details.cast.isNotEmpty()) {
                item {
                    Text(
                        text = "Cast",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .background(CinematicBackground)
                            .padding(start = 20.dp, top = 16.dp, bottom = 10.dp)
                            .fillMaxWidth(),
                    )
                }
                item {
                    Box(modifier = Modifier.background(CinematicBackground)) {
                        CastRow(cast = details.cast)
                    }
                }
            }

            if (details.seasons.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.background(CinematicBackground)) {
                        SeasonsSection(
                            state = state,
                            details = details,
                            onSelectSeason = onSelectSeason,
                            onPlay = onPlay,
                        )
                    }
                }
            }

            if (state.isLoadingRelated) {
                item {
                    Text(
                        text = "More like this",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .background(CinematicBackground)
                            .padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
                            .fillMaxWidth(),
                    )
                }
                item {
                    Box(modifier = Modifier.background(CinematicBackground)) {
                        RelatedLoadingRow()
                    }
                }
            } else if (state.relatedContent.isNotEmpty()) {
                item {
                    Text(
                        text = "More like this",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .background(CinematicBackground)
                            .padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
                            .fillMaxWidth(),
                    )
                }
                item {
                    Box(modifier = Modifier.background(CinematicBackground)) {
                        RelatedRow(
                            items = state.relatedContent,
                            onContentSelected = onContentSelected,
                        )
                    }
                }
            }
        }

        // ── Fixed back button — does NOT scroll ──────────────────────────────
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onBack,
                    )
                    .padding(12.dp),
            )
        }
    }
}

// ── Sub-task A: Collapsible description ─────────────────────────────────────

@Composable
private fun ExpandableDescription(
    description: String,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!expanded) {
                    isOverflowing = result.hasVisualOverflow
                }
            },
        )

        if (isOverflowing || expanded) {
            val interactionSource = remember { MutableInteractionSource() }
            Text(
                text = if (expanded) "Show less ▴" else "Read more ▾",
                style = MaterialTheme.typography.labelMedium,
                color = CinematicPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { expanded = !expanded },
                    ),
            )
        }
    }
}

// ── Supporting composables ───────────────────────────────────────────────────

@Composable
private fun RelatedLoadingRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(5) {
            item {
                ShimmerBox(
                    modifier = Modifier
                        .height(180.dp)
                        .width(120.dp),
                )
            }
        }
    }
}

@Composable
private fun DetailsMetaSection(
    details: ContentDetails,
    onPlay: (url: String, title: String?, meta: String?, poster: String?) -> Unit,
) {
    val summary = details.summary
    val mainSource = summary.defaultVideoUrl ?: details.playback.sources.firstOrNull()?.url

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = summary.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (summary.averageRating > 0) {
                MetaPill(text = "★ ${summary.averageRating.formatRating()}")
            }
            summary.durationMillis?.let { duration ->
                if (duration > 0) {
                    MetaPill(text = duration.formatMinutes())
                }
            }
            summary.releaseDate?.let { date ->
                MetaPill(text = date.releaseYear())
            }
            summary.vjName?.let { vjName ->
                MetaPill(text = vjName)
            }
        }

        if (details.categoryNames.isNotEmpty()) {
            Text(
                text = details.categoryNames.joinToString(" • "),
                style = MaterialTheme.typography.labelMedium,
                color = CinematicMutedText,
                maxLines = 2,
            )
        }

        // Play button
        val playInteractionSource = remember { MutableInteractionSource() }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(CinematicPrimary)
                .clickable(
                    interactionSource = playInteractionSource,
                    indication = null,
                    onClick = {
                        mainSource?.let {
                            val language = details.languageName
                            onPlay(
                                it,
                                summary.title,
                                language ?: details.categoryNames.firstOrNull(),
                                summary.landscapeUrl ?: summary.thumbnailUrl,
                            )
                        }
                    },
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.Black.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = CinematicBackground,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Play",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CinematicBackground,
            )
        }
    }
}

@Composable
private fun MetaPill(
    text: String,
    highlight: Boolean = false,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = if (highlight) CinematicBackground else Color.White,
        modifier = Modifier
            .background(
                color = if (highlight) CinematicPrimary else Color(0xFF2B2F37),
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
