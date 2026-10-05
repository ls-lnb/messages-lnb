package org.fossify.messages.extensions

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan

fun String.getExtensionFromMimeType(): String {
    return when (lowercase()) {
        "image/png" -> ".png"
        "image/apng" -> ".apng"
        "image/webp" -> ".webp"
        "image/svg+xml" -> ".svg"
        "image/gif" -> ".gif"
        else -> ".jpg"
    }
}

fun String.isImageMimeType(): Boolean {
    return lowercase().startsWith("image")
}

fun String.isGifMimeType(): Boolean {
    return lowercase().endsWith("gif")
}

fun String.isVideoMimeType(): Boolean {
    return lowercase().startsWith("video")
}

fun String.isVCardMimeType(): Boolean {
    val lowercase = lowercase()
    return lowercase.endsWith("x-vcard") || lowercase.endsWith("vcard")
}

fun String.isAudioMimeType(): Boolean {
    return lowercase().startsWith("audio")
}

fun String.isCalendarMimeType(): Boolean {
    return lowercase().endsWith("calendar")
}

fun String.isPdfMimeType(): Boolean {
    return lowercase().endsWith("pdf")
}

fun String.isZipMimeType(): Boolean {
    return lowercase().endsWith("zip")
}

fun String.isPlainTextMimeType(): Boolean {
    return lowercase() == "text/plain"
}

private val similarShortCodeRegex = Regex("^[A-Za-z]{2}-(.+)\$")

/**
 * Optional similarity key for letter short codes like `JM-HDFCBK-S` -> `HDFCBK-S`.
 * Used only for suggestions, never for automatic grouping.
 */
fun String.similarShortCodeKey(): String? {
    val match = similarShortCodeRegex.matchEntire(trim()) ?: return null
    return match.groupValues[1].ifBlank { null }?.uppercase()
}

/**
 * Marks every case-insensitive occurrence of [query] with a yellow
 * highlighter-like background (and dark text), so search matches
 * are easy to spot in both light and dark themes.
 */
fun String.highlightSearchMatches(query: String): CharSequence {
    if (query.isEmpty() || !contains(query, true)) {
        return this
    }

    val spannable = SpannableString(this)
    var startIndex = 0
    while (true) {
        val start = indexOf(query, startIndex, ignoreCase = true)
        if (start == -1) {
            break
        }
        val end = start + query.length
        spannable.setSpan(BackgroundColorSpan(Color.YELLOW), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(Color.BLACK), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        startIndex = end
    }
    return spannable
}
