package com.homeapp.features.property

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.Property
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Backs the "Saved homes" screen, observing all favorited properties. */
class SavedHomesViewModel : ViewModel() {

    val saved: StateFlow<List<Property>> =
        AppContainer.propertyRepository.observeFavoriteProperties()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
