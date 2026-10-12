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
    val companyRepresentative: Boolean = false,
    val companyIdentifier: String = "",
    val titleNumber: String = "",
    val unitNumber: String = "",
    val jurisdiction: String = "",
    val isBusy: Boolean = false,
    val error: String? = null,
    val isDone: Boolean = false,
) {
    val canSubmit: Boolean
        get() = title.isNotBlank() && location.isNotBlank() &&
            ownerDeclared && (!companyRepresentative || companyIdentifier.isNotBlank()) && titleNumber.isNotBlank() && unitNumber.isNotBlank() && jurisdiction.isNotBlank() && city.isNotBlank() && priceText.filter { it.isDigit() }.isNotEmpty() && !isBusy
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

    fun onCompanyRepresentative(value: Boolean) = _ui.update { it.copy(companyRepresentative = value, ownerDeclared = false) }
    fun onCompanyIdentifier(value: String) = _ui.update { it.copy(companyIdentifier = value.take(160)) }
    fun onOwnerDeclared(value: Boolean) = _ui.update { it.copy(ownerDeclared = value) }
    fun onTitleNumber(v: String) = _ui.update { it.copy(titleNumber = v.take(160), error = null) }
    fun onUnitNumber(v: String) = _ui.update { it.copy(unitNumber = v.take(160), error = null) }
    fun onJurisdiction(v: String) = _ui.update { it.copy(jurisdiction = v.take(100), error = null) }

    fun postProperty() {
        val s = _ui.value
        val amount = s.priceText.filter { it.isDigit() }.toLongOrNull() ?: 0L
        if (!s.canSubmit || amount <= 0) {
            _ui.update { it.copy(error = "Enter your official title, exact unit and jurisdiction, and confirm ownership") }
            return
        }
        _ui.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            val owner = (AppContainer.authRepository.observeSession().first() as? com.homeapp.data.model.AuthState.SignedIn)?.user
                ?: run { _ui.update { it.copy(isBusy = false, error = "Sign in to post a listing") }; return@launch }
            val submitted = runCatching {
                val payload = kotlinx.serialization.json.JsonObject(mapOf(
                    "title" to kotlinx.serialization.json.JsonPrimitive(s.title),
                    "location" to kotlinx.serialization.json.JsonPrimitive(s.location),
                    "city" to kotlinx.serialization.json.JsonPrimitive(s.city),
                    "listing_type" to kotlinx.serialization.json.JsonPrimitive(s.listingType.type),
                    "price_ugx" to kotlinx.serialization.json.JsonPrimitive(amount),
                    "company_id" to kotlinx.serialization.json.JsonPrimitive(s.companyIdentifier),
                    "bedrooms" to kotlinx.serialization.json.JsonPrimitive(s.bedrooms),
                )).toString()
                AppContainer.verificationRepository.submit(owner.id, "property", s.titleNumber,
                    s.jurisdiction, unit = s.unitNumber, relationship = if (s.companyRepresentative) "owning_company_representative" else "individual_owner", payload = payload)
            }
            if (submitted.isFailure) {
                _ui.update { it.copy(isBusy = false, error = submitted.exceptionOrNull()?.message ?: "Unable to save application") }
                return@launch
            }
            _ui.update { it.copy(isBusy = false, isDone = true) }
        }
    }

    fun dismissError() = _ui.update { it.copy(error = null) }
}
