package com.engineerfred.beststreamsug.mobile.ui.util

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.mobile.ui.theme.PremiumGold
import com.engineerfred.beststreamsug.mobile.ui.theme.RatingGreen

private val uriRegex = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://")

fun String?.toSafeHttpsUrl(): String? {
    val value = this ?: return null
    if (value.isBlank()) return null
    return if (uriRegex.containsMatchIn(value)) value else {
        val builder = Uri.parse(value)
        if (builder.scheme == null) "https:$value" else value
    }
}

fun <T> AppResult<List<T>>.contentOrEmpty(): List<T> =
    (this as? AppResult.Success)?.data.orEmpty()

fun <T> AppResult<T>?.isLoading(): Boolean =
    this == null || this is AppResult.Failure

fun Int.toCategoryColor(): Color = when (this % 12) {
    0 -> Color(0xFF4E342E)
    1 -> Color(0xFF283593)
    2 -> Color(0xFF00695C)
    3 -> Color(0xFF4A148C)
    4 -> Color(0xFFB71C1C)
    5 -> Color(0xFF1B5E20)
    6 -> Color(0xFFE65100)
    7 -> Color(0xFF0D47A1)
    8 -> Color(0xFF880E4F)
    9 -> Color(0xFF37474F)
    10 -> Color(0xFF5D4037)
    else -> Color(0xFF1565C0)
}

fun Boolean.premiumColor(): Color = if (this) PremiumGold else RatingGreen