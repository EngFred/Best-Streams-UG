package com.engineerfred.beststreamsug.mobile.presentation.search.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.mobile.ui.components.AppSearchTextField
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicSurface

@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppSearchTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search movies & series",
            onClear = onClearQuery,
            height = 52.dp,
            shape = RoundedCornerShape(14.dp),
            backgroundColor = CinematicSurface,
            modifier = Modifier.weight(1f),
        )
    }
}