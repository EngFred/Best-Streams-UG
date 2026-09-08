package com.engineerfred.beststreamsug.mobile.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engineerfred.beststreamsug.mobile.presentation.navigation.MobileTab
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicPrimary
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicSurface

@Composable
fun CinematicBottomBar(
    currentTab: MobileTab?,
    onTabSelected: (MobileTab) -> Unit,
) {
    val tabs = MobileTab.entries
    val selectedIndex = tabs.indexOfFirst { it == currentTab }
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CinematicSurface)
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.06f)),
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val segmentWidth = maxWidth / tabs.size.toFloat()
            val restOffset by animateDpAsState(
                targetValue = if (selectedIndex >= 0) segmentWidth * selectedIndex else 0.dp,
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                label = "tabRestOffset",
            )

            Row(modifier = Modifier.fillMaxWidth().height(58.dp)) {
                tabs.forEachIndexed { index, tab ->
                    TabItem(
                        tab = tab,
                        selected = index == selectedIndex,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTabSelected(tab)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (selectedIndex >= 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = restOffset)
                        .width(segmentWidth)
                        .height(58.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 9.dp)
                            .width(20.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(1.25.dp))
                            .background(CinematicPrimary),
                    )
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: MobileTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tint = if (selected) CinematicPrimary else CinematicMutedText
    Column(
        modifier = modifier
            .height(58.dp)
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tab.label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}