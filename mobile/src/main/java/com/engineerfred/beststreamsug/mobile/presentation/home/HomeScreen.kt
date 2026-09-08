package com.engineerfred.beststreamsug.mobile.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.HomeContent
import com.engineerfred.beststreamsug.mobile.presentation.home.components.HomeCategoryRail
import com.engineerfred.beststreamsug.mobile.presentation.home.components.HomeCategoryRailLoadingBlock
import com.engineerfred.beststreamsug.mobile.presentation.home.components.HomeHeroCarousel
import com.engineerfred.beststreamsug.mobile.presentation.home.components.HomeSectionRail
import com.engineerfred.beststreamsug.mobile.presentation.home.components.HomeShimmerSkeleton

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onContentSelected: (ContentSummary) -> Unit,
    onOpenCategory: (categoryId: Int, title: String) -> Unit,
    onOpenLanguage: (languageId: Int, title: String) -> Unit,
    onOpenSection: (sectionId: Int, title: String) -> Unit,
    onBrowseSelected: () -> Unit,
    onSearchSelected: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onRetry = viewModel::refresh,
        onContentSelected = onContentSelected,
        onOpenCategory = onOpenCategory,
        onOpenLanguage = onOpenLanguage,
        onOpenSection = onOpenSection,
        onBrowseSelected = onBrowseSelected,
        onSearchSelected = onSearchSelected,
    )
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenCategory: (categoryId: Int, title: String) -> Unit,
    onOpenLanguage: (languageId: Int, title: String) -> Unit,
    onOpenSection: (sectionId: Int, title: String) -> Unit,
    onBrowseSelected: () -> Unit,
    onSearchSelected: () -> Unit,
) {
    val content = state.content

    when {
        state.isLoading && content == null -> {
            HomeShimmerSkeleton(
                onSearchSelected = onSearchSelected,
            )
        }
        content != null -> {
            HomeContentScreen(
                content = content,
                isCategoryRailsLoading = state.isCategoryRailsLoading,
                onContentSelected = onContentSelected,
                onOpenCategory = onOpenCategory,
                onOpenLanguage = onOpenLanguage,
                onOpenSection = onOpenSection,
                onBrowseSelected = onBrowseSelected,
                onSearchSelected = onSearchSelected,
            )
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize()) {
                com.engineerfred.beststreamsug.mobile.ui.components.ErrorState(
                    error = state.error,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun HomeContentScreen(
    content: HomeContent,
    isCategoryRailsLoading: Boolean,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenCategory: (categoryId: Int, title: String) -> Unit,
    onOpenLanguage: (languageId: Int, title: String) -> Unit,
    onOpenSection: (sectionId: Int, title: String) -> Unit,
    onBrowseSelected: () -> Unit,
    onSearchSelected: () -> Unit,
) {
    val banners = (content.banners as? AppResult.Success)?.data.orEmpty()
    val sections = (content.sections as? AppResult.Success)?.data
        .orEmpty()
        .filter { it.items.isNotEmpty() }
    val categoryRails = (content.categoryRails as? AppResult.Success)?.data.orEmpty()
        .filter { rail ->
            (rail.content as? AppResult.Success)?.data?.items?.isNotEmpty() == true
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp),
    ) {
        item {
            HomeHeroCarousel(
                banners = banners,
                onContentSelected = onContentSelected,
                onSearchSelected = onSearchSelected,
            )
        }

        items(
            items = sections,
            key = { "section_${it.id}" },
        ) { section ->
            HomeSectionRail(
                section = section,
                onContentSelected = onContentSelected,
                onOpenCategory = onOpenCategory,
                onOpenLanguage = onOpenLanguage,
                onOpenSection = onOpenSection,
                onBrowseSelected = onBrowseSelected,
            )
        }

        if (isCategoryRailsLoading) {
            item {
                HomeCategoryRailLoadingBlock()
            }
        }

        items(
            items = categoryRails,
            key = { "rail_${it.category.id}" },
        ) { rail ->
            HomeCategoryRail(
                rail = rail,
                onContentSelected = onContentSelected,
                onOpenCategory = onOpenCategory,
            )
        }
    }
}