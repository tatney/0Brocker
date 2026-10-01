package com.homeapp.features.services

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.ServiceProfessional
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ServicesCategoryViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val category: String = savedStateHandle.get<String>("category") ?: "Plumber"

    val professionals: StateFlow<List<ServiceProfessional>> =
        AppContainer.serviceRepository.observeByCategory(category)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
