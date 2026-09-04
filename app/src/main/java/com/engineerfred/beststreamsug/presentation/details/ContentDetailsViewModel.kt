package com.engineerfred.beststreamsug.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.usecase.GetContentDetailsUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetEpisodesUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetRelatedContentUseCase
import com.engineerfred.beststreamsug.presentation.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ContentDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getContentDetails: GetContentDetailsUseCase,
    private val getEpisodes: GetEpisodesUseCase,
    private val getRelatedContent: GetRelatedContentUseCase,
) : ViewModel() {
    private val contentId: Int? = savedStateHandle[AppDestination.ContentDetails.contentIdArgument]
    private val contentKind: ContentKind? = savedStateHandle
        .get<String>(AppDestination.ContentDetails.contentKindArgument)
        ?.let { value -> runCatching { ContentKind.valueOf(value) }.getOrNull() }

    private val _uiState = MutableStateFlow(ContentDetailsUiState())
    val uiState: StateFlow<ContentDetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    fun refresh() {
        loadDetails()
    }

    fun selectSeason(seasonId: Int) {
        val details = _uiState.value.details ?: return
        if (details.seasons.none { it.id == seasonId }) return
        loadEpisodes(details.summary.id, seasonId)
    }

    private fun loadDetails() {
        val id = contentId
        val kind = contentKind
        if (id == null || id < 1 || kind == null || kind == ContentKind.UNKNOWN) {
            _uiState.value = ContentDetailsUiState(
                isLoading = false,
                error = AppError.InvalidResponse("Invalid content details route"),
            )
            return
        }
        viewModelScope.launch {
            try {
                when (
                    val result = getContentDetails(
                        contentId = id,
                        typeId = kind.apiTypeId(),
                        videoType = kind.apiTypeId(),
                    )
                ) {
                    is AppResult.Success -> {
                        val firstSeason = result.data.seasons.minByOrNull { it.sortOrder }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            details = result.data,
                            error = null,
                        )
                        if (firstSeason != null) {
                            loadEpisodes(result.data.summary.id, firstSeason.id)
                        }
                        loadRelatedContent(id)
                    }
                    is AppResult.Failure -> {
                        _uiState.value = ContentDetailsUiState(
                            isLoading = false,
                            error = result.error,
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = ContentDetailsUiState(
                    isLoading = false,
                    error = AppError.Unknown(exception),
                )
            }
        }
    }

    private fun loadRelatedContent(contentId: Int) {
        val kind = contentKind ?: return
        _uiState.value = _uiState.value.copy(
            isLoadingRelated = true,
            relatedError = null,
        )
        viewModelScope.launch {
            try {
                when (val result = getRelatedContent(
                    contentId = contentId,
                    typeId = kind.apiTypeId(),
                    videoType = kind.apiTypeId(),
                    pageNumber = 1,
                )) {
                    is AppResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoadingRelated = false,
                        relatedContent = result.data.items,
                    )
                    is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                        isLoadingRelated = false,
                        relatedError = result.error,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoadingRelated = false,
                    relatedError = AppError.Unknown(exception),
                )
            }
        }
    }

    private fun loadEpisodes(showId: Int, seasonId: Int) {
        _uiState.value = _uiState.value.copy(
            selectedSeasonId = seasonId,
            isLoadingEpisodes = true,
            episodesError = null,
            episodes = emptyList(),
        )
        viewModelScope.launch {
            try {
                when (val result = getEpisodes(showId, seasonId)) {
                    is AppResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoadingEpisodes = false,
                        episodes = result.data,
                    )
                    is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                        isLoadingEpisodes = false,
                        episodesError = result.error,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoadingEpisodes = false,
                    episodesError = AppError.Unknown(exception),
                )
            }
        }
    }
}

private fun ContentKind.apiTypeId(): Int = when (this) {
    ContentKind.MOVIE -> 1
    ContentKind.TV_SHOW -> 2
    ContentKind.UNKNOWN -> 0
}
