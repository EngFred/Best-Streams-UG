package com.engineerfred.beststreamsug.presentation.navigation

import android.net.Uri
import com.engineerfred.beststreamsug.domain.model.ContentKind

sealed interface AppDestination {
    val route: String

    data object Home : AppDestination {
        override val route: String = "home"
    }

    data object BrowseDirectory : AppDestination {
        override val route: String = "browse"
    }

    data object Series : AppDestination {
        override val route: String = "series"
    }

    data object Search : AppDestination {
        override val route: String = "search"
    }

    data object Catalog : AppDestination {
        const val sourceArgument = "source"
        const val idArgument = "id"
        const val titleArgument = "title"
        const val filterArgument = "filter"
        override val route: String =
            "catalog/{$sourceArgument}/{$idArgument}/{$titleArgument}?$filterArgument={$filterArgument}"

        fun createRoute(
            source: String,
            id: Int,
            title: String,
            filter: String = FILTER_MOVIES,
        ): String = "catalog/$source/$id/${Uri.encode(title)}?$filterArgument=$filter"

        const val FILTER_MOVIES = "movies"
        const val FILTER_SERIES = "series"
        const val FILTER_ALL = "all"
    }

    data object SectionGrid : AppDestination {
        const val screenArgument = "screen"
        const val titleArgument = "titleArgument"
        override val route: String = "sectionGrid/{$screenArgument}/{$titleArgument}"

        const val SCREEN_HOME = 0
        const val SCREEN_SERIES = 1

        fun createRoute(screen: Int, title: String): String =
            "sectionGrid/$screen/${Uri.encode(title)}"
    }

    data object ContentDetails : AppDestination {
        const val contentIdArgument = "contentId"
        const val contentKindArgument = "contentKind"
        override val route: String = "details/{$contentIdArgument}/{$contentKindArgument}"

        fun createRoute(contentId: Int, kind: ContentKind): String =
            "details/$contentId/${kind.name}"
    }

    data object Player : AppDestination {
        const val urlArgument = "url"
        const val titleArgument = "title"
        const val metaArgument = "meta"
        const val posterArgument = "poster"
        override val route: String =
            "player/{$urlArgument}?$titleArgument={$titleArgument}&$metaArgument={$metaArgument}&$posterArgument={$posterArgument}"

        fun createRoute(
            url: String,
            title: String? = null,
            meta: String? = null,
            poster: String? = null,
        ): String =
            "player/${Uri.encode(url)}" +
                "?$titleArgument=${Uri.encode(title ?: "")}" +
                "&$metaArgument=${Uri.encode(meta ?: "")}" +
                "&$posterArgument=${Uri.encode(poster ?: "")}"
    }
}
