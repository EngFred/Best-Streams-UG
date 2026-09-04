package com.engineerfred.beststreamsug.presentation.details

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.presentation.common.skeletons.ShimmerBox

@Composable
fun DetailsLoadingSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 56.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        // Hero Stage (460dp)
        item {
            DetailsHeroSkeleton()
        }
        // Stat Pills
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 6.dp, bottom = 6.dp),
            ) {
                items((0 until 5).toList()) {
                    ShimmerBox(
                        modifier = Modifier.width(110.dp).height(36.dp),
                        shape = RoundedCornerShape(20.dp),
                    )
                }
            }
        }
        // Cast & Crew Rail
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier.padding(horizontal = 48.dp).width(140.dp).height(26.dp),
                    shape = RoundedCornerShape(6.dp),
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 10.dp, bottom = 14.dp),
                ) {
                    items((0 until 6).toList()) {
                        Column(
                            modifier = Modifier.width(116.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ShimmerBox(
                                modifier = Modifier.size(104.dp),
                                shape = CircleShape,
                            )
                            ShimmerBox(modifier = Modifier.width(90.dp).height(16.dp), shape = RoundedCornerShape(4.dp))
                            ShimmerBox(modifier = Modifier.width(64.dp).height(12.dp), shape = RoundedCornerShape(4.dp))
                        }
                    }
                }
            }
        }
        // Related / Recommendation Rail
        item {
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
    }
}

@Composable
private fun DetailsHeroSkeleton() {
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
                .padding(start = 48.dp, end = 48.dp, top = 24.dp)
                .width(660.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Badges row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShimmerBox(modifier = Modifier.width(80.dp).height(24.dp), shape = RoundedCornerShape(4.dp))
                ShimmerBox(modifier = Modifier.width(70.dp).height(24.dp), shape = RoundedCornerShape(4.dp))
                ShimmerBox(modifier = Modifier.width(50.dp).height(24.dp), shape = RoundedCornerShape(4.dp))
            }
            // Title
            ShimmerBox(modifier = Modifier.width(440.dp).height(40.dp), shape = RoundedCornerShape(8.dp))
            // Genres
            ShimmerBox(modifier = Modifier.width(220.dp).height(18.dp), shape = RoundedCornerShape(4.dp))
            // Synopsis
            ShimmerBox(modifier = Modifier.width(540.dp).height(18.dp), shape = RoundedCornerShape(4.dp))
            ShimmerBox(modifier = Modifier.width(380.dp).height(18.dp), shape = RoundedCornerShape(4.dp))
            Spacer(modifier = Modifier.height(2.dp))
            // CTAs
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ShimmerBox(modifier = Modifier.width(150.dp).height(48.dp), shape = RoundedCornerShape(8.dp))
                ShimmerBox(modifier = Modifier.width(130.dp).height(48.dp), shape = RoundedCornerShape(8.dp))
            }
        }
    }
}