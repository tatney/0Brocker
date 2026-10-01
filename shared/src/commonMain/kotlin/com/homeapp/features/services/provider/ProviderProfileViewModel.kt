package com.homeapp.features.services.provider

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.ServiceProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProviderProfileViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val providerId: Long = savedStateHandle["providerId"] ?: 0L

    val provider: StateFlow<ServiceProvider?> =
        AppContainer.marketplaceRepository.observeAllProviders()
            .map { providers -> providers.firstOrNull { it.id == providerId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
