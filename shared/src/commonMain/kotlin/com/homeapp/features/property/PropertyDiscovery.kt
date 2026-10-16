package com.homeapp.features.property

import com.homeapp.data.model.Property

/** Every constraint is intersected; searching never bypasses selected filters. */
fun filterProperties(rows: List<Property>, query: String, filter: PropertyFilter): List<Property> {
    val terms = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
    val matches = rows.filter { p ->
        val text = "${p.title} ${p.location} ${p.city}".lowercase()
        terms.all { it in text } &&
            (filter.listingType == null || p.listingType == filter.listingType) &&
            (filter.assetType == null || p.assetType == filter.assetType) &&
            (filter.category == null || p.category == filter.category) &&
            (filter.maxPrice == null || p.priceUgx <= filter.maxPrice) &&
            (!filter.verifiedOnly || p.isVerified)
    }
    return when (filter.sort) {
        "PRICE_ASC" -> matches.sortedBy { it.priceUgx }
        "PRICE_DESC" -> matches.sortedByDescending { it.priceUgx }
        else -> matches.sortedWith(compareByDescending<Property> { it.isVerified }.thenByDescending { it.rating }.thenBy { it.id })
    }
}
