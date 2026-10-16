package com.homeapp.features.property

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.Property
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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

    val error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    fun messageOwner(onOpen: (Long) -> Unit) {
        viewModelScope.launch {
            val p = property.value ?: return@launch
            val userId = (AppContainer.authRepository.observeSession().first() as? com.homeapp.data.model.AuthState.SignedIn)?.user?.id ?: return@launch
            runCatching {AppContainer.chatRepository.openOwnerConversation(p.id,userId,p.ownerName)}.onSuccess(onOpen).onFailure {error.value=it.message}
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch {runCatching {repo.toggleFavorite(propertyId)}.onFailure {error.value=it.message}}
    }
}
