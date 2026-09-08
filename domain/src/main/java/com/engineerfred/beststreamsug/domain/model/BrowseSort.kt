package com.engineerfred.beststreamsug.domain.model

sealed interface BrowseSort {
    data object Newest : BrowseSort

    data object MostViewed : BrowseSort

    data object MostLiked : BrowseSort
}
