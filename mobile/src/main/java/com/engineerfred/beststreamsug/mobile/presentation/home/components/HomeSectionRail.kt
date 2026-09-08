package com.engineerfred.beststreamsug.mobile.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentSection
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.mobile.ui.components.ContentRail
import com.engineerfred.beststreamsug.mobile.ui.components.SectionHeader

@Composable
fun HomeSectionRail(
    section: ContentSection,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenSection: (sectionId: Int, title: String) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(
            title = section.title,
            subtitle = null,
            onSeeAll = { onOpenSection(section.id, section.title) },
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        ContentRail(
            items = section.items,
            onItemClick = onContentSelected,
        )
    }
}