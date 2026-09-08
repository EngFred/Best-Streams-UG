package com.engineerfred.beststreamsug.mobile.presentation.browse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.Language
import com.engineerfred.beststreamsug.mobile.presentation.browse.components.BrowseShimmerSkeleton
import com.engineerfred.beststreamsug.mobile.ui.components.ErrorState
import com.engineerfred.beststreamsug.mobile.ui.components.SectionHeader
import com.engineerfred.beststreamsug.mobile.ui.util.toCategoryColor
import com.engineerfred.beststreamsug.mobile.ui.util.toSafeHttpsUrl

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
            val categories = state.categories.sortedBy { it.sortOrder }
            val languages = state.languages.sortedBy { it.sortOrder }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                    ) {
                        Text(
                            text = "Browse",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                if (categories.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Categories",
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                    item {
                        CategoryGrid(
                            categories = categories,
                            onCategorySelected = onCategorySelected,
                            featured = true,
                        )
                    }
                }

                if (languages.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "VJ Channels & Languages",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        )
                    }
                    item {
                        LanguageGrid(
                            languages = languages,
                            onLanguageSelected = onLanguageSelected,
                        )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryGrid(
    categories: List<Category>,
    onCategorySelected: (categoryId: Int, title: String) -> Unit,
    featured: Boolean,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = categories,
            key = { "cat_${it.id}" },
        ) { category ->
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (featured) 88.dp else 64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(category.id.toCategoryColor())
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onCategorySelected(category.id, category.name) },
                    ),
            ) {
                category.imageUrl?.let { url ->
                    AsyncImage(
                        model = url.toSafeHttpsUrl(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(
                                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.35f),
                                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.25f),
                                ),
                            ),
                        ),
                )
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = androidx.compose.ui.graphics.Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LanguageGrid(
    languages: List<Language>,
    onLanguageSelected: (languageId: Int, title: String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = languages,
            key = { "lang_${it.id}" },
        ) { language ->
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(language.id.toCategoryColor())
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onLanguageSelected(language.id, language.name) },
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = language.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = androidx.compose.ui.graphics.Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }
    }
}