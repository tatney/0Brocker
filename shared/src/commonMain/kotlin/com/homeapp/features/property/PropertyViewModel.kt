package com.homeapp.features.property

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.Property
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class PropertyFilter(
    val listingType: String? = null,   // RENT | BUY
    val assetType: String? = null,     // HOUSE | LAND | CAR
    val category: String? = null,
    val maxPrice: Long? = null,
    val verifiedOnly: Boolean = false,
    val sort: String = "RECOMMENDED",      // RESIDENTIAL | COMMERCIAL
)

class PropertyViewModel : ViewModel() {

    private val repo = AppContainer.propertyRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filter = MutableStateFlow(PropertyFilter())
    val filter: StateFlow<PropertyFilter> = _filter

    val properties: StateFlow<List<Property>> =
        combine(repo.observeAll(), _searchQuery, _filter) { rows, query, filter ->
            filterProperties(rows, query, filter)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onBudgetChange(value: String) { _filter.value = _filter.value.copy(maxPrice = value.toLongOrNull()) }
    fun onVerifiedChange(value: Boolean) { _filter.value = _filter.value.copy(verifiedOnly = value) }
    fun onSortChange(value: String) { _filter.value = _filter.value.copy(sort = value) }

    fun applySearch(query: String, filter: PropertyFilter) { _searchQuery.value = query; _filter.value = filter }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onListingTypeChange(type: String?) {
        _filter.value = _filter.value.copy(listingType = toggleCurrent(_filter.value.listingType, type))
    }

    fun onAssetTypeChange(type: String?) {
        _filter.value = _filter.value.copy(assetType = toggleCurrent(_filter.value.assetType, type))
    }

    fun onCategoryChange(category: String?) {
        _filter.value = _filter.value.copy(category = toggleCurrent(_filter.value.category, category))
    }

    fun clearFilters() {
        _filter.value = PropertyFilter()
    }

    private fun toggleCurrent(current: String?, new: String?): String? =
        if (current == new) null else new
}