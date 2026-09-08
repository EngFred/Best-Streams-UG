package com.engineerfred.beststreamsug.mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox

@Composable
fun ShimmerImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onError: (() -> Unit)? = null,
) {
    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            Box(modifier = Modifier.fillMaxSize()) {
                ShimmerBox(modifier = Modifier.fillMaxSize())
            }
        },
        error = {
            // Notify the caller once so it can swap to a fallback (e.g. a
            // solid color tile) instead of leaving this shimmer skeleton
            // showing indefinitely for a genuinely broken/missing image.
            LaunchedEffect(model) {
                onError?.invoke()
            }
            Box(modifier = Modifier.fillMaxSize()) {
                ShimmerBox(modifier = Modifier.fillMaxSize())
            }
        },
        success = {
            SubcomposeAsyncImageContent()
        },
    )
}