package com.engineerfred.beststreamsug.presentation.browse

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.BrowseSort
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.usecase.GetCategoryContentUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetLanguageContentUseCase
import com.engineerfred.beststreamsug.presentation.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CatalogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoryContent: GetCategoryContentUseCase,
    private val getLanguageContent: GetLanguageContentUseCase,
) : ViewModel() {
    private val source: String = savedStateHandle[AppDestination.Catalog.sourceArgument] ?: ""
    private val id: Int = savedStateHandle[AppDestination.Catalog.idArgument] ?: 0
    private val title: String = savedStateHandle[AppDestination.Catalog.titleArgument] ?: "Catalog"
    private val filter: String = savedStateHandle[AppDestination.Catalog.filterArgument]
        ?: AppDestination.Catalog.FILTER_MOVIES
    private var currentPage = 0
    private var requestInFlight = false

    private val _uiState = MutableStateFlow(
        CatalogUiState(title = title, source = source, filter = filter),
    )
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (
            requestInFlight ||
            (!_uiState.value.isLoading && !_uiState.value.hasMore && _uiState.value.error == null)
        ) return
        if (id < 1 || source !in SOURCES) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = AppError.InvalidResponse("Invalid catalog route"),
            )
            return
        }
        requestInFlight = true
        val page = currentPage + 1
        _uiState.value = _uiState.value.copy(
            isLoading = page == 1,
            isLoadingMore = page > 1,
            error = null,
        )
        viewModelScope.launch {
            try {
                val result = when (source) {
                    CATEGORY_SOURCE -> getCategoryContent(id, page, BrowseSort.Newest)
                    else -> getLanguageContent(id, page, BrowseSort.Newest)
                }
                when (result) {
                    is AppResult.Success -> {
                        currentPage = result.data.currentPage
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            items = _uiState.value.items + result.data.items.filter { item ->
                                when (filter) {
                                    AppDestination.Catalog.FILTER_SERIES -> item.kind == ContentKind.TV_SHOW
                                    AppDestination.Catalog.FILTER_ALL -> true
                                    else -> item.kind == ContentKind.MOVIE
                                }
                            },
                            hasMore = result.data.hasMore,
                        )
                    }
                    is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = result.error,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = AppError.Unknown(exception),
                )
            } finally {
                requestInFlight = false
            }
        }
    }

    private companion object {
        const val CATEGORY_SOURCE = "category"
        val SOURCES = setOf(CATEGORY_SOURCE, "vj")
    }
}
