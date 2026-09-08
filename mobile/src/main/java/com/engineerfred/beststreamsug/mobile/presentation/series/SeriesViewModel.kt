package com.engineerfred.beststreamsug.mobile.presentation.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.SeriesHomeContent
import com.engineerfred.beststreamsug.domain.usecase.GetSeriesCategoryRailsUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetSeriesCoreContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeriesUiState(
    val isLoading: Boolean = true,
    val content: SeriesHomeContent? = null,
    val isCategoryRailsLoading: Boolean = false,
    val error: AppError? = null,
)

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val getSeriesCoreContent: GetSeriesCoreContentUseCase,
    private val getSeriesCategoryRails: GetSeriesCategoryRailsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SeriesUiState())
    val uiState: StateFlow<SeriesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        _uiState.value = SeriesUiState(isLoading = true)
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val content = getSeriesCoreContent()
                _uiState.value = SeriesUiState(
                    isLoading = false,
                    content = content,
                    isCategoryRailsLoading = true,
                )
                launch {
                    getSeriesCategoryRails.categoryDefinitions.forEach { (id, name) ->
                        launch {
                            val rail = getSeriesCategoryRails.loadRail(id, name)
                            _uiState.update { current ->
                                current.copy(
                                    content = current.content?.copy(
                                        categoryRails = current.content.categoryRails
                                            .filterNot { it.categoryId == rail.categoryId } + rail,
                                    ),
                                )
                            }
                        }
                    }
                    _uiState.update { it.copy(isCategoryRailsLoading = false) }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = SeriesUiState(
                    isLoading = false,
                    error = AppError.Unknown(exception),
                )
            }
        }
    }
}