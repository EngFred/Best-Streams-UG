package com.engineerfred.beststreamsug.presentation.series

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Banner
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.SeriesCategoryRail
import com.engineerfred.beststreamsug.domain.model.SeriesHomeContent
import com.engineerfred.beststreamsug.presentation.common.PosterCard
import com.engineerfred.beststreamsug.presentation.common.RailHeader
import com.engineerfred.beststreamsug.presentation.common.skeletons.LoadingRailRow

@Composable
fun SeriesRoute(
    onBack: () -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenSection: (String) -> Unit = {},
    onOpenCategory: (categoryId: Int, name: String) -> Unit = { _, _ -> },
    viewModel: SeriesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SeriesScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onContentSelected = onContentSelected,
        onOpenSection = onOpenSection,
        onOpenCategory = onOpenCategory,
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeriesScreen(
    state: SeriesUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenSection: (String) -> Unit = {},
    onOpenCategory: (categoryId: Int, name: String) -> Unit = { _, _ -> },
) {
    when {
        state.isLoading -> SeriesLoadingSkeleton()
        state.content != null -> SeriesContentScreen(
            content = state.content,
            isCategoryRailsLoading = state.isCategoryRailsLoading,
            onContentSelected = onContentSelected,
            onOpenSection = onOpenSection,
            onOpenCategory = onOpenCategory,
        )
        else -> SeriesError(state.error, onRetry)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesContentScreen(
    content: SeriesHomeContent,
    isCategoryRailsLoading: Boolean,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenSection: (String) -> Unit,
    onOpenCategory: (categoryId: Int, name: String) -> Unit,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 48.dp, top = 32.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Series & TV Shows",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Binge-worthy seasonal dramas and translated series",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SeriesBannerRail(content, onContentSelected)
        }

        items(
            items = (content.sections as? AppResult.Success)?.data.orEmpty(),
            key = { "section_${it.id}" },
        ) { section ->
            SeriesRail(
                title = section.title,
                content = section.items,
                onContentSelected = onContentSelected,
                onSeeAll = { onOpenSection(section.title) },
            )
        }

        if (isCategoryRailsLoading) {
            item {
                LoadingRailRow(title = "Genre collections")
            }
        }

        items(content.categoryRails, key = { "rail_${it.categoryId}" }) { rail ->
            SeriesRail(rail, onContentSelected, onOpenCategory)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesBannerRail(
    content: SeriesHomeContent,
    onContentSelected: (ContentSummary) -> Unit,
) {
    val banners = (content.banners as? AppResult.Success)?.data.orEmpty()
    if (banners.isEmpty()) return
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 10.dp, bottom = 14.dp),
    ) {
        items(banners, key = { it.content.id }) { banner ->
            SeriesBannerCard(
                banner = banner,
                modifier = Modifier.width(460.dp).height(258.dp),
                onClick = { onContentSelected(banner.content) },
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesBannerCard(
    banner: Banner,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = banner.content
    val genres = banner.categoryNames.joinToString(" • ")

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = CardDefaults.shape(
            shape = RoundedCornerShape(12.dp),
            focusedShape = RoundedCornerShape(12.dp),
        ),
        scale = CardDefaults.scale(
            scale = 1f,
            focusedScale = 1.06f,
        ),
        border = CardDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
            ),
            focusedBorder = Border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
            ),
        ),
        glow = CardDefaults.glow(
            focusedGlow = Glow(
                elevationColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                elevation = 10.dp,
            ),
        ),
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = content.landscapeUrl ?: content.thumbnailUrl,
                contentDescription = content.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.90f),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (genres.isNotEmpty()) {
                    Text(
                        text = genres,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                content.vjName?.let { vj ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(4.dp),
                            )
                            .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.5.dp),
                    ) {
                        Text(
                            text = vj,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesRail(
    rail: SeriesCategoryRail,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenCategory: (categoryId: Int, name: String) -> Unit,
) {
    val series = (rail.content as? AppResult.Success)?.data.orEmpty()
    SeriesRail(
        title = rail.categoryName,
        content = series,
        onContentSelected = onContentSelected,
        onSeeAll = { onOpenCategory(rail.categoryId, rail.categoryName) },
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesRail(
    title: String,
    content: List<ContentSummary>,
    onContentSelected: (ContentSummary) -> Unit,
    onSeeAll: () -> Unit,
) {
    val seeAllFocusRequester = remember { FocusRequester() }
    if (content.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RailHeader(
            title = title,
            modifier = Modifier.padding(horizontal = 48.dp),
            seeAllFocusRequester = seeAllFocusRequester,
            onSeeAll = onSeeAll,
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties {
                    up = seeAllFocusRequester
                },
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 10.dp, bottom = 14.dp),
        ) {
            items(content, key = { it.id }) { item ->
                PosterCard(item, onClick = { onContentSelected(item) })
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesError(
    error: AppError?,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(error?.message() ?: "Unable to load Series", style = MaterialTheme.typography.headlineSmall)
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

private fun AppError.message(): String = when (this) {
    AppError.NetworkUnavailable -> "Network unavailable"
    AppError.Timeout -> "The service took too long to respond"
    is AppError.Http -> "The service is temporarily unavailable"
    is AppError.Serialization, is AppError.InvalidResponse -> "The service returned invalid data"
    AppError.EmptyResponse -> "No series are available"
    is AppError.Unknown -> "Unable to load Series"
}