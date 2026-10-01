package com.homeapp.features.services

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.ServiceProfessional
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ServiceDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val serviceId: Long = savedStateHandle.get<Long>("serviceId") ?: 0L

    val professional: StateFlow<ServiceProfessional?> =
        AppContainer.serviceRepository.observeProfessionalById(serviceId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
