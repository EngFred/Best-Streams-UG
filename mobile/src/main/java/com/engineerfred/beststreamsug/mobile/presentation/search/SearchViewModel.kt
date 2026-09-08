package com.engineerfred.beststreamsug.mobile.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.usecase.SearchContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<ContentSummary> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val error: AppError? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchContentUseCase: SearchContentUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        _uiState
            .onEach { if (it.query.isBlank()) _uiState.update { state -> state.copy(results = emptyList(), isLoading = false, error = null, hasSearched = false) } }
            .debounce(450L)
            .onEach { state ->
                if (state.query.isNotBlank()) {
                    performSearch(state.query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun clearQuery() {
        _uiState.update { it.copy(query = "") }
    }

    fun retry() {
        performSearch(_uiState.value.query)
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, hasSearched = true) }
            when (val result = searchContentUseCase(query)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(results = result.data.items, isLoading = false)
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(error = result.error, isLoading = false) }
                }
            }
        }
    }
}