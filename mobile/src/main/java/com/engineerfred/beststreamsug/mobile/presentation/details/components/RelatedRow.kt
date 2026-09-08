package com.engineerfred.beststreamsug.mobile.presentation.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.mobile.ui.components.ContentPosterCard

@Composable
fun RelatedRow(
    items: List<ContentSummary>,
    onContentSelected: (ContentSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = items,
            key = { "related_${it.id}" },
        ) { item ->
            ContentPosterCard(
                content = item,
                onClick = { onContentSelected(item) },
                modifier = Modifier.width(110.dp),
                showTitle = true,
            )
        }
    }
}