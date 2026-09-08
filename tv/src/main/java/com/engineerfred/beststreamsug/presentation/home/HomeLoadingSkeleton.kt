package com.engineerfred.beststreamsug.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.presentation.common.skeletons.ShimmerBox

@Composable
fun HomeLoadingSkeleton() {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 56.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Hero Stage Skeleton (460dp)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp)
                        .background(
                            Brush.verticalGradient(
                                0f to Color(0xFF161A24),
                                0.75f to Color(0xFF10121A),
                                1f to Color.Transparent,
                            ),
                        ),
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 48.dp, end = 48.dp, top = 64.dp)
                            .width(640.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Badges
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ShimmerBox(modifier = Modifier.width(80.dp).height(24.dp), shape = RoundedCornerShape(4.dp))
                            ShimmerBox(modifier = Modifier.width(70.dp).height(24.dp), shape = RoundedCornerShape(4.dp))
                            ShimmerBox(modifier = Modifier.width(50.dp).height(24.dp), shape = RoundedCornerShape(4.dp))
                        }
                        // Title
                        ShimmerBox(modifier = Modifier.width(420.dp).height(38.dp), shape = RoundedCornerShape(8.dp))
                        // Synopsis
                        ShimmerBox(modifier = Modifier.width(520.dp).height(20.dp), shape = RoundedCornerShape(4.dp))
                        ShimmerBox(modifier = Modifier.width(360.dp).height(20.dp), shape = RoundedCornerShape(4.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        // CTAs
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ShimmerBox(modifier = Modifier.width(148.dp).height(48.dp), shape = RoundedCornerShape(8.dp))
                            ShimmerBox(modifier = Modifier.width(132.dp).height(48.dp), shape = RoundedCornerShape(8.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Thumbnail Carousel Selector Strip
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(4) {
                                ShimmerBox(
                                    modifier = Modifier.width(130.dp).height(74.dp),
                                    shape = RoundedCornerShape(8.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Rails
            items((0 until 4).toList()) {
                HomeSkeletonRail()
            }
        }

        // Pinned Top Navigation Bar Skeleton with gradient scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.90f),
                        0.7f to Color.Black.copy(alpha = 0.60f),
                        1f to Color.Transparent,
                    ),
                )
                .padding(bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Brand Logo & Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ShimmerBox(modifier = Modifier.size(38.dp), shape = RoundedCornerShape(8.dp))
                    ShimmerBox(modifier = Modifier.width(140.dp).height(28.dp), shape = RoundedCornerShape(6.dp))
                }
                // Nav Pills
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) {
                        ShimmerBox(modifier = Modifier.width(96.dp).height(40.dp), shape = RoundedCornerShape(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSkeletonRail() {
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