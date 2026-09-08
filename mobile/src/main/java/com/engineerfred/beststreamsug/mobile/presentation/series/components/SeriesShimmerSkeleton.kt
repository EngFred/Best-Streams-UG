package com.engineerfred.beststreamsug.mobile.presentation.series.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox

@Composable
fun SeriesShimmerSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
            )
        }
        repeat(4) {
            item {
                SkeletonRailBlock(modifier = Modifier.padding(top = 24.dp))
            }
        }
    }
}

@Composable
private fun SkeletonRailBlock(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            ShimmerBox(
                modifier = Modifier
                    .height(18.dp)
                    .width(160.dp),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(5) {
                item {
                    Column(modifier = Modifier.width(120.dp)) {
                        ShimmerBox(
                            modifier = Modifier
                                .height(180.dp)
                                .width(120.dp),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }
            }
        }
    }
}