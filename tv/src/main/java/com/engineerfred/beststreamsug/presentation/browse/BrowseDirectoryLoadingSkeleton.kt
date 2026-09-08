package com.engineerfred.beststreamsug.presentation.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.presentation.common.skeletons.ShimmerBox

private const val GRID_COLUMNS = 6

@Composable
fun BrowseDirectoryLoadingSkeleton() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 36.dp, bottom = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Screen Header
        item(span = { GridItemSpan(GRID_COLUMNS) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShimmerBox(modifier = Modifier.width(220.dp).height(36.dp), shape = RoundedCornerShape(8.dp))
                ShimmerBox(modifier = Modifier.width(320.dp).height(20.dp), shape = RoundedCornerShape(6.dp))
            }
        }
        // Section 1 Header
        item(span = { GridItemSpan(GRID_COLUMNS) }) {
            ShimmerBox(modifier = Modifier.width(140.dp).height(28.dp), shape = RoundedCornerShape(6.dp))
        }
        // Section 1 Tiles
        items(GRID_COLUMNS) {
            BrowseTileSkeleton()
        }
        // Section 2 Header
        item(span = { GridItemSpan(GRID_COLUMNS) }) {
            ShimmerBox(modifier = Modifier.width(160.dp).height(28.dp), shape = RoundedCornerShape(6.dp))
        }
        // Section 2 Tiles
        items(GRID_COLUMNS) {
            BrowseTileSkeleton()
        }
    }
}

@Composable
private fun BrowseTileSkeleton() {
    Column(
        modifier = Modifier.width(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShimmerBox(
            modifier = Modifier.size(118.dp),
            shape = RoundedCornerShape(14.dp),
        )
        ShimmerBox(
            modifier = Modifier.width(100.dp).height(18.dp),
            shape = RoundedCornerShape(4.dp),
        )
    }
}