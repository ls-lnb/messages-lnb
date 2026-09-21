package org.fossify.messages.extensions

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

private val indianShortCodeRegex = Regex("^[A-Za-z]{2}-([A-Za-z][A-Za-z0-9-]*)\$")

/**
 * Indian DLT sender IDs look like `JM-HDFCBK-S` or `AD-AXISBK`.
 * The first two letters are a carrier/header prefix and should be ignored when grouping.
 * Returns the normalized sender (e.g. `HDFCBK-S`) or null if this is not that format.
 */
fun String.normalizedIndianShortCodeSender(): String? {
    val match = indianShortCodeRegex.matchEntire(trim()) ?: return null
    return match.groupValues[1].uppercase()
}
