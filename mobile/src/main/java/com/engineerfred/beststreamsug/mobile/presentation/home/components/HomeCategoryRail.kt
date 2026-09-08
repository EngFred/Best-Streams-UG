package com.engineerfred.beststreamsug.mobile.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.HomeCategoryRail
import com.engineerfred.beststreamsug.mobile.ui.components.ContentRail
import com.engineerfred.beststreamsug.mobile.ui.components.SectionHeader
import com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox

@Composable
fun HomeCategoryRail(
    rail: HomeCategoryRail,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenCategory: (categoryId: Int, title: String) -> Unit,
) {
    val result = rail.content
    if (result !is AppResult.Success) return
    val items = result.data.items
    if (items.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(
            title = rail.category.name,
            subtitle = null,
            onSeeAll = { onOpenCategory(rail.category.id, rail.category.name) },
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        ContentRail(
            items = items,
            onItemClick = onContentSelected,
        )
    }
}

@Composable
fun HomeCategoryRailLoadingBlock() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Spacer(modifier = Modifier.height(14.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            ShimmerBox(
                modifier = Modifier.height(16.dp),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
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
}