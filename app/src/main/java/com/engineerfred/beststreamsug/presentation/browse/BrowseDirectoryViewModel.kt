package com.engineerfred.beststreamsug.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.usecase.GetCategoriesUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetLanguagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BrowseDirectoryViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val getLanguages: GetLanguagesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BrowseDirectoryUiState())
    val uiState: StateFlow<BrowseDirectoryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        _uiState.value = BrowseDirectoryUiState()
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val categoryResult = async { getCategories() }.await()
                val languageResult = async { getLanguages() }.await()
                val categories = (categoryResult as? AppResult.Success)?.data.orEmpty()
                val vjs = (languageResult as? AppResult.Success)?.data
                    .orEmpty()
                    .filter { it.name.contains("vj", ignoreCase = true) }
                val error = listOf(categoryResult, languageResult)
                    .mapNotNull { (it as? AppResult.Failure)?.error }
                    .firstOrNull()
                _uiState.value = BrowseDirectoryUiState(
                    isLoading = false,
                    categories = categories,
                    vjs = vjs,
                    error = if (categories.isEmpty() && vjs.isEmpty()) error else null,
                )
            } catch (exception: kotlinx.coroutines.CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = BrowseDirectoryUiState(
                    isLoading = false,
                    error = AppError.Unknown(exception),
                )
            }
        }
    }
}
