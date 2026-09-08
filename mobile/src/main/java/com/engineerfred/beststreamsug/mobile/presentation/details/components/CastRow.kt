package com.engineerfred.beststreamsug.mobile.presentation.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.engineerfred.beststreamsug.domain.model.CastMember
import com.engineerfred.beststreamsug.mobile.ui.components.ShimmerImage
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.util.toSafeHttpsUrl

@Composable
fun CastRow(
    cast: List<CastMember>,
    modifier: Modifier = Modifier,
) {
    val horizontalPadding = PaddingValues(horizontal = 20.dp)
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = horizontalPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = cast,
            key = { "cast_${it.id}" },
        ) { member ->
            Column(
                modifier = Modifier.width(76.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1E24)),
                ) {
                    member.imageUrl?.let { url ->
                        ShimmerImage(
                            model = url.toSafeHttpsUrl(),
                            contentDescription = member.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
                member.roleType?.let { role ->
                    if (role.isNotBlank()) {
                        Text(
                            text = role,
                            style = MaterialTheme.typography.labelSmall,
                            color = CinematicMutedText,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}