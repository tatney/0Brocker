package com.homeapp.features.property

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.Property
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Holds a single property loaded by id for the detail screen. */
class PropertyDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val propertyId: Long = savedStateHandle.get<Long>("propertyId") ?: 0L
    private val repo = AppContainer.propertyRepository

    val property: StateFlow<Property?> =
        repo.observeById(propertyId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSaved: StateFlow<Boolean> =
        repo.isFavorite(propertyId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onToggleFavorite() {
        viewModelScope.launch { repo.toggleFavorite(propertyId) }
    }
}
