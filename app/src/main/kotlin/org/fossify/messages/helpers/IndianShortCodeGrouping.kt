package org.fossify.messages.helpers

/**
 * In-memory map of Android thread IDs that belong to the same normalized Indian short code.
 * Populated whenever conversations are loaded from the telephony provider.
 */
object IndianShortCodeGrouping {
    @Volatile
    private var relatedThreadIds: Map<Long, List<Long>> = emptyMap()

    fun updateGroups(groups: Collection<List<Long>>) {
        val map = HashMap<Long, List<Long>>(groups.size * 2)
        for (group in groups) {
            val ids = group.distinct()
            if (ids.isEmpty()) {
                continue
            }
            for (id in ids) {
                map[id] = ids
            }
        }
        relatedThreadIds = map
    }

    fun relatedThreadIds(threadId: Long): List<Long> {
        return relatedThreadIds[threadId] ?: listOf(threadId)
    }

    fun hasMapping(threadId: Long): Boolean {
        return relatedThreadIds.containsKey(threadId)
    }
}
