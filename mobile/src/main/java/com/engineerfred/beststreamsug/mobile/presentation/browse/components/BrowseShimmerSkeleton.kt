package com.engineerfred.beststreamsug.mobile.presentation.browse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox

@Composable
fun BrowseShimmerSkeleton() {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            ShimmerBox(
                modifier = Modifier
                    .height(30.dp)
                    .fillMaxWidth(0.4f),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            ShimmerBox(
                modifier = Modifier
                    .height(18.dp)
                    .fillMaxWidth(0.5f),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(6) {
                item {
                    ShimmerBox(
                        modifier = Modifier
                            .height(96.dp)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}