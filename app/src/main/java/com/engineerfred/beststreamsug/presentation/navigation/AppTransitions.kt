package com.engineerfred.beststreamsug.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val NAV_DURATION_MS = 360

fun navEnterTransition(): EnterTransition =
    slideInHorizontally(animationSpec = tween(NAV_DURATION_MS, easing = FastOutSlowInEasing)) { it } +
        fadeIn(tween(NAV_DURATION_MS))

fun navExitTransition(): ExitTransition =
    slideOutHorizontally(animationSpec = tween(NAV_DURATION_MS, easing = FastOutSlowInEasing)) { -it } +
        fadeOut(tween(NAV_DURATION_MS))

fun navPopEnterTransition(): EnterTransition =
    slideInHorizontally(animationSpec = tween(NAV_DURATION_MS, easing = FastOutSlowInEasing)) { -it } +
        fadeIn(tween(NAV_DURATION_MS))

fun navPopExitTransition(): ExitTransition =
    slideOutHorizontally(animationSpec = tween(NAV_DURATION_MS, easing = FastOutSlowInEasing)) { it } +
        fadeOut(tween(NAV_DURATION_MS))