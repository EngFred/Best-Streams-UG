package com.engineerfred.beststreamsug.mobile.presentation.browse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.Language
import com.engineerfred.beststreamsug.mobile.presentation.browse.components.BrowseShimmerSkeleton
import com.engineerfred.beststreamsug.mobile.ui.components.ErrorState
import com.engineerfred.beststreamsug.mobile.ui.components.ShimmerImage
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicBackground
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicSurface
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicSurfaceContainerHigh
import com.engineerfred.beststreamsug.mobile.ui.util.toSafeHttpsUrl

enum class BrowseTab(val title: String) {
    Categories("Categories"),
    VjChannels("VJ channels"),
}

@Composable
fun BrowseRoute(
    viewModel: BrowseViewModel = hiltViewModel(),
    onCategorySelected: (categoryId: Int, title: String) -> Unit,
    onLanguageSelected: (languageId: Int, title: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BrowseScreen(
        state = state,
        onRetry = viewModel::refresh,
        onCategorySelected = onCategorySelected,
        onLanguageSelected = onLanguageSelected,
    )
}

@Composable
private fun BrowseScreen(
    state: BrowseUiState,
    onRetry: () -> Unit,
    onCategorySelected: (categoryId: Int, title: String) -> Unit,
    onLanguageSelected: (languageId: Int, title: String) -> Unit,
) {
    when {
        state.isLoading -> BrowseShimmerSkeleton()
        state.categories.isNotEmpty() || state.languages.isNotEmpty() -> {
            var selectedTab by rememberSaveable { mutableStateOf(BrowseTab.Categories) }
            var searchQuery by rememberSaveable { mutableStateOf("") }
            var isCategoriesExpanded by rememberSaveable { mutableStateOf(false) }
            var isVjChannelsExpanded by rememberSaveable { mutableStateOf(false) }

            val allCategories = state.categories.sortedBy { it.sortOrder }
            val allLanguages = state.languages.sortedBy { it.sortOrder }

            val filteredCategories = remember(searchQuery, allCategories) {
                if (searchQuery.isBlank()) allCategories
                else allCategories.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
            }

            val filteredLanguages = remember(searchQuery, allLanguages) {
                if (searchQuery.isBlank()) allLanguages
                else allLanguages.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CinematicBackground),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                // Screen Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = "Browse",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Search-to-filter bar
                        BrowseSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onClear = { searchQuery = "" },
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Underline Tabs (Categories / VJ channels)
                        BrowseTabBar(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                        )
                    }
                }

                when (selectedTab) {
                    BrowseTab.Categories -> {
                        val displayCategories = if (searchQuery.isNotBlank() || isCategoriesExpanded) {
                            filteredCategories
                        } else {
                            filteredCategories.take(4)
                        }

                        if (filteredCategories.isEmpty()) {
                            item {
                                BrowseEmptySearch(query = searchQuery)
                            }
                        } else {
                            item {
                                Text(
                                    text = "GENRES",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = CinematicMutedText,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                )
                            }

                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    displayCategories.chunked(2).forEach { rowCategories ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            rowCategories.forEach { category ->
                                                BrowseCard(
                                                    title = category.name,
                                                    id = category.id,
                                                    imageUrl = category.imageUrl,
                                                    onClick = { onCategorySelected(category.id, category.name) },
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                            if (rowCategories.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            if (searchQuery.isBlank() && filteredCategories.size > 4) {
                                item {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    BrowseExpandButton(
                                        isExpanded = isCategoriesExpanded,
                                        label = if (isCategoriesExpanded) "Show less" else "Show all genres",
                                        onClick = { isCategoriesExpanded = !isCategoriesExpanded },
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                    )
                                }
                            }
                        }
                    }

                    BrowseTab.VjChannels -> {
                        val displayLanguages = if (searchQuery.isNotBlank() || isVjChannelsExpanded) {
                            filteredLanguages
                        } else {
                            filteredLanguages.take(4)
                        }

                        if (filteredLanguages.isEmpty()) {
                            item {
                                BrowseEmptySearch(query = searchQuery)
                            }
                        } else {
                            item {
                                Text(
                                    text = "VJ CHANNELS & LANGUAGES",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = CinematicMutedText,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                )
                            }

                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    displayLanguages.chunked(2).forEach { rowLanguages ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            rowLanguages.forEach { language ->
                                                BrowseCard(
                                                    title = language.name,
                                                    id = language.id,
                                                    imageUrl = null,
                                                    onClick = { onLanguageSelected(language.id, language.name) },
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                            if (rowLanguages.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            if (searchQuery.isBlank() && filteredLanguages.size > 4) {
                                item {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    BrowseExpandButton(
                                        isExpanded = isVjChannelsExpanded,
                                        label = if (isVjChannelsExpanded) "Show less" else "Show all VJ channels",
                                        onClick = { isVjChannelsExpanded = !isVjChannelsExpanded },
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize()) {
                ErrorState(
                    error = state.error,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun BrowseSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        placeholder = {
            Text(
                text = "Search categories, VJs, countries",
                style = MaterialTheme.typography.bodyMedium,
                color = CinematicMutedText,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = CinematicMutedText,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                val interactionSource = remember { MutableInteractionSource() }
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Clear",
                    tint = CinematicMutedText,
                    modifier = Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClear,
                        )
                        .padding(8.dp),
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF16181D),
            unfocusedContainerColor = Color(0xFF16181D),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
        ),
    )
}

@Composable
private fun BrowseTabBar(
    selectedTab: BrowseTab,
    onTabSelected: (BrowseTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        BrowseTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val interactionSource = remember { MutableInteractionSource() }

            Column(
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onTabSelected(tab) },
                    )
                    .padding(bottom = 6.dp),
            ) {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else CinematicMutedText,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .width(if (isSelected) 36.dp else 0.dp)
                        .height(3.dp)
                        .background(
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(1.5.dp),
                        ),
                )
            }
        }
    }
}

@Composable
private fun BrowseCard(
    title: String,
    id: Int,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val gradient = remember(id) { id.toHuluCategoryGradient() }

    Box(
        modifier = modifier
            .height(86.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(gradient)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        imageUrl?.let { url ->
            ShimmerImage(
                model = url.toSafeHttpsUrl(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.25f),
                            ),
                        ),
                    ),
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
        )
    }
}

@Composable
private fun BrowseExpandButton(
    isExpanded: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF14161B))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun BrowseEmptySearch(
    query: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No results for \"$query\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Try checking the spelling or use a different search term.",
            style = MaterialTheme.typography.bodySmall,
            color = CinematicMutedText,
            textAlign = TextAlign.Center,
        )
    }
}

private fun Int.toHuluCategoryGradient(): Brush {
    val gradients = listOf(
        listOf(Color(0xFF3E1B1E), Color(0xFF1E0E10)), // War / Deep Burgundy
        listOf(Color(0xFF4A154B), Color(0xFF200B21)), // Horror / Deep Magenta
        listOf(Color(0xFF4D2C10), Color(0xFF241408)), // Romance / Deep Amber
        listOf(Color(0xFF1B2B48), Color(0xFF0C1422)), // Fantasy / Deep Navy
        listOf(Color(0xFF0D3B32), Color(0xFF061B17)), // Adventure / Deep Teal
        listOf(Color(0xFF521822), Color(0xFF220A0E)), // Action / Deep Crimson
        listOf(Color(0xFF2B1D52), Color(0xFF130D24)), // Sci-Fi / Deep Violet
        listOf(Color(0xFF242F3D), Color(0xFF10161D)), // Thriller / Deep Slate
        listOf(Color(0xFF442D1D), Color(0xFF1E130B)), // Drama / Bronze
        listOf(Color(0xFF1A3B4A), Color(0xFF0B1A22)), // Mystery / Deep Cyan
        listOf(Color(0xFF3B1A4A), Color(0xFF180A20)), // Animation / Purple
        listOf(Color(0xFF2D3B1A), Color(0xFF131A0B)), // Documentary / Deep Olive
    )
    val colors = gradients[Math.abs(this) % gradients.size]
    return Brush.horizontalGradient(colors)
}