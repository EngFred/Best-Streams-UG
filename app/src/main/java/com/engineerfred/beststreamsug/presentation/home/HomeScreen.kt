package com.engineerfred.beststreamsug.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.engineerfred.beststreamsug.R
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.Banner
import com.engineerfred.beststreamsug.domain.model.ContentSection
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.HomeCategoryRail
import com.engineerfred.beststreamsug.domain.model.HomeContent
import com.engineerfred.beststreamsug.presentation.common.PosterCard
import com.engineerfred.beststreamsug.presentation.common.RailHeader
import com.engineerfred.beststreamsug.presentation.common.skeletons.LoadingRailRow

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onContentSelected: (ContentSummary) -> Unit = {},
    onBrowseSelected: () -> Unit = {},
    onSeriesSelected: () -> Unit = {},
    onSearchSelected: () -> Unit = {},
    onOpenSection: (String) -> Unit = {},
    onOpenCategory: (categoryId: Int, name: String) -> Unit = { _, _ -> },
    onOpenVj: (vjId: Int, name: String) -> Unit = { _, _ -> },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onRetry = viewModel::refresh,
        onContentSelected = onContentSelected,
        onBrowseSelected = onBrowseSelected,
        onSeriesSelected = onSeriesSelected,
        onSearchSelected = onSearchSelected,
        onOpenSection = onOpenSection,
        onOpenCategory = onOpenCategory,
        onOpenVj = onOpenVj,
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onContentSelected: (ContentSummary) -> Unit = {},
    onBrowseSelected: () -> Unit = {},
    onSeriesSelected: () -> Unit = {},
    onSearchSelected: () -> Unit = {},
    onOpenSection: (String) -> Unit = {},
    onOpenCategory: (categoryId: Int, name: String) -> Unit = { _, _ -> },
    onOpenVj: (vjId: Int, name: String) -> Unit = { _, _ -> },
) {
    when {
        state.isLoading && state.content == null -> LoadingState()
        state.content != null -> HomeContentScreen(
            content = state.content,
            isRecentlyAddedLoading = state.isRecentlyAddedLoading,
            isCategoryRailsLoading = state.isCategoryRailsLoading,
            onContentSelected = onContentSelected,
            onBrowseSelected = onBrowseSelected,
            onSeriesSelected = onSeriesSelected,
            onSearchSelected = onSearchSelected,
            onOpenSection = onOpenSection,
            onOpenCategory = onOpenCategory,
            onOpenVj = onOpenVj,
        )
        else -> ErrorState(state.error, onRetry)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HomeContentScreen(
    content: HomeContent,
    isRecentlyAddedLoading: Boolean,
    isCategoryRailsLoading: Boolean,
    onContentSelected: (ContentSummary) -> Unit,
    onBrowseSelected: () -> Unit,
    onSeriesSelected: () -> Unit,
    onSearchSelected: () -> Unit,
    onOpenSection: (String) -> Unit,
    onOpenCategory: (categoryId: Int, name: String) -> Unit,
    onOpenVj: (vjId: Int, name: String) -> Unit,
) {
    val banners = content.banners.contentOrEmpty()
    var selectedBannerIndex by remember { mutableIntStateOf(0) }
    val heroFocusRequester = remember { FocusRequester() }
    val topBarFocusRequester = remember { FocusRequester() }

    val listState = rememberLazyListState()
    var isHeroFocused by remember { mutableStateOf(false) }
    var isTopBarFocused by remember { mutableStateOf(false) }

    // When focus moves to the hero or top bar, scroll cleanly back to the absolute top
    LaunchedEffect(isHeroFocused, isTopBarFocused) {
        if (isHeroFocused || isTopBarFocused) {
            listState.animateScrollToItem(0, 0)
        }
    }

    // Derive whether the user has scrolled down past the hero
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20
        }
    }

    // Animate top bar solid background: transparent at top → solid dark when scrolled
    val barBgAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 0.96f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "topBarAlpha",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 56.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Cinematic Hero Stage with Selector Strip
            if (banners.isNotEmpty()) {
                val currentBanner = banners.getOrElse(selectedBannerIndex) { banners.first() }
                item {
                    HeroStage(
                        banner = currentBanner,
                        banners = banners,
                        selectedIndex = selectedBannerIndex,
                        heroFocusRequester = heroFocusRequester,
                        topBarFocusRequester = topBarFocusRequester,
                        modifier = Modifier.onFocusChanged { isHeroFocused = it.hasFocus },
                        onSelectIndex = { selectedBannerIndex = it },
                        onContentSelected = onContentSelected,
                    )
                }
            }

            // Content Sections
            items(
                items = content.sections.contentOrEmpty(),
                key = { "section_${it.id}" },
            ) { section ->
                SectionRail(
                    section = section,
                    onContentSelected = onContentSelected,
                    onOpenSection = onOpenSection,
                    onOpenCategory = onOpenCategory,
                    onOpenVj = onOpenVj,
                    onBrowseDirectory = onBrowseSelected,
                )
            }

            if (isCategoryRailsLoading) {
                item {
                    LoadingRailRow(title = "More categories")
                }
            }

            // Category Rails
            items(
                items = content.categoryRails.contentOrEmpty(),
                key = { "rail_${it.category.id}" },
            ) { rail ->
                CategoryRail(
                    rail = rail,
                    onContentSelected = onContentSelected,
                    onOpenCategory = onOpenCategory,
                )
            }

            // Catalog Footer Info
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "BestStreams • ${content.categories.contentOrEmpty().size} categories • ${content.languages.contentOrEmpty().size} VJ channels",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        }

        // Pinned Top Navigation Bar
        // Transparent when at top (scrim gradient allows hero fan art to bleed through)
        // Solid dark background when scrolled down so rail content never conflicts
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .onFocusChanged { isTopBarFocused = it.hasFocus }
                .background(Color(0xFF0C0E15).copy(alpha = barBgAlpha))
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = if (isScrolled) 0f else 0.85f),
                        0.6f to Color.Black.copy(alpha = if (isScrolled) 0f else 0.40f),
                        1f to Color.Transparent,
                    ),
                )
                .padding(bottom = 16.dp),
        ) {
            TopNavigationBar(
                heroFocusRequester = heroFocusRequester,
                topBarFocusRequester = topBarFocusRequester,
                onSearchSelected = onSearchSelected,
                onSeriesSelected = onSeriesSelected,
                onBrowseSelected = onBrowseSelected,
            )
        }
    }
}


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TopNavigationBar(
    heroFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
    onSearchSelected: () -> Unit,
    onSeriesSelected: () -> Unit,
    onBrowseSelected: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // App Branding
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo_mark_cinematic),
                contentDescription = null,
                modifier = Modifier.size(38.dp),
            )
            Text(
                text = "BestStreams",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Navigation Tabs / Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavPill(
                label = "Search",
                icon = Icons.Default.Search,
                modifier = Modifier.focusProperties {
                    down = heroFocusRequester
                },
                onClick = onSearchSelected,
            )
            NavPill(
                label = "Home",
                icon = Icons.Default.Home,
                isActive = true,
                modifier = Modifier
                    .focusRequester(topBarFocusRequester)
                    .focusProperties {
                        down = heroFocusRequester
                    },
                onClick = {},
            )
            NavPill(
                label = "Series",
                icon = Icons.Default.Tv,
                modifier = Modifier.focusProperties {
                    down = heroFocusRequester
                },
                onClick = onSeriesSelected,
            )
            NavPill(
                label = "Browse",
                icon = Icons.Default.GridView,
                modifier = Modifier.focusProperties {
                    down = heroFocusRequester
                },
                onClick = onBrowseSelected,
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NavPill(
    label: String,
    icon: ImageVector,
    isActive: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused },
        shape = CardDefaults.shape(
            shape = RoundedCornerShape(20.dp),
            focusedShape = RoundedCornerShape(20.dp),
        ),
        scale = CardDefaults.scale(
            scale = 1f,
            focusedScale = 1.06f,
        ),
        border = CardDefaults.border(
            border = if (isActive) {
                Border(
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(20.dp),
                )
            } else {
                Border.None
            },
            focusedBorder = Border(
                border = BorderStroke(2.dp, Color.White),
                shape = RoundedCornerShape(20.dp),
            ),
        ),
        colors = CardDefaults.colors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            } else {
                Color.White.copy(alpha = 0.06f)
            },
            focusedContainerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        val contentColor = when {
            isFocused -> Color.Black
            isActive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = contentColor,
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HeroStage(
    banner: Banner,
    banners: List<Banner>,
    selectedIndex: Int,
    heroFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onSelectIndex: (Int) -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
) {
    val content = banner.content
    val genres = banner.categoryNames.take(3).joinToString(" • ")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(460.dp),
    ) {
        // Full Bleed Fan Art Backdrop
        AsyncImage(
            model = content.landscapeUrl ?: content.thumbnailUrl,
            contentDescription = content.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Multi-layer Scrim Gradients for Absolute Legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.95f),
                        0.42f to Color.Black.copy(alpha = 0.88f),
                        0.68f to Color.Black.copy(alpha = 0.40f),
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
                        0.55f to Color.Transparent,
                        0.90f to MaterialTheme.colorScheme.background.copy(alpha = 0.90f),
                        1f to MaterialTheme.colorScheme.background,
                    ),
                ),
        )

        // Left-Aligned Hero Details
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 48.dp, end = 48.dp, top = 64.dp)
                .width(640.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Badges & Metadata Tagline
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                content.vjName?.let { vj ->
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

                if (content.isPremium) {
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

                if (content.averageRating > 0) {
                    Text(
                        text = "★ ${"%.1f".format(content.averageRating)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFFC107),
                    )
                }

                content.releaseDate?.takeIf { it.isNotBlank() }?.let { date ->
                    Text(
                        text = date.take(4),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }

                if (genres.isNotEmpty()) {
                    Text(
                        text = "•  $genres",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.80f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Cinematic Display Title
            Text(
                text = content.title,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            // Synopsis Teaser
            content.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Primary CTAs
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Card(
                    onClick = { onContentSelected(content) },
                    modifier = Modifier
                        .focusRequester(heroFocusRequester)
                        .focusProperties {
                            up = topBarFocusRequester
                        },
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
                            text = "Watch Now",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black,
                        )
                    }
                }

                Card(
                    onClick = { onContentSelected(content) },
                    modifier = Modifier.focusProperties {
                        up = topBarFocusRequester
                    },
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
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "More Info",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Thumbnail Carousel Selector Strip
            if (banners.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    itemsIndexed(banners) { index, item ->
                        val isSelected = index == selectedIndex
                        HeroThumbnailPill(
                            banner = item,
                            isSelected = isSelected,
                            heroFocusRequester = heroFocusRequester,
                            onFocus = { onSelectIndex(index) },
                            onClick = {
                                onSelectIndex(index)
                                onContentSelected(item.content)
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HeroThumbnailPill(
    banner: Banner,
    isSelected: Boolean,
    heroFocusRequester: FocusRequester,
    onFocus: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(130.dp)
            .height(74.dp)
            .focusProperties {
                up = heroFocusRequester
            }
            .onFocusChanged { if (it.isFocused) onFocus() },
        shape = CardDefaults.shape(
            shape = RoundedCornerShape(8.dp),
            focusedShape = RoundedCornerShape(8.dp),
        ),
        scale = CardDefaults.scale(
            scale = if (isSelected) 1.02f else 0.98f,
            focusedScale = 1.10f,
        ),
        border = CardDefaults.border(
            border = if (isSelected) {
                Border(
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                )
            } else {
                Border(
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                )
            },
            focusedBorder = Border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
            ),
        ),
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = banner.content.landscapeUrl ?: banner.content.thumbnailUrl,
                contentDescription = banner.content.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (!isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.40f)),
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SectionRail(
    section: ContentSection,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenSection: (String) -> Unit,
    onOpenCategory: (categoryId: Int, name: String) -> Unit,
    onOpenVj: (vjId: Int, name: String) -> Unit,
    onBrowseDirectory: () -> Unit,
) {
    val isLanguageLayout = section.layout == "language"
    val isCategoryLayout = section.layout == "category"
    val seeAllFocusRequester = remember { FocusRequester() }

    if (section.items.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when {
            isLanguageLayout -> RailHeader(
                title = section.title,
                modifier = Modifier.padding(horizontal = 48.dp),
                seeAllFocusRequester = seeAllFocusRequester,
                onSeeAll = onBrowseDirectory,
            )
            isCategoryLayout -> RailHeader(
                title = section.title,
                modifier = Modifier.padding(horizontal = 48.dp),
                seeAllFocusRequester = seeAllFocusRequester,
                onSeeAll = onBrowseDirectory,
            )
            else -> RailHeader(
                title = section.title,
                modifier = Modifier.padding(horizontal = 48.dp),
                seeAllFocusRequester = seeAllFocusRequester,
                onSeeAll = { onOpenSection(section.title) },
            )
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties {
                    up = seeAllFocusRequester
                },
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 10.dp, bottom = 14.dp),
        ) {
            items(section.items, key = { it.id }) { item ->
                when {
                    isLanguageLayout -> VjCard(item, onClick = { onOpenVj(item.id, item.title) })
                    isCategoryLayout -> GenreCard(item, onClick = { onOpenCategory(item.id, item.title) })
                    else -> PosterCard(item, onClick = { onContentSelected(item) })
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun VjCard(
    content: ContentSummary,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(116.dp),
    ) {
        Card(
            onClick = onClick,
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
            AsyncImage(
                model = content.thumbnailUrl ?: content.landscapeUrl,
                contentDescription = content.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Text(
            text = content.title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun GenreCard(
    content: ContentSummary,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(96.dp),
        shape = CardDefaults.shape(
            shape = RoundedCornerShape(12.dp),
            focusedShape = RoundedCornerShape(12.dp),
        ),
        scale = CardDefaults.scale(
            scale = 1f,
            focusedScale = 1.08f,
        ),
        border = CardDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
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
                elevation = 8.dp,
            ),
        ),
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = content.thumbnailUrl ?: content.landscapeUrl,
                contentDescription = content.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.45f),
                            1f to Color.Black.copy(alpha = 0.85f),
                        ),
                    ),
            )
            Text(
                text = content.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                maxLines = 2,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp),
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CategoryRail(
    rail: HomeCategoryRail,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenCategory: (categoryId: Int, name: String) -> Unit,
) {
    val content = rail.content.pageContentOrEmpty()
    val seeAllFocusRequester = remember { FocusRequester() }
    if (content.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RailHeader(
            title = rail.category.name,
            modifier = Modifier.padding(horizontal = 48.dp),
            seeAllFocusRequester = seeAllFocusRequester,
            onSeeAll = { onOpenCategory(rail.category.id, rail.category.name) },
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

@Composable
private fun LoadingState() {
    HomeLoadingSkeleton()
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ErrorState(
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

private fun <T> com.engineerfred.beststreamsug.core.common.AppResult<List<T>>.contentOrEmpty(): List<T> =
    when (this) {
        is com.engineerfred.beststreamsug.core.common.AppResult.Success -> data
        is com.engineerfred.beststreamsug.core.common.AppResult.Failure -> emptyList()
    }

private fun <T> com.engineerfred.beststreamsug.core.common.AppResult<Page<T>>.pageContentOrEmpty(): List<T> =
    when (this) {
        is com.engineerfred.beststreamsug.core.common.AppResult.Success -> data.items
        is com.engineerfred.beststreamsug.core.common.AppResult.Failure -> emptyList()
    }

private fun AppError?.userMessage(): String = when (this) {
    AppError.NetworkUnavailable -> "Network unavailable"
    AppError.Timeout -> "The service took too long to respond"
    is AppError.Http -> "The service is temporarily unavailable"
    is AppError.Serialization, is AppError.InvalidResponse -> "The service returned invalid data"
    AppError.EmptyResponse -> "No Home content is available"
    is AppError.Unknown, null -> "Unable to load Home"
}
