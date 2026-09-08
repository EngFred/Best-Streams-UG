package com.engineerfred.beststreamsug.mobile.presentation.series.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.SeriesCategoryRail
import com.engineerfred.beststreamsug.mobile.ui.components.SectionHeader

@Composable
fun SeriesCategoryRail(
    rail: SeriesCategoryRail,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenCategory: (categoryId: Int, title: String) -> Unit,
) {
    val result = rail.content
    if (result !is AppResult.Success) return
    val items = result.data
    if (items.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(
            title = rail.categoryName,
            subtitle = null,
            onSeeAll = { onOpenCategory(rail.categoryId, rail.categoryName) },
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = items,
                key = { "series_card_${it.id}" },
            ) { item ->
                SeriesPosterCard(
                    content = item,
                    onClick = { onContentSelected(item) },
                )
            }
        }
    }
}

@Composable
private fun SeriesPosterCard(
    content: ContentSummary,
    onClick: () -> Unit,
) {
    com.engineerfred.beststreamsug.mobile.ui.components.ContentPosterCard(
        content = content,
        onClick = onClick,
    )
}