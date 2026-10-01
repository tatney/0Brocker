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
    val sortBy: SortOption = SortOption.RECOMMENDED,
    val availableOnly: Boolean = false,
)

enum class SortOption(val label: String) {
    RECOMMENDED("Recommended"),
    NEAREST("Nearest"),
    HIGHEST_RATED("Highest Rated"),
    LOWEST_PRICE("Lowest Price"),
    FASTEST("Estimated travel"),
}

class MapViewModel : ViewModel() {

    // User's assumed reference location (Kampala city centre)
    private val userLat = 0.3476
    private val userLng = 32.5825

    /**
     * The user's own position, so the map can show it and frame the results
     * around it. Previously the map canvas accepted a `userLocation` that
     * nothing ever supplied, so the location dot was unreachable.
     */
    val userLocation: GeoPoint = GeoPoint(userLat, userLng)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    val providers: StateFlow<List<ServiceProvider>> =
        combine(
            AppContainer.marketplaceRepository.observeAllProviders(),
            _uiState,
        ) { allProviders, state ->
            val enriched = allProviders.map { it.withDistanceFrom(userLat, userLng) }
            val filtered = enriched.filter { provider ->
                // Alias-based, not label equality: the database writes
                // "Interior Design" / "Appliance Repair" where the enum says
                // "Interior" / "Appliance", so an equality check silently
                // returned zero providers for those categories.
                val matchesCategory = state.selectedCategory.matches(provider.category)
                val matchesSearch = state.searchQuery.isBlank() ||
                    provider.name.contains(state.searchQuery, ignoreCase = true) ||
                    provider.category.contains(state.searchQuery, ignoreCase = true) ||
                    provider.headline.contains(state.searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch && (!state.availableOnly || provider.status == "AVAILABLE")
            }
            when (state.sortBy) {
                SortOption.NEAREST -> filtered.sortedBy { it.distanceKm }
                SortOption.HIGHEST_RATED -> filtered.sortedByDescending { it.rating }
                SortOption.LOWEST_PRICE -> filtered.sortedBy { it.priceFrom }
                SortOption.FASTEST -> filtered.sortedBy { it.etaMinutes }
                SortOption.RECOMMENDED -> filtered.sortedByDescending { it.rating * it.jobsCompleted }
            }
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

private fun ServiceProvider.withDistanceFrom(userLat: Double, userLng: Double): ServiceProvider {
    val distanceKm = haversineKm(GeoPoint(userLat, userLng), GeoPoint(this.lat, this.lng))
    val etaMinutes = (distanceKm / 30.0 * 60.0).toInt().coerceAtLeast(5)
    return copy(distanceKm = distanceKm, etaMinutes = etaMinutes)
}
