package com.engineerfred.beststreamsug.mobile.ui.util

import java.util.Locale

fun Long?.formatDuration(): String {
    val millis = this ?: return ""
    if (millis <= 0) return ""
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
    } else {
        String.format(Locale.getDefault(), "%d min", minutes)
    }
}

fun Long?.formatMinutes(): String {
    val millis = this ?: return ""
    if (millis <= 0) return ""
    val minutes = millis / 60000
    return if (minutes < 60) {
        "$minutes min"
    } else {
        val hours = minutes / 60
        val rem = minutes % 60
        if (rem == 0L) "${hours}h" else "${hours}h ${rem}m"
    }
}

fun String?.releaseYear(): String {
    val value = this ?: return ""
    return value.take(4)
}

fun String?.displayDate(): String {
    val value = this ?: return ""
    return value.take(10)
}

fun Int.formatViewCount(): String {
    if (this >= 1_000_000) {
        val value = this / 1_000_000.0
        return String.format(Locale.getDefault(), "%.1fM", value)
    }
    if (this >= 1_000) {
        val value = this / 1_000.0
        return String.format(Locale.getDefault(), "%.1fK", value)
    }
    return this.toString()
}

fun Double.formatRating(): String = String.format(Locale.getDefault(), "%.1f", this)