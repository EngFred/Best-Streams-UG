package com.engineerfred.beststreamsug.mobile.presentation.home.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.engineerfred.beststreamsug.domain.model.Banner
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicBackground
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicPrimary
import com.engineerfred.beststreamsug.mobile.ui.util.toSafeHttpsUrl
import kotlinx.coroutines.delay

@Composable
fun HomeHeroCarousel(
    banners: List<Banner>,
    onContentSelected: (ContentSummary) -> Unit,
    onSearchSelected: () -> Unit,
) {
    if (banners.isEmpty()) {
        HomeHeroEmpty(onSearchSelected)
        return
    }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { banners.size },
    )

    LaunchedEffect(pagerState.currentPage, pagerState.settledPage) {
        delay(6000)
        val nextPage = if (pagerState.currentPage == banners.lastIndex) 0 else pagerState.currentPage + 1
        pagerState.animateScrollToPage(
            page = nextPage,
            animationSpec = tween(700),
        )
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
        ) { pageIndex ->
            val banner = banners[pageIndex]
            HeroSlide(
                banner = banner,
                onClick = { onContentSelected(banner.content) },
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 14.dp, end = 16.dp)
                .size(44.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onSearchSelected,
                    ),
            )
        }

        // Indicator dots
        if (banners.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(banners.size) { index ->
                    val selected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .width(if (selected) 18.dp else 6.dp)
                            .height(6.dp)
                            .background(
                                color = if (selected) CinematicPrimary else Color.White.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(3.dp),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSlide(
    banner: Banner,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        AsyncImage(
            model = (banner.content.landscapeUrl ?: banner.content.thumbnailUrl).toSafeHttpsUrl(),
            contentDescription = banner.content.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.35f to Color.Transparent,
                        0.72f to Color.Transparent,
                        1f to CinematicBackground,
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                banner.categoryNames.take(2).forEach { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
            Text(
                text = banner.content.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.9f),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeroActionButton(
                    icon = Icons.Rounded.PlayArrow,
                    label = "Play",
                    isPrimary = true,
                    onClick = onClick,
                )
                HeroActionButton(
                    icon = Icons.Rounded.Info,
                    label = "More Info",
                    isPrimary = false,
                    onClick = onClick,
                )
            }
        }
    }
}

@Composable
private fun HeroActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(
                color = if (isPrimary) Color.White else Color(0xFF2B2F37).copy(alpha = 0.85f),
                shape = RoundedCornerShape(22.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 11.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isPrimary) CinematicBackground else Color.White,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isPrimary) CinematicBackground else Color.White,
        )
    }
}

@Composable
private fun HomeHeroEmpty(onSearchSelected: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CinematicBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "BestStreams",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            val interactionSource = remember { MutableInteractionSource() }
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onSearchSelected,
                    ),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}