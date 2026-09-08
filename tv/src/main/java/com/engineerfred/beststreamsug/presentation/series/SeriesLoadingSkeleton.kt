package com.engineerfred.beststreamsug.presentation.series

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.presentation.common.skeletons.ShimmerBox

@Composable
fun SeriesLoadingSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        // Screen Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 48.dp, top = 32.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShimmerBox(modifier = Modifier.width(220.dp).height(36.dp), shape = RoundedCornerShape(8.dp))
                ShimmerBox(modifier = Modifier.width(340.dp).height(20.dp), shape = RoundedCornerShape(6.dp))
            }
        }
        // Banner Rail (460x258dp)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 10.dp, bottom = 14.dp),
            ) {
                items((0 until 3).toList()) {
                    ShimmerBox(
                        modifier = Modifier.width(460.dp).height(258.dp),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
        }
        // Series Rails
        items((0 until 3).toList()) {
            SeriesSkeletonRail()
        }
    }
}

@Composable
private fun SeriesSkeletonRail() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ShimmerBox(
            modifier = Modifier.padding(horizontal = 48.dp).width(180.dp).height(26.dp),
            shape = RoundedCornerShape(6.dp),
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 10.dp, bottom = 14.dp),
        ) {
            items((0 until 6).toList()) {
                ShimmerBox(
                    modifier = Modifier.width(160.dp).height(240.dp),
                    shape = RoundedCornerShape(10.dp),
                )
            }
        }
    }
}