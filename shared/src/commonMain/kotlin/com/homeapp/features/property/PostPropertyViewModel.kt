package com.homeapp.features.property

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.core.icons.IconBriefcase
import com.homeapp.core.icons.IconClock
import com.homeapp.core.icons.IconShieldCheck
import com.homeapp.core.icons.IconTree
import com.homeapp.data.AppContainer
import com.homeapp.data.model.Property
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ListingTypeOption(
    val label: String,
    val type: String,
    val icon: ImageVector,
    val monthly: Boolean,
) {
    RENT("Rent", "RENT", IconClock, true),
    BUY("Buy", "BUY", IconShieldCheck, false),
    PG("Shared home", "RENT", IconBriefcase, true),
    PLOT("Land", "BUY", IconTree, false),
}

val bedroomOptions = listOf("1 RK", "1 BHK", "2 BHK", "3 BHK", "4+ BHK")

data class PostPropertyUiState(
    val title: String = "",
    val location: String = "",
    val city: String = "Kampala",
    val listingType: ListingTypeOption = ListingTypeOption.RENT,
    val priceText: String = "",
    val bedrooms: String = "2 BHK",
    val ownerDeclared: Boolean = false,
    val isBusy: Boolean = false,
    val error: String? = null,
    val isDone: Boolean = false,
) {
    val canSubmit: Boolean
        get() = title.isNotBlank() && location.isNotBlank() &&
            ownerDeclared && city.isNotBlank() && priceText.filter { it.isDigit() }.isNotEmpty() && !isBusy
}

class PostPropertyViewModel : ViewModel() {

    private val repo = AppContainer.propertyRepository

    private val _ui = MutableStateFlow(PostPropertyUiState())
    val ui: StateFlow<PostPropertyUiState> = _ui.asStateFlow()

    fun onTitleChange(v: String) = _ui.update { it.copy(title = v.take(60), error = null) }
    fun onLocationChange(v: String) = _ui.update { it.copy(location = v.take(60), error = null) }
    fun onCityChange(v: String) = _ui.update { it.copy(city = v.take(30), error = null) }
    fun onPriceChange(v: String) = _ui.update { it.copy(priceText = v.filter { it.isDigit() }.take(13), error = null) }
    fun onBedroomsChange(v: String) = _ui.update { it.copy(bedrooms = v) }
    fun onListingTypeChange(v: ListingTypeOption) = _ui.update { it.copy(listingType = v) }

    fun onOwnerDeclared(value: Boolean) = _ui.update { it.copy(ownerDeclared = value) }

    fun postProperty() {
        val s = _ui.value
        val amount = s.priceText.filter { it.isDigit() }.toLongOrNull() ?: 0L
        if (!s.ownerDeclared || s.city.isBlank() || s.title.isBlank() || s.location.isBlank() || amount <= 0) {
            _ui.update { it.copy(error = "Confirm you own this property and complete all required fields") }
            return
        }
        _ui.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            val id = com.homeapp.data.database.DatabaseProvider.database.homeAppDatabaseQueries.propertyMaxId().executeAsOne() + 1
            val priceUgx = amount.toLong()
            val priceLabel = if (s.listingType.monthly) "/mo" else ""
            val owner = (AppContainer.authRepository.observeSession().first() as? com.homeapp.data.model.AuthState.SignedIn)?.user
                ?: run { _ui.update { it.copy(isBusy = false, error = "Sign in to post a listing") }; return@launch }
            repo.insert(
                Property(
                    id = id,
                    title = s.title,
                    location = s.location,
                    city = s.city,
                    listingType = s.listingType.type,
                    // A plot is land; everything else is a house. This used to be
                    // hardcoded to HOUSE, so a posted Plot rendered with a house
                    // glyph and filtered out of the Land row.
                    assetType = if (s.listingType == ListingTypeOption.PLOT) "LAND" else "HOUSE",
                    category = "RESIDENTIAL",
                    priceUgx = priceUgx,
                    priceLabel = priceLabel,
                    rating = 0.0,
                    isVerified = false,
                    bedrooms = s.bedrooms,
                    // Legacy seed column. The UI resolves the asset icon from
                    // [assetType] now, so mirror that rather than storing a glyph.
                    emoji = s.listingType.type,
                    ownerId = owner.id,
                    ownerName = owner.fullName,
                    ownerDeclared = true,
                ),
            )
            _ui.update { it.copy(isBusy = false, isDone = true) }
        }
    }

    fun dismissError() = _ui.update { it.copy(error = null) }
}
