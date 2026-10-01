package com.homeapp.core.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.homeapp.core.icons.IconBolt
import com.homeapp.core.icons.IconBroom
import com.homeapp.core.icons.IconBug
import com.homeapp.core.icons.IconCar
import com.homeapp.core.icons.IconDroplet
import com.homeapp.core.icons.IconHammer
import com.homeapp.core.icons.IconHome
import com.homeapp.core.icons.IconLeaf
import com.homeapp.core.icons.IconPaintRoller
import com.homeapp.core.icons.IconPerson
import com.homeapp.core.icons.IconPlug
import com.homeapp.core.icons.IconSaw
import com.homeapp.core.icons.IconScissors
import com.homeapp.core.icons.IconSofa
import com.homeapp.core.icons.IconSnowflake
import com.homeapp.core.icons.IconSteeringWheel
import com.homeapp.core.icons.IconTools
import com.homeapp.core.icons.IconTree
import com.homeapp.core.icons.IconTruck
import com.homeapp.core.icons.IconWrench
import com.homeapp.core.theme.CategoryColors
import com.homeapp.core.theme.StatusColors
import com.homeapp.core.theme.categoryColorsAt
import com.homeapp.core.theme.categoryColorsOf
import com.homeapp.core.theme.kAssetCar
import com.homeapp.core.theme.kAssetHouse
import com.homeapp.core.theme.kAssetLand
import com.homeapp.core.theme.kListingBuy
import com.homeapp.core.theme.kListingRent
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kStatusAvailableColors
import com.homeapp.core.theme.kStatusBusyColors
import com.homeapp.core.theme.kStatusOfflineColors
import com.homeapp.core.theme.kStatusScheduledColors
import com.homeapp.data.model.ProviderStatus
import com.homeapp.data.model.Property
import com.homeapp.data.model.ServiceCategory
import com.homeapp.data.model.ServiceProfessional
import com.homeapp.data.model.ServiceProvider

// ─────────────────────────────────────────────────────────────────────────────
// CATEGORY PRESENTATION
//
// The single place where a service category resolves to a glyph and a colour.
// Chips, rails, map markers, provider sheets and avatars all read from here, so
// "Painting" is guaranteed to look identical wherever it appears.
// ─────────────────────────────────────────────────────────────────────────────

/** The recognisable glyph that stands for this category. */
val ServiceCategory.icon: ImageVector
    get() = when (this) {
        ServiceCategory.ALL -> IconTools
        ServiceCategory.CLEANING -> IconBroom
        ServiceCategory.PLUMBING -> IconWrench
        ServiceCategory.ELECTRICAL -> IconBolt
        ServiceCategory.PAINTING -> IconPaintRoller
        ServiceCategory.REPAIRS -> IconHammer
        ServiceCategory.MOVING -> IconTruck
        ServiceCategory.CARPENTRY -> IconSaw
        ServiceCategory.AC_REPAIR -> IconSnowflake
        ServiceCategory.PEST_CONTROL -> IconBug
        ServiceCategory.GARDENING -> IconLeaf
        ServiceCategory.INTERIOR_DESIGN -> IconSofa
        ServiceCategory.BEAUTY -> IconScissors
        ServiceCategory.AUTOMOTIVE -> IconSteeringWheel
        ServiceCategory.APPLIANCE -> IconPlug
    }

/** This category's accent / container / on-container trio. */
val ServiceCategory.colors: CategoryColors
    get() = categoryColorsAt(ordinal)

/** Secondary glyph, used where a category needs a supporting metaphor. */
val ServiceCategory.supportingIcon: ImageVector
    get() = when (this) {
        ServiceCategory.PLUMBING -> IconDroplet
        ServiceCategory.AUTOMOTIVE -> IconCar
        ServiceCategory.MOVING -> IconTruck
        ServiceCategory.INTERIOR_DESIGN -> IconSofa
        ServiceCategory.BEAUTY -> IconPerson
        else -> icon
    }

// ─────────────────────────────────────────────────────────────────────────────
// CATEGORY MATCHING
//
// The database stores free-text category strings, and they never matched the
// enum labels exactly: the seed data writes "Interior Design" and "Appliance
// Repair" while the enum says "Interior" and "Appliance", and the legacy
// `service_professionals` table uses yet another vocabulary ("Plumber",
// "Electrician", "Salon at Home", "Car Wash"). Comparing labels directly meant
// those filters silently returned zero providers.
// ─────────────────────────────────────────────────────────────────────────────

private val categoryAliases: Map<ServiceCategory, Set<String>> = mapOf(
    ServiceCategory.CLEANING to setOf("cleaning", "cleaner", "house cleaning", "deep cleaning", "sanitation"),
    ServiceCategory.PLUMBING to setOf("plumbing", "plumber", "plumbers"),
    ServiceCategory.ELECTRICAL to setOf("electrical", "electrician", "electricians", "wiring", "solar"),
    ServiceCategory.PAINTING to setOf("painting", "painter", "painters", "paint", "decorating"),
    ServiceCategory.REPAIRS to setOf("repairs", "repair", "handyman", "maintenance", "fixit"),
    ServiceCategory.MOVING to setOf("moving", "movers", "packers and movers", "packers movers", "removals", "relocation"),
    ServiceCategory.CARPENTRY to setOf("carpentry", "carpenter", "woodwork", "furniture"),
    ServiceCategory.AC_REPAIR to setOf("ac repair", "ac", "air conditioning", "hvac", "cooling"),
    ServiceCategory.PEST_CONTROL to setOf("pest control", "pest", "fumigation", "termite"),
    ServiceCategory.GARDENING to setOf("gardening", "garden", "landscaping", "lawn"),
    ServiceCategory.INTERIOR_DESIGN to setOf("interior", "interior design", "interiors"),
    ServiceCategory.BEAUTY to setOf("beauty", "salon at home", "salon", "hair", "spa", "nails"),
    ServiceCategory.AUTOMOTIVE to setOf("automotive", "car wash", "mechanic", "detailing", "garage"),
    ServiceCategory.APPLIANCE to setOf("appliance", "appliance repair", "appliances"),
)

/** Aliases short enough to be ambiguous inside another word ("car" in "carpentry"). */
private const val AMBIGUOUS_ALIAS_LENGTH = 6

private fun normaliseCategory(raw: String): String = raw
    .trim()
    .lowercase()
    .map { if (it.isLetterOrDigit()) it else ' ' }
    .joinToString(separator = "")
    .replace(Regex("\\s+"), " ")
    .trim()

/**
 * Whether [raw] — a category string from any table in the app — belongs to this
 * category. Matching is alias-based rather than label-equality so that every
 * seeded and user-entered spelling resolves to exactly one category.
 *
 * Note this is a membership test, used when the category is already known
 * (a user picked it from the rail). Use [serviceCategoryOf] to go the other way.
 */
fun ServiceCategory.matches(raw: String): Boolean = matchScore(raw) > 0

/**
 * How strongly [raw] matches this category, as the length of the matching
 * alias — or 0 for no match.
 *
 * Returning a score rather than a boolean lets the reverse lookup pick the
 * *most specific* category. "Appliance Repair" contains REPAIRS' alias
 * "repair", so a first-match-wins scan in enum order filed appliances under
 * general repairs; scoring by alias length puts APPLIANCE (16) ahead of
 * REPAIRS (6).
 */
private fun ServiceCategory.matchScore(raw: String): Int {
    if (this == ServiceCategory.ALL) return Int.MAX_VALUE
    val needle = normaliseCategory(raw)
    if (needle.isEmpty()) return 0
    val aliases = categoryAliases[this].orEmpty()
    if (needle in aliases) return needle.length
    // Conservative fallback: only aliases long enough that they cannot be a
    // substring of an unrelated category name.
    return aliases
        .filter { it.length >= AMBIGUOUS_ALIAS_LENGTH && needle.contains(it) }
        .maxOfOrNull { it.length } ?: 0
}

/**
 * Resolves the owning category for a free-text category string.
 *
 * Exact aliases win first; otherwise the category with the longest matching
 * alias wins, so more specific labels beat the general handyman bucket.
 * Unknown values fall back to [ServiceCategory.REPAIRS], which is the app's
 * general bucket, so an unrecognised string still gets a sensible glyph and
 * colour instead of appearing unstyled.
 */
fun serviceCategoryOf(raw: String): ServiceCategory {
    val exact = ServiceCategory.entries.firstOrNull {
        it != ServiceCategory.ALL && normaliseCategory(raw) in categoryAliases[it].orEmpty()
    }
    if (exact != null) return exact

    return ServiceCategory.entries
        .filter { it != ServiceCategory.ALL }
        .maxByOrNull { it.matchScore(raw) }
        ?.takeIf { it.matchScore(raw) > 0 }
        ?: ServiceCategory.REPAIRS
}

/**
 * Category names that exist in the legacy `service_professionals` table, so the
 * original booking flow can still be reached by category where it has rows.
 * Empty means the category is marketplace-only.
 */
val ServiceCategory.legacyCategoryNames: Set<String>
    get() = when (this) {
        ServiceCategory.CLEANING -> setOf("Cleaning")
        ServiceCategory.PLUMBING -> setOf("Plumber")
        ServiceCategory.ELECTRICAL -> setOf("Electrician")
        ServiceCategory.AC_REPAIR -> setOf("AC Repair")
        ServiceCategory.PEST_CONTROL -> setOf("Pest Control")
        ServiceCategory.BEAUTY -> setOf("Salon at Home")
        ServiceCategory.AUTOMOTIVE -> setOf("Car Wash")
        ServiceCategory.APPLIANCE -> setOf("Appliance")
        else -> emptySet()
    }

// ─────────────────────────────────────────────────────────────────────────────
// PROVIDER STATUS
// ─────────────────────────────────────────────────────────────────────────────

val ProviderStatus.colors: StatusColors
    get() = when (this) {
        ProviderStatus.AVAILABLE -> kStatusAvailableColors
        ProviderStatus.BUSY -> kStatusBusyColors
        ProviderStatus.SCHEDULED -> kStatusScheduledColors
        ProviderStatus.OFFLINE -> kStatusOfflineColors
    }

// ─────────────────────────────────────────────────────────────────────────────
// PROPERTY
// ─────────────────────────────────────────────────────────────────────────────

/** How a property asset type is represented across the app. */
enum class PropertyAssetStyle(
    val label: String,
    val icon: ImageVector,
    val accent: Color,
) {
    HOUSE("House", IconHome, kAssetHouse),
    LAND("Land", IconTree, kAssetLand),
    CAR("Car", IconCar, kAssetCar),
    ;

    companion object {
        /** Resolves [assetType] (`HOUSE` / `LAND` / `CAR`) to its style. */
        fun of(assetType: String): PropertyAssetStyle = when (assetType.uppercase()) {
            "LAND" -> LAND
            "CAR" -> CAR
            else -> HOUSE
        }
    }
}

val PropertyAssetStyle.colors: CategoryColors
    get() = categoryColorsOf(accent)

/**
 * The listing's asset icon, resolved from [Property.assetType].
 *
 * `Property.emoji` is a legacy seed column that held a house/land/car glyph. It
 * stays in the schema for compatibility but is no longer rendered, because a
 * seeded emoji per row is not something a new listing could ever pick up.
 */
val Property.assetIcon: ImageVector
    get() = PropertyAssetStyle.of(assetType).icon

/** The asset treatment for this listing, for tinting the card and icon tile. */
val Property.assetStyle: PropertyAssetStyle
    get() = PropertyAssetStyle.of(assetType)

/**
 * The provider's trade icon, resolved from the free-text [ServiceProvider.category].
 *
 * This is what every provider avatar, card and detail header should show. The
 * legacy `emoji` column is a per-seed trade glyph and does not identify a person.
 */
val ServiceProvider.categoryIcon: ImageVector
    get() = serviceCategoryOf(category).icon

/** The resolved category for this provider, for tinting. */
val ServiceProvider.serviceCategory: ServiceCategory
    get() = serviceCategoryOf(category)

/**
 * Same treatment for the legacy booking-table [ServiceProfessional], which is a
 * separate model from [ServiceProvider] but carries the same free-text
 * `category` string. Both are used by live screens, so both resolve the same way.
 */
val ServiceProfessional.categoryIcon: ImageVector
    get() = serviceCategoryOf(category).icon

val ServiceProfessional.serviceCategory: ServiceCategory
    get() = serviceCategoryOf(category)

/** Accent for a listing-type tag: teal for rent, brand red for sale. */
fun listingTypeAccent(listingType: String): Color =
    if (listingType.uppercase() == "RENT") kListingRent else kListingBuy

/** Accent for the "All" pseudo-category, which reuses the brand colour. */
val allCategoryAccent: Color get() = kPrimaryRed
