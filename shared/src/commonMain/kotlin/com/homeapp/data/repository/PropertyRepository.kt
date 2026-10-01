package com.homeapp.data.repository

import com.homeapp.data.model.Property
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {
    fun observeAll(): Flow<List<Property>>
    fun search(query: String): Flow<List<Property>>
    fun observeByType(type: String): Flow<List<Property>>
    fun observeByFilters(listingType: String?, assetType: String?, category: String?): Flow<List<Property>>
    fun observeById(id: Long): Flow<Property?>
    fun observeFavoriteProperties(): Flow<List<Property>>
    fun isFavorite(id: Long): Flow<Boolean>
    suspend fun toggleFavorite(id: Long)
    suspend fun favoritesCount(): Long
    suspend fun propertyCount(): Long
    suspend fun insert(property: Property)
}
