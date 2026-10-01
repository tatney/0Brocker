package com.homeapp.data.repository

import com.homeapp.core.time.currentTimeEpochSeconds

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.homeapp.data.model.Property
import com.homeapp.db.HomeAppDatabase
import com.homeapp.db.Properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class PropertyRepositoryImpl(
    private val db: HomeAppDatabase,
) : PropertyRepository {

    private val queries get() = db.homeAppDatabaseQueries

    override fun observeAll(): Flow<List<Property>> =
        queries.selectAllProperties().asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun search(query: String): Flow<List<Property>> =
        queries.searchProperties(query).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeByType(type: String): Flow<List<Property>> =
        queries.propertiesByType(type).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeByFilters(listingType: String?, assetType: String?, category: String?): Flow<List<Property>> =
        queries.propertiesByFilters(
            listingType = listingType,
            assetType = assetType,
            category = category,
        ).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeById(id: Long): Flow<Property?> =
        queries.selectPropertyById(id).asFlow().mapToOneOrNull(Dispatchers.Default).map { it?.toDomain() }

    override fun observeFavoriteProperties(): Flow<List<Property>> =
        queries.favoriteProperties().asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun isFavorite(id: Long): Flow<Boolean> =
        queries.isFavorite(id).asFlow().mapToOne(Dispatchers.Default).map { it > 0L }

    override suspend fun toggleFavorite(id: Long) {
        if (isFavorite(id).first()) {
            queries.deleteFavorite(id)
        } else {
            queries.insertFavorite(id, currentTimeEpochSeconds())
        }
    }

    override suspend fun favoritesCount(): Long =
        queries.favoritesCount().executeAsOne()

    override suspend fun propertyCount(): Long =
        queries.propertyCount().executeAsOne()

    override suspend fun insert(property: Property) {
        queries.insertProperty(
            id = property.id,
            title = property.title,
            location = property.location,
            city = property.city,
            listing_type = property.listingType,
            asset_type = property.assetType,
            property_category = property.category,
            price_ugx = property.priceUgx,
            price_label = property.priceLabel,
            rating = property.rating,
            is_verified = if (property.isVerified) 1L else 0L,
            bedrooms = property.bedrooms,
            emoji = property.emoji,
        )
        queries.insertPropertyOwner(property.id, property.ownerId, property.ownerName, if (property.ownerDeclared) 1L else 0L)
    }

    private fun Properties.toDomain(): Property {
        val owner = queries.selectPropertyOwner(id).executeAsOneOrNull()
        return Property(
        id = id,
        title = title,
        location = location,
        city = city,
        listingType = listing_type,
        assetType = asset_type,
        category = property_category,
        priceUgx = price_ugx,
        priceLabel = price_label,
        rating = rating,
        isVerified = is_verified == 1L,
        bedrooms = bedrooms,
        emoji = emoji,
        ownerId = owner?.owner_id ?: 0,
        ownerName = owner?.owner_name ?: "",
        ownerDeclared = owner?.owner_declared == 1L,
    )
    }
}
