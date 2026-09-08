package com.engineerfred.beststreamsug.mobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.HomeContent
import com.engineerfred.beststreamsug.domain.usecase.GetHomeCategoryRailsUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetHomeCoreContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val content: HomeContent? = null,
    val isCategoryRailsLoading: Boolean = false,
    val totalCategoryCount: Int = 0,
    val error: AppError? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeContent: GetHomeCoreContentUseCase,
    private val getHomeCategoryRails: GetHomeCategoryRailsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        _uiState.value = HomeUiState(isLoading = true)
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val content = getHomeContent()
                val categories = (content.categories as? AppResult.Success)?.data.orEmpty()
                val targetCategories = categories.sortedBy { it.sortOrder }.take(8)

                _uiState.value = HomeUiState(
                    isLoading = false,
                    content = content,
                    isCategoryRailsLoading = targetCategories.isNotEmpty(),
                    totalCategoryCount = targetCategories.size,
                )

                if (targetCategories.isNotEmpty()) {
                    launch {
                        coroutineScope {
                            targetCategories.forEach { category ->
                                launch {
                                    val rail = getHomeCategoryRails(category)
                                    _uiState.update { current ->
                                        val existing = (
                                            current.content?.categoryRails as? AppResult.Success
                                        )?.data.orEmpty()
                                        val updated = (existing.filterNot { it.category.id == rail.category.id } + rail)
                                            .sortedBy { it.category.sortOrder }
                                        current.copy(
                                            content = current.content?.copy(
                                                categoryRails = AppResult.Success(updated),
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                        _uiState.update { it.copy(isCategoryRailsLoading = false) }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = AppError.Unknown(exception),
                )
            }
        }
    }
}