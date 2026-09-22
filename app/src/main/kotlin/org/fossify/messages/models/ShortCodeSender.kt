package org.fossify.messages.models

data class ShortCodeSender(
    val address: String,
    val snippet: String = "",
)
