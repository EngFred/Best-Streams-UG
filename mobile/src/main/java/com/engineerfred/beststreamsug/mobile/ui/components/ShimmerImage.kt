package com.engineerfred.beststreamsug.mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
            Box(modifier = Modifier.fillMaxSize()) {
                ShimmerBox(modifier = Modifier.fillMaxSize())
            }
        },
        success = {
            SubcomposeAsyncImageContent()
        },
    )
}
