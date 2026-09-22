package org.fossify.messages.models

class Events {
    class RefreshMessages
    class RefreshConversations(val cacheOnly: Boolean = false)
}
