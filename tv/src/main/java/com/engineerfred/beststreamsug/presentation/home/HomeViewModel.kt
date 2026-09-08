package com.engineerfred.beststreamsug.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.usecase.GetHomeCategoryRailsUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetHomeCoreContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeContent: GetHomeCoreContentUseCase,
    private val getHomeCategoryRails: GetHomeCategoryRailsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            isRecentlyAddedLoading = true,
            isCategoryRailsLoading = true,
            error = null,
        )
        loadHome()
    }

    private fun loadHome() {
        viewModelScope.launch {
            try {
                val content = getHomeContent()
                _uiState.value = HomeUiState(
                    isLoading = false,
                    isRecentlyAddedLoading = false,
                    isCategoryRailsLoading = true,
                    content = content,
                )
                launch {
                    val categories = (content.categories as? AppResult.Success)?.data.orEmpty()
                    if (categories.isEmpty()) {
                        _uiState.update { it.copy(isCategoryRailsLoading = false) }
                    } else {
                        categories
                            .sortedBy { it.sortOrder }
                            .take(8)
                            .forEach { category ->
                                launch {
                                    val rail = getHomeCategoryRails(category)
                                    _uiState.update { current ->
                                        val existing = (
                                            current.content?.categoryRails as? AppResult.Success
                                        )?.data.orEmpty()
                                        current.copy(
                                            content = current.content?.copy(
                                                categoryRails = AppResult.Success(
                                                    (existing + rail).sortedBy { it.category.sortOrder },
                                                ),
                                            ),
                                        )
                                    }
                                }
                            }
                        _uiState.update { it.copy(isCategoryRailsLoading = false) }
                    }
                }
            } catch (exception: kotlinx.coroutines.CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    isCategoryRailsLoading = false,
                    error = AppError.Unknown(exception),
                )
            }
        }
    }
}
