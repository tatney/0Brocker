package com.homeapp.features.services.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.core.model.matches
import com.homeapp.data.AppContainer
import com.homeapp.data.model.ServiceCategory
import com.homeapp.data.model.ServiceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class MapUiState(
    val searchQuery: String = "",
    val selectedCategory: ServiceCategory = ServiceCategory.ALL,
    val selectedProvider: ServiceProvider? = null,
    val isMapView: Boolean = true,
    val sortBy: SortOption = SortOption.NEAREST,
    val availableOnly: Boolean = true,
)

enum class SortOption(val label: String) {
    RECOMMENDED("Recommended"),
    NEAREST("Nearest"),
    HIGHEST_RATED("Highest Rated"),
    LOWEST_PRICE("Lowest Price"),
    FASTEST("Estimated travel"),
}

class MapViewModel : ViewModel() {

    val userLocation = UserLocation.point

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    val providers: StateFlow<List<ServiceProvider>> =
        combine(
            AppContainer.marketplaceRepository.observeAllProviders(),
            _uiState,
            userLocation,
        ) { allProviders, state, location ->
            nearbyMatches(allProviders,location,state.selectedCategory,state.searchQuery,state.sortBy,com.homeapp.core.time.currentTimeEpochSeconds())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query.take(50)) }
    }

    fun onCategorySelect(category: ServiceCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onProviderSelect(provider: ServiceProvider?) {
        _uiState.update { it.copy(selectedProvider = provider) }
    }

    fun onAvailableChange(value: Boolean) { _uiState.update { it.copy(availableOnly = value) } }

    fun onSortChange(sort: SortOption) {
        _uiState.update { it.copy(sortBy = sort) }
    }

    fun toggleView() {
        _uiState.update { it.copy(isMapView = !it.isMapView) }
    }
}
