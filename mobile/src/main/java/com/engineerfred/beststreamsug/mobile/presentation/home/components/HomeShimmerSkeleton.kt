package com.engineerfred.beststreamsug.mobile.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox

@Composable
fun HomeShimmerSkeleton(
    onSearchSelected: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Hero skeleton (full-bleed 16:10, mirrors HomeHeroCarousel)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF232830),
                            Color(0xFF1B1F26),
                        ),
                    ),
                ),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 16.dp)
                    .size(44.dp),
                shape = CircleShape,
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .height(12.dp)
                        .width(70.dp),
                )
                ShimmerBox(
                    modifier = Modifier
                        .height(20.dp)
                        .width(160.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(40.dp)
                            .width(96.dp),
                        shape = RoundedCornerShape(22.dp),
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(40.dp)
                            .width(126.dp),
                        shape = RoundedCornerShape(22.dp),
                    )
                }
            }
        }

        // Content section skeletons
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            repeat(3) {
                item {
                    SkeletonSection(modifier = Modifier.padding(top = 24.dp))
                }
            }
        }
    }
}

@Composable
private fun SkeletonSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            ShimmerBox(
                modifier = Modifier
                    .height(18.dp)
                    .width(160.dp),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyRowSkeleton()
    }
}

@Composable
private fun LazyRowSkeleton() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(6) {
            item {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(2f / 3f),
                ) {
                    ShimmerBox(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
        }
    }
}