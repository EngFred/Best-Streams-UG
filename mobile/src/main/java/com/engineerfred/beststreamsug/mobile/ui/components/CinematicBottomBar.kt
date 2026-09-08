package com.engineerfred.beststreamsug.mobile.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.mobile.presentation.navigation.MobileTab
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicPrimary

@Composable
fun CinematicBottomBar(
    currentTab: MobileTab?,
    onTabSelected: (MobileTab) -> Unit,
) {
    val tabs = MobileTab.entries
    val selectedIndex = tabs.indexOfFirst { it == currentTab }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val segmentWidth = maxWidth / tabs.size.toFloat()
            val animatedOffset by animateDpAsState(
                targetValue = if (selectedIndex >= 0) segmentWidth * selectedIndex else 0.dp,
                animationSpec = tween(durationMillis = 380),
                label = "dockIndicator",
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            0f to Color(0xFF24272E),
                            1f to Color(0xFF0F1114),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(32.dp),
                    ),
            ) {}

            if (selectedIndex >= 0) {
                Box(
                    modifier = Modifier
                        .offset(x = animatedOffset)
                        .width(segmentWidth)
                        .height(64.dp)
                        .padding(horizontal = 5.dp, vertical = 5.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(27.dp))
                            .background(
                                Brush.verticalGradient(
                                    0f to CinematicPrimary.copy(alpha = 0.22f),
                                    1f to CinematicPrimary.copy(alpha = 0.05f),
                                ),
                            )
                            .border(
                                width = 1.dp,
                                color = CinematicPrimary.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(27.dp),
                            ),
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { index, tab ->
                    DockItem(
                        tab = tab,
                        selected = index == selectedIndex,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DockItem(
    tab: MobileTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tint = if (selected) CinematicPrimary else CinematicMutedText
    Column(
        modifier = modifier
            .height(64.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = tab.label,
            color = tint,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        )
    }
}