package com.homeapp.features.services.booking2

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant

/** Device-independent Kampala time; schedules are explicit, future-only instants. */
fun bookingSchedule(urgency: String, date: String, time: String, now: Long): Long {
    if (urgency == "NOW") return now
    val requested = LocalDateTime(LocalDate.parse(date), LocalTime.parse(time))
        .toInstant(UtcOffset(hours = 3)).epochSeconds
    require(requested > now) { "Choose a future date and time (Kampala time)" }
    return requested
}
