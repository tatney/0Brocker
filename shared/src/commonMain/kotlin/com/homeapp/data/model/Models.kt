package com.homeapp.data.model

fun Long.toFormattedString(): String {
    val s = this.toString()
    if (s.length <= 3) return s
    val sb = StringBuilder()
    for (i in s.indices) {
        if (i > 0 && (s.length - i) % 3 == 0) sb.append(',')
        sb.append(s[i])
    }
    return sb.toString()
}

fun Double.toFormattedString(): String {
    val rounded = kotlin.math.round(this).toLong()
    return rounded.toFormattedString()
}

fun formatUgx(amount: Double): String = "UGX ${amount.toFormattedString()}"

fun formatOneDecimal(value: Double): String {
    val rounded = kotlin.math.round(value * 10.0) / 10.0
    val whole = rounded.toLong()
    val dec = kotlin.math.round((rounded - whole) * 10.0).toInt()
    return "$whole.$dec"
}

data class User(
    val id: Long,
    val fullName: String,
    val phone: String,
    val email: String?,
    val avatarEmoji: String,
    val isProfessional: Boolean,
    val passwordHash: String = "",
)

sealed interface AuthState {
    data object SignedOut : AuthState
    data class SignedIn(
        val user: User,
        val isOnboarded: Boolean,
    ) : AuthState
}

data class Property(
    val id: Long,
    val title: String,
    val location: String,
    val city: String,
    val listingType: String,          // RENT | BUY
    val assetType: String,            // HOUSE | LAND | CAR
    val category: String,             // RESIDENTIAL | COMMERCIAL
    val priceUgx: Long,
    val priceLabel: String,           // "/mo", "/day", "/acre" or ""
    val rating: Double,
    val isVerified: Boolean,
    val bedrooms: String,
    val emoji: String,
    val ownerId: Long = 0,
    val ownerName: String = "",
    val ownerDeclared: Boolean = false,
) {
    fun formatPrice(): String = formatUgx(priceUgx.toDouble())
    fun priceUnitLabel(): String = when (priceLabel) {
        "/mo" -> "per month"
        "/day" -> "per day"
        "/acre" -> "per acre"
        else -> "asking price"
    }
    fun isRent(): Boolean = listingType == "RENT"
    fun amenities(): List<Pair<String, String>> = buildList {
        if (assetType == "HOUSE") add("Bedrooms" to bedrooms)
        add("Use" to category.titleLowercase())
        add("Area" to "Not supplied")
        add("Deposit / title" to "Confirm with owner")
        add("Furnishing" to "Not supplied")
        add("Brokerage" to "None")
    }
}

private fun formatUgx(amount: Long): String = formatUgx(amount.toDouble())

private fun String.titleLowercase(): String =
    replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

data class ServiceProfessional(
    val id: Long,
    val name: String,
    val emoji: String,
    val category: String,
    val rating: Double,
    val bookingsCount: Long,
    val headline: String,
) {
    fun derivedPriceUgx(): Long = when {
        "Salon" in category -> 50_000L
        "Plumber" in category -> 45_000L
        "Electrician" in category -> 40_000L
        "Cleaning" in category -> 60_000L
        "AC" in category -> 80_000L
        "Car" in category -> 45_000L
        "Pest" in category -> 70_000L
        else -> 35_000L
    }
    fun formatPrice(): String = formatUgx(derivedPriceUgx().toDouble())
}

data class LegacyBooking(
    val id: Long,
    val userId: Long,
    val professionalId: Long,
    val serviceName: String,
    val status: String,
    val amountPaise: Long,
    val bookedAtEpoch: Long,
)

data class WalletAccount(
    val id: Long,
    val userId: Long,
    val balancePaise: Long,
    val currency: String,
    val cardLast4: String,
    val cardType: String,
    val cardName: String,
) {
    fun formatBalance(): String = formatUgx(balancePaise / 100.0)
}

data class WalletTransaction(
    val id: Long,
    val accountId: Long,
    val title: String,
    val meta: String,
    val amountPaise: Long,
    val isCredit: Boolean,
    val createdAtEpoch: Long,
) {
    fun formatAmount(): String {
        val amount = kotlin.math.abs(amountPaise) / 100.0
        val sign = if (isCredit) "+" else "\u2212"
        return "$sign ${formatUgx(amount)}"
    }
}

data class ChatConversation(
    val id: Long,
    val name: String,
    val avatarEmoji: String,
    val lastMessage: String,
    val lastMessageAtEpoch: Long,
    val unreadCount: Long,
)

data class ChatMessage(
    val id: Long,
    val conversationId: Long,
    val sender: String,
    val text: String,
    val createdAtEpoch: Long,
) {
    val isMine: Boolean get() = sender == "me"
}

// ─── Home Services Marketplace Models ───

/**
 * The canonical service categories.
 *
 * The icon and colour for each entry deliberately live in
 * `com.homeapp.core.model.CategoryVisuals` rather than here, so the enum stays
 * free of any UI dependency. This field used to carry a per-category emoji,
 * which is what the category rail rendered before it moved to vectors; the
 * ordinal order is load-bearing for the colour table, so do not reorder.
 */
enum class ServiceCategory(val label: String) {
    ALL("All"),
    CLEANING("Cleaning"),
    PLUMBING("Plumbing"),
    ELECTRICAL("Electrical"),
    PAINTING("Painting"),
    REPAIRS("Repairs"),
    MOVING("Moving"),
    CARPENTRY("Carpentry"),
    AC_REPAIR("AC Repair"),
    PEST_CONTROL("Pest Control"),
    GARDENING("Gardening"),
    INTERIOR_DESIGN("Interior"),
    BEAUTY("Beauty"),
    AUTOMOTIVE("Automotive"),
    APPLIANCE("Appliance"),
}

enum class ProviderStatus(val label: String) {
    AVAILABLE("Available Now"),
    BUSY("Busy"),
    SCHEDULED("Scheduled"),
    OFFLINE("Offline"),
}

data class ServiceProvider(
    val id: Long,
    val name: String,
    val emoji: String,
    val category: String,
    val rating: Double,
    val reviewsCount: Long,
    val jobsCompleted: Long,
    val experienceYears: Int,
    val headline: String,
    val lat: Double,
    val lng: Double,
    val status: ProviderStatus,
    val priceFrom: Long,
    val isVerified: Boolean,
    val coverageRadiusKm: Double,
    val distanceKm: Double = 0.0,
    val etaMinutes: Int = 0,
    val services: List<String> = emptyList(),
) {
    fun formatPrice(): String = formatUgx(priceFrom.toDouble())
    fun formatDistance(): String = "${formatOneDecimal(distanceKm)} km away"
    fun formatEta(): String = "$etaMinutes min"
    fun formatRating(): String = formatOneDecimal(rating)
}

enum class BookingStatus(val label: String) {
    SEARCHING("Searching"),
    PROVIDER_FOUND("Provider Found"),
    ASSIGNED("Provider Assigned"),
    EN_ROUTE("Provider En Route"),
    ARRIVED("Provider Arrived"),
    INSPECTION("Inspection"),
    IN_PROGRESS("Service In Progress"),
    MATERIALS_REQUIRED("Materials Required"),
    COMPLETED("Service Completed"),
    PAYMENT("Payment"),
    REVIEWED("Reviewed"),
    CANCELLED("Cancelled"),
}

data class Booking(
    val id: Long,
    val customerId: Long,
    val providerId: Long,
    val providerName: String = "",
    val providerEmoji: String = "",
    val serviceType: String,
    val description: String = "",
    val status: BookingStatus = BookingStatus.SEARCHING,
    val locationText: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val scheduledAt: Long = 0,
    val startedAt: Long = 0,
    val completedAt: Long = 0,
    val baseCost: Long = 0,
    val additionalCost: Long = 0,
    val platformFee: Long = 0,
    val totalCost: Long = 0,
    val paymentMethod: String = "",
    val isPaid: Boolean = false,
    val createdAtEpoch: Long = 0,
) {
    fun formatTotal(): String = formatUgx(totalCost.toDouble())
    fun formatBase(): String = formatUgx(baseCost.toDouble())
    fun formatAdditional(): String = formatUgx(additionalCost.toDouble())
    fun formatPlatformFee(): String = formatUgx(platformFee.toDouble())
}

data class Quote(
    val id: Long,
    val bookingId: Long,
    val providerId: Long,
    val providerName: String = "",
    val providerEmoji: String = "",
    val labourCost: Long,
    val materialsCost: Long,
    val serviceFee: Long,
    val total: Long,
    val estimatedDuration: String,
    val arrivalTime: String,
    val status: String = "PENDING",
) {
    fun formatTotal(): String = formatUgx(total.toDouble())
}

data class Review(
    val id: Long,
    val bookingId: Long,
    val providerId: Long,
    val customerId: Long,
    val quality: Int,
    val professionalism: Int,
    val timeliness: Int,
    val communication: Int,
    val valueRating: Int,
    val overall: Int,
    val comment: String = "",
    val createdAtEpoch: Long = 0,
) {
    fun overallFormatted(): String = formatOneDecimal((quality + professionalism + timeliness + communication + valueRating) / 5.0)
}
