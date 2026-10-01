package com.homeapp.features.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.ServiceProfessional
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ServicesViewModel : ViewModel() {

    private val repo = AppContainer.serviceRepository

    val professionals: StateFlow<List<ServiceProfessional>> =
        repo.observeAllProfessionals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
