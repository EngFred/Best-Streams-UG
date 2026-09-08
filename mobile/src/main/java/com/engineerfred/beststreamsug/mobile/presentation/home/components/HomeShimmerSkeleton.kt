package com.engineerfred.beststreamsug.mobile.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicBackground

@Composable
fun HomeShimmerSkeleton(
    onSearchSelected: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Brand row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "BestStreams",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            val interactionSource = remember { MutableInteractionSource() }
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onSearchSelected,
                    ),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Hero skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF232830),
                            Color(0xFF1B1F26),
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .height(18.dp)
                        .width(140.dp),
                )
                ShimmerBox(
                    modifier = Modifier
                        .height(16.dp)
                        .width(100.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(40.dp)
                            .width(96.dp),
                        shape = CircleShape,
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .height(40.dp)
                            .width(120.dp),
                        shape = CircleShape,
                    )
                }
            }
        }

        // Content section skeletons
        androidx.compose.foundation.lazy.LazyColumn(
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
                Column(modifier = Modifier.width(120.dp)) {
                    ShimmerBox(
                        modifier = Modifier
                            .height(180.dp)
                            .width(120.dp),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .height(12.dp)
                            .width(100.dp),
                    )
                }
            }
        }
    }
}