package com.engineerfred.beststreamsug.mobile.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentDetails
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.Episode
import com.engineerfred.beststreamsug.domain.model.PlaybackSource
import com.engineerfred.beststreamsug.domain.usecase.GetContentDetailsUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetEpisodesUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetRelatedContentUseCase
import com.engineerfred.beststreamsug.mobile.presentation.navigation.MobileDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailsUiState(
    val isLoading: Boolean = true,
    val details: ContentDetails? = null,
    val error: AppError? = null,
    val selectedSeasonId: Int? = null,
    val episodes: List<Episode> = emptyList(),
    val isLoadingEpisodes: Boolean = false,
    val episodesError: AppError? = null,
    val selectedQuality: PlaybackSource? = null,
    val isDownloadMenuOpen: Boolean = false,
    val relatedContent: List<ContentSummary> = emptyList(),
    val isLoadingRelated: Boolean = false,
    val relatedError: AppError? = null,
)

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getContentDetails: GetContentDetailsUseCase,
    private val getEpisodes: GetEpisodesUseCase,
    private val getRelatedContent: GetRelatedContentUseCase,
) : ViewModel() {
    private val contentId: Int = savedStateHandle[MobileDestination.ContentDetails.contentIdArg] ?: 0
    private val contentKind: ContentKind = savedStateHandle
        .get<String>(MobileDestination.ContentDetails.kindArg)
        ?.let { value -> runCatching { ContentKind.valueOf(value) }.getOrNull() }
        ?: ContentKind.UNKNOWN

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() {
        load()
    }

    fun selectSeason(seasonId: Int) {
        val details = _uiState.value.details ?: return
        if (details.seasons.none { it.id == seasonId }) return
        loadEpisodes(details.summary.id, seasonId)
    }

    fun selectQuality(source: PlaybackSource) {
        _uiState.update { it.copy(selectedQuality = source) }
    }

    fun setDownloadMenuOpen(open: Boolean) {
        _uiState.update { it.copy(isDownloadMenuOpen = open) }
    }

    private fun load() {
        if (contentId < 1 || contentKind == ContentKind.UNKNOWN) {
            _uiState.value = DetailsUiState(
                isLoading = false,
                error = AppError.InvalidResponse("Invalid content details route"),
            )
            return
        }
        _uiState.value = DetailsUiState(isLoading = true)
        viewModelScope.launch {
            try {
                when (
                    val result = getContentDetails(
                        contentId = contentId,
                        typeId = contentKind.apiTypeId(),
                        videoType = contentKind.apiTypeId(),
                    )
                ) {
                    is AppResult.Success -> {
                        val details = result.data
                        val firstSeason = details.seasons.minByOrNull { it.sortOrder }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            details = details,
                            error = null,
                            selectedQuality = details.playback.sources.firstOrNull(),
                        )
                        if (firstSeason != null) {
                            loadEpisodes(details.summary.id, firstSeason.id)
                        }
                        loadRelated()
                    }
                    is AppResult.Failure -> _uiState.value = DetailsUiState(
                        isLoading = false,
                        error = result.error,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = DetailsUiState(
                    isLoading = false,
                    error = AppError.Unknown(exception),
                )
            }
        }
    }

    private fun loadRelated() {
        if (contentId < 1) return
        _uiState.update { it.copy(isLoadingRelated = true, relatedError = null) }
        viewModelScope.launch {
            try {
                when (val result = getRelatedContent(
                    contentId = contentId,
                    typeId = contentKind.apiTypeId(),
                    videoType = contentKind.apiTypeId(),
                    pageNumber = 1,
                )) {
                    is AppResult.Success -> _uiState.update {
                        it.copy(isLoadingRelated = false, relatedContent = result.data.items)
                    }
                    is AppResult.Failure -> _uiState.update {
                        it.copy(isLoadingRelated = false, relatedError = result.error)
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.update {
                    it.copy(isLoadingRelated = false, relatedError = AppError.Unknown(exception))
                }
            }
        }
    }

    private fun loadEpisodes(showId: Int, seasonId: Int) {
        _uiState.update {
            it.copy(
                selectedSeasonId = seasonId,
                isLoadingEpisodes = true,
                episodesError = null,
                episodes = emptyList(),
            )
        }
        viewModelScope.launch {
            try {
                when (val result = getEpisodes(showId, seasonId)) {
                    is AppResult.Success -> _uiState.update {
                        it.copy(isLoadingEpisodes = false, episodes = result.data)
                    }
                    is AppResult.Failure -> _uiState.update {
                        it.copy(isLoadingEpisodes = false, episodesError = result.error)
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.update {
                    it.copy(isLoadingEpisodes = false, episodesError = AppError.Unknown(exception))
                }
            }
        }
    }
}

private fun ContentKind.apiTypeId(): Int = when (this) {
    ContentKind.MOVIE -> 1
    ContentKind.TV_SHOW -> 2
    ContentKind.UNKNOWN -> 0
}