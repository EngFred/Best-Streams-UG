package com.engineerfred.beststreamsug.mobile.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.domain.model.ContentSection
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.mobile.ui.components.ContentRail
import com.engineerfred.beststreamsug.mobile.ui.components.SectionHeader
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicSurfaceContainer

@Composable
fun HomeSectionRail(
    section: ContentSection,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenCategory: (categoryId: Int, title: String) -> Unit,
    onOpenLanguage: (languageId: Int, title: String) -> Unit,
    onOpenSection: (sectionId: Int, title: String) -> Unit,
    onBrowseSelected: () -> Unit,
) {
    if (section.items.isEmpty()) return

    val isNavSection = section.layout == "category" || section.layout == "language"

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(
            title = section.title,
            subtitle = null,
            onSeeAll = if (isNavSection) onBrowseSelected else {
                { onOpenSection(section.id, section.title) }
            },
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        when (section.layout) {
            "category" -> NavTileRow(section.items) { onOpenCategory(it.id, it.title) }
            "language" -> NavTileRow(section.items) { onOpenLanguage(it.id, it.title) }
            else -> ContentRail(items = section.items, onItemClick = onContentSelected)
        }
    }
}

@Composable
private fun NavTileRow(
    tiles: List<ContentSummary>,
    onTileClick: (ContentSummary) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(
            items = tiles,
            key = { "nav_tile_${it.id}" },
        ) { item ->
            NavTileCard(
                title = item.title,
                onClick = { onTileClick(item) },
            )
        }
    }
}

@Composable
private fun NavTileCard(
    title: String,
    onClick: () -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = CinematicMutedText,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CinematicSurfaceContainer)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 40.dp)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    )
}