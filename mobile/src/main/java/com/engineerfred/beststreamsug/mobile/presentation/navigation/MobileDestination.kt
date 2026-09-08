package com.engineerfred.beststreamsug.mobile.presentation.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import com.engineerfred.beststreamsug.domain.model.ContentKind

sealed interface MobileDestination {
    val route: String

    data object Home : MobileDestination {
        override val route: String = "home"
    }

    data object Series : MobileDestination {
        override val route: String = "series"
    }

    data object Browse : MobileDestination {
        override val route: String = "browse"
    }

    data object Search : MobileDestination {
        override val route: String = "search"
    }

    data object Catalog : MobileDestination {
        const val sourceArg = "source"
        const val idArg = "id"
        const val titleArg = "title"
        const val filterArg = "filter"
        override val route: String =
            "catalog/{$sourceArg}/{$idArg}/{$titleArg}?$filterArg={$filterArg}"

        const val FILTER_MOVIES = "movies"
        const val FILTER_SERIES = "series"
        const val FILTER_ALL = "all"

        fun createRoute(
            source: String,
            id: Int,
            title: String,
            filter: String = FILTER_ALL,
        ): String = "catalog/$source/$id/${Uri.encode(title)}?$filterArg=$filter"
    }

    data object ContentDetails : MobileDestination {
        const val contentIdArg = "contentId"
        const val kindArg = "kind"
        override val route: String = "details/{$contentIdArg}/{$kindArg}"

        fun createRoute(contentId: Int, kind: ContentKind): String =
            "details/$contentId/${kind.name}"
    }

    data object Player : MobileDestination {
        const val urlArg = "url"
        const val titleArg = "title"
        const val metaArg = "meta"
        const val posterArg = "poster"
        override val route: String =
            "player/{$urlArg}?$titleArg={$titleArg}&$metaArg={$metaArg}&$posterArg={$posterArg}"

        fun createRoute(
            url: String,
            title: String? = null,
            meta: String? = null,
            poster: String? = null,
        ): String = buildString {
            append("player/")
            append(Uri.encode(url))
            append("?$titleArg=")
            append(Uri.encode(title.orEmpty()))
            append("&$metaArg=")
            append(Uri.encode(meta.orEmpty()))
            append("&$posterArg=")
            append(Uri.encode(poster.orEmpty()))
        }
    }
}

enum class MobileTab(
    val label: String,
    val icon: ImageVector,
    val destination: MobileDestination,
) {
    Home("Home", Icons.Rounded.Home, MobileDestination.Home),
    Series("Series", Icons.Rounded.LiveTv, MobileDestination.Series),
    Browse("Browse", Icons.Rounded.VideoLibrary, MobileDestination.Browse),
    Search("Search", Icons.Rounded.Search, MobileDestination.Search),
}

fun String.asContentKind(): ContentKind? =
    runCatching { ContentKind.valueOf(this) }.getOrNull()