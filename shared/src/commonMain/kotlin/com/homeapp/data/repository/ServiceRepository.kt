package com.homeapp.data.repository

import com.homeapp.data.model.LegacyBooking
import com.homeapp.data.model.ServiceProfessional
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {
    fun observeAllProfessionals(): Flow<List<ServiceProfessional>>
    fun observeByCategory(category: String): Flow<List<ServiceProfessional>>
    fun observeProfessionalById(id: Long): Flow<ServiceProfessional?>
    suspend fun insertProfessional(professional: ServiceProfessional)
    fun observeAllBookings(): Flow<List<LegacyBooking>>
    fun observeBookingsForProfessional(professionalId: Long): Flow<List<LegacyBooking>>
    suspend fun insertBooking(booking: LegacyBooking)
    suspend fun bookingsCount(): Long
}
