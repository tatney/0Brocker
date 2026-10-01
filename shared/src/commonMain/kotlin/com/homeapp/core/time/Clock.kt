package com.homeapp.core.time

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Wall-clock time in epoch milliseconds, for common code.
 *
 * `System.currentTimeMillis()` is JVM-only, but it was being called from
 * `commonMain` in roughly a dozen view models and repositories, which is why the
 * Android and iOS compilations of this module did not build even though the JVM
 * one did. Everything time-related now goes through here instead.
 *
 * Epoch *milliseconds* rather than microseconds because the values are persisted
 * into SQLDelight columns and compared against seeded epoch-second data.
 */
fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

/** The current instant, for the rare caller that needs more than epoch millis. */
fun currentInstant(): Instant = Clock.System.now()

/**
 * Wall-clock time in epoch **seconds**.
 *
 * Every timestamp column in the SQLDelight schema is epoch seconds, and most
 * call sites were dividing `currentTimeMillis()` by 1000 inline. This keeps that
 * conversion in one place.
 */
fun currentTimeEpochSeconds(): Long = Clock.System.now().toEpochMilliseconds() / 1000L

/**
 * Formats [epochSeconds] as a short relative age: `just now`, `5m ago`, `3h ago`.
 *
 * Shared so the booking list and the transactions list cannot drift apart again.
 * Both previously had their own copy, one of which divided by 1000 and the other
 * of which did not, so the same booking could read `45m ago` and `27000d ago`.
 *
 * Anything a week or older falls through to [formatCoarseAge].
 */
fun formatRelativeAge(
    epochSeconds: Long,
    nowSeconds: Long = currentTimeEpochSeconds(),
): String {
    val diff = nowSeconds - epochSeconds
    return when {
        diff < 60 -> "just now"
        diff < 3_600 -> "${diff / 60}m ago"
        diff < 86_400 -> "${diff / 3_600}h ago"
        else -> formatCoarseAge(epochSeconds, nowSeconds)
    }
}

/** Weeks/months/years bucket for timestamps older than a week. */
fun formatCoarseAge(epochSeconds: Long, nowSeconds: Long = currentTimeEpochSeconds()): String {
    val diff = (nowSeconds - epochSeconds).coerceAtLeast(0L)
    val days = diff / 86_400L
    return when {
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        days < 365 -> "${days / 30}mo ago"
        else -> "${days / 365}y ago"
    }
}
