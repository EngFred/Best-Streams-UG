package com.engineerfred.beststreamsug.mobile.presentation.browse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox

@Composable
fun BrowseShimmerSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            // Title skeleton
            ShimmerBox(
                modifier = Modifier
                    .height(32.dp)
                    .width(130.dp),
                shape = RoundedCornerShape(6.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search bar skeleton
            ShimmerBox(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Tabs skeleton
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ShimmerBox(
                    modifier = Modifier
                        .height(24.dp)
                        .width(90.dp),
                    shape = RoundedCornerShape(4.dp),
                )
                ShimmerBox(
                    modifier = Modifier
                        .height(24.dp)
                        .width(100.dp),
                    shape = RoundedCornerShape(4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section label skeleton
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            ShimmerBox(
                modifier = Modifier
                    .height(14.dp)
                    .width(70.dp),
                shape = RoundedCornerShape(4.dp),
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(6) {
                item {
                    ShimmerBox(
                        modifier = Modifier
                            .height(86.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
        }
    }
}