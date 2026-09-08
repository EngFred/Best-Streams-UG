package com.engineerfred.beststreamsug.mobile.presentation.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.BrowseSort
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.usecase.GetCategoryContentUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetHomeCoreContentUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetLanguageContentUseCase
import com.engineerfred.beststreamsug.mobile.presentation.navigation.MobileDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CatalogUiState(
    val source: String = "",
    val title: String = "Catalog",
    val filter: String = MobileDestination.Catalog.FILTER_ALL,
    val items: List<ContentSummary> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val error: AppError? = null,
)

private const val CATEGORY_SOURCE = "category"
private const val LANGUAGE_SOURCE = "language"
private const val SECTION_SOURCE = "section"
private val SOURCES = setOf(CATEGORY_SOURCE, LANGUAGE_SOURCE, SECTION_SOURCE)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoryContent: GetCategoryContentUseCase,
    private val getLanguageContent: GetLanguageContentUseCase,
    private val getHomeCoreContent: GetHomeCoreContentUseCase,
) : ViewModel() {
    private val source: String = savedStateHandle[MobileDestination.Catalog.sourceArg] ?: ""
    private val id: Int = savedStateHandle[MobileDestination.Catalog.idArg] ?: 0
    private val title: String = savedStateHandle[MobileDestination.Catalog.titleArg] ?: "Catalog"
    private val filter: String = savedStateHandle[MobileDestination.Catalog.filterArg]
        ?: MobileDestination.Catalog.FILTER_ALL

    private var currentPage = 0
    private var requestInFlight = false
    private var sectionsLoaded = false

    private val _uiState = MutableStateFlow(
        CatalogUiState(source = source, title = title, filter = filter),
    )
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadNextPage()
    }

    fun refresh() {
        currentPage = 0
        sectionsLoaded = false
        _uiState.value = CatalogUiState(source = source, title = title, filter = filter)
        loadNextPage()
    }

    fun loadNextPage() {
        if (requestInFlight) return
        if (source !in SOURCES) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = AppError.InvalidResponse("Invalid catalog route"),
            )
            return
        }
        if (source == SECTION_SOURCE) {
            if (sectionsLoaded) return
            sectionsLoaded = true
            loadSection()
            return
        }
        if (id < 1) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = AppError.InvalidResponse("Invalid catalog route"),
            )
            return
        }
        if (!_uiState.value.isLoading && !_uiState.value.hasMore && _uiState.value.error == null) {
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
                            items = (_uiState.value.items + result.data.items).filter { item ->
                                when (filter) {
                                    MobileDestination.Catalog.FILTER_SERIES -> item.kind == ContentKind.TV_SHOW
                                    MobileDestination.Catalog.FILTER_ALL -> true
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

    private fun loadSection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                when (val result = getHomeCoreContent().sections) {
                    is AppResult.Success -> {
                        val section = result.data.firstOrNull { it.id == id }
                            ?: result.data.firstOrNull { it.title.equals(title, ignoreCase = true) }
                        val items = section?.items.orEmpty().filter { item ->
                            when (filter) {
                                MobileDestination.Catalog.FILTER_SERIES -> item.kind == ContentKind.TV_SHOW
                                MobileDestination.Catalog.FILTER_ALL -> true
                                else -> item.kind == ContentKind.MOVIE
                            }
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            title = section?.title ?: title,
                            items = items,
                            hasMore = false,
                        )
                    }
                    is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.error,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = AppError.Unknown(exception),
                )
            }
        }
    }
}