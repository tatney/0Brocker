package com.homeapp.features.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MarketplaceHubViewModel : ViewModel() {
    val professionals = AppContainer.marketplaceRepository.observeAllProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
