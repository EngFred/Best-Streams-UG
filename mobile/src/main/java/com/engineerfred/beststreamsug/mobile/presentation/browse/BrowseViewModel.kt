package com.engineerfred.beststreamsug.mobile.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.Language
import com.engineerfred.beststreamsug.domain.usecase.GetCategoriesUseCase
import com.engineerfred.beststreamsug.domain.usecase.GetLanguagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BrowseUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val languages: List<Language> = emptyList(),
    val error: AppError? = null,
)

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val getLanguages: GetLanguagesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        _uiState.value = BrowseUiState(isLoading = true)
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val categoryResult = async { getCategories() }.await()
                val languageResult = async { getLanguages() }.await()
                val categories = (categoryResult as? AppResult.Success)?.data.orEmpty()
                val languages = (languageResult as? AppResult.Success)?.data.orEmpty()
                val error = listOf(categoryResult, languageResult)
                    .mapNotNull { (it as? AppResult.Failure)?.error }
                    .firstOrNull()
                _uiState.value = BrowseUiState(
                    isLoading = false,
                    categories = categories,
                    languages = languages,
                    error = if (categories.isEmpty() && languages.isEmpty()) error else null,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = BrowseUiState(
                    isLoading = false,
                    error = AppError.Unknown(exception),
                )
            }
        }
    }
}