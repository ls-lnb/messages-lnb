package org.fossify.messages.helpers

/**
 * In-memory map of Android thread IDs that belong to the same user-defined sender group.
 * Populated whenever conversations are loaded from the telephony provider.
 */
object SenderGrouping {
    @Volatile
    var pendingUiRefresh = false

    /** Set after a settings import; triggers a full conversation reload with the sync bar. */
    @Volatile
    var pendingRestoreUiRefresh = false

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

    fun mergeGroup(ids: List<Long>) {
        val distinct = ids.distinct()
        if (distinct.isEmpty()) {
            return
        }
        val map = HashMap(relatedThreadIds)
        for (id in distinct) {
            map[id] = distinct
        }
        relatedThreadIds = map
    }
}
