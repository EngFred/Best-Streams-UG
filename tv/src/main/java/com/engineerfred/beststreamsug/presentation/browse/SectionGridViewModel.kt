package com.engineerfred.beststreamsug.presentation.browse

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.repository.SectionRepository
import com.engineerfred.beststreamsug.presentation.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SectionGridViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sectionRepository: SectionRepository,
) : ViewModel() {
    private val screen: Int = savedStateHandle[AppDestination.SectionGrid.screenArgument] ?: 0
    private val title: String = savedStateHandle[AppDestination.SectionGrid.titleArgument] ?: ""
    private val seriesOnly = screen == AppDestination.SectionGrid.SCREEN_SERIES

    private val _uiState = MutableStateFlow(SectionGridUiState(title = title))
    val uiState: StateFlow<SectionGridUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        if (title.isBlank()) {
            _uiState.value = SectionGridUiState(
                title = title,
                isLoading = false,
                error = AppError.InvalidResponse("Invalid section"),
            )
            return
        }
        viewModelScope.launch {
            try {
                when (val result = sectionRepository.getSections(isHomeScreen = 1, typeId = 1)) {
                    is AppResult.Success -> {
                        val section = result.data.firstOrNull { it.title.equals(title, ignoreCase = true) }
                            ?: result.data.firstOrNull { it.title.contains(title, ignoreCase = true) }
                        val items = section?.items.orEmpty()
                            .filter { if (seriesOnly) it.kind == ContentKind.TV_SHOW else true }
                        _uiState.value = SectionGridUiState(
                            title = section?.title ?: title,
                            subtitle = if (seriesOnly) "Only series" else "",
                            items = items,
                            isLoading = false,
                        )
                    }
                    is AppResult.Failure -> _uiState.value = SectionGridUiState(
                        title = title,
                        isLoading = false,
                        error = result.error,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                _uiState.value = SectionGridUiState(
                    title = title,
                    isLoading = false,
                    error = AppError.Unknown(exception),
                )
            }
        }
    }
}