package com.engineerfred.beststreamsug.mobile.presentation.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.Language
import com.engineerfred.beststreamsug.mobile.presentation.browse.components.BrowseShimmerSkeleton
import com.engineerfred.beststreamsug.mobile.ui.components.ErrorState
import com.engineerfred.beststreamsug.mobile.ui.components.SectionHeader
import com.engineerfred.beststreamsug.mobile.ui.components.ShimmerImage
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

@Composable
private fun CategoryGrid(
    categories: List<Category>,
    onCategorySelected: (categoryId: Int, title: String) -> Unit,
    featured: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        categories.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowCategories.forEach { category ->
                    CategoryTile(
                        category = category,
                        featured = featured,
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

@Composable
private fun CategoryTile(
    category: Category,
    featured: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(if (featured) 88.dp else 64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(category.id.toCategoryColor())
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        category.imageUrl?.let { url ->
            ShimmerImage(
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
                    Brush.horizontalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.25f),
                        ),
                    ),
                ),
        )
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
        )
    }
}

@Composable
private fun LanguageGrid(
    languages: List<Language>,
    onLanguageSelected: (languageId: Int, title: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        languages.chunked(2).forEach { rowLanguages ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowLanguages.forEach { language ->
                    LanguageTile(
                        language = language,
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

@Composable
private fun LanguageTile(
    language: Language,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(language.id.toCategoryColor())
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = language.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}