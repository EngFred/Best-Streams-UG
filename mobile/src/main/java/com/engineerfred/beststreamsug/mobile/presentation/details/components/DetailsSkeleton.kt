package com.engineerfred.beststreamsug.mobile.presentation.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox

@Composable
fun DetailsShimmerSkeleton(
    onBack: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape,
                )
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onBack,
                            )
                            .padding(12.dp),
                    )
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .height(28.dp)
                        .fillMaxWidth(0.8f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(3) {
                        ShimmerBox(
                            modifier = Modifier
                                .height(24.dp)
                                .width(70.dp),
                            shape = RoundedCornerShape(6.dp),
                        )
                    }
                }
                ShimmerBox(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.5f),
                )
                ShimmerBox(
                    modifier = Modifier
                        .height(52.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                )
                ShimmerBox(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                )
            }
        }
        repeat(3) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                ) {
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
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(180.dp),
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
            }
        }
    }
}