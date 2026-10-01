package com.homeapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.homeapp.data.model.LegacyBooking
import com.homeapp.data.model.ServiceProfessional
import com.homeapp.db.HomeAppDatabase
import com.homeapp.db.Service_bookings
import com.homeapp.db.Service_professionals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ServiceRepositoryImpl(
    private val db: HomeAppDatabase,
) : ServiceRepository {

    private val queries get() = db.homeAppDatabaseQueries

    override fun observeAllProfessionals(): Flow<List<ServiceProfessional>> =
        queries.selectAllProfessionals().asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeByCategory(category: String): Flow<List<ServiceProfessional>> =
        queries.professionalsByCategory(category).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeProfessionalById(id: Long): Flow<ServiceProfessional?> =
        queries.selectProfessionalById(id).asFlow().mapToOneOrNull(Dispatchers.Default).map { row ->
            row?.toDomain()
        }

    override suspend fun insertProfessional(professional: ServiceProfessional) {
        queries.insertProfessional(
            id = professional.id,
            name = professional.name,
            emoji = professional.emoji,
            category = professional.category,
            rating = professional.rating,
            bookings_count = professional.bookingsCount,
            headline = professional.headline,
        )
    }

    override fun observeAllBookings(): Flow<List<LegacyBooking>> =
        queries.selectAllBookings().asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeBookingsForProfessional(professionalId: Long): Flow<List<LegacyBooking>> =
        queries.selectBookingsForProfessional(professionalId).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun bookingsCount(): Long =
        queries.bookingCount().executeAsOne()

    override suspend fun insertBooking(booking: LegacyBooking) {
        queries.insertBooking(
            id = booking.id,
            user_id = booking.userId,
            professional_id = booking.professionalId,
            service_name = booking.serviceName,
            status = booking.status,
            amount_paise = booking.amountPaise,
            booked_at_epoch = booking.bookedAtEpoch,
        )
    }

    private fun Service_bookings.toDomain() = LegacyBooking(
        id = id,
        userId = user_id,
        professionalId = professional_id,
        serviceName = service_name,
        status = status,
        amountPaise = amount_paise,
        bookedAtEpoch = booked_at_epoch,
    )

    private fun Service_professionals.toDomain() = ServiceProfessional(
        id = id,
        name = name,
        emoji = emoji,
        category = category,
        rating = rating,
        bookingsCount = bookings_count,
        headline = headline,
    )
}
