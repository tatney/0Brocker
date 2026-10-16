package com.homeapp.features.services.map

import com.homeapp.core.model.matches
import com.homeapp.data.model.*
import com.homeapp.features.verification.VerificationPolicy

/** Location matching is deterministic; the server repeats eligibility at request/acceptance. */
fun nearbyMatches(providers: List<ServiceProvider>, location: GeoPoint?, category: ServiceCategory,
    query: String, sort: SortOption, now: Long): List<ServiceProvider> {
    if(location==null) return emptyList()
    val matches=providers.filter {p->VerificationPolicy.eligible(p,now)&&p.status==ProviderStatus.AVAILABLE&&category.matches(p.category)&&
        (query.isBlank()||p.name.contains(query,true)||p.category.contains(query,true)||p.headline.contains(query,true))
    }.map {p->
        val distance=if(p.distanceOriginLat==location.lat && p.distanceOriginLng==location.lng) p.distanceKm else haversineKm(location,GeoPoint(p.lat,p.lng))
        p.copy(distanceKm=distance,etaMinutes=(distance/30.0*60.0).toInt().coerceAtLeast(5))
    }.filter {it.distanceKm<=it.coverageRadiusKm}
    return when(sort) {
        SortOption.NEAREST -> matches.sortedWith(compareBy<ServiceProvider> {it.distanceKm}.thenBy {it.id})
        SortOption.HIGHEST_RATED,SortOption.RECOMMENDED -> matches.sortedWith(compareByDescending<ServiceProvider> {VerificationPolicy.reliableRating(it)}.thenBy {it.distanceKm})
        SortOption.LOWEST_PRICE -> matches.sortedWith(compareBy<ServiceProvider> {it.priceFrom}.thenBy {it.distanceKm})
        SortOption.FASTEST -> matches.sortedWith(compareBy<ServiceProvider> {it.etaMinutes}.thenBy {it.distanceKm})
    }
}
