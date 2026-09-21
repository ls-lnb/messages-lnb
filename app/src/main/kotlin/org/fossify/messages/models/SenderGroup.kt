package org.fossify.messages.models

import org.json.JSONArray
import org.json.JSONObject

data class SenderGroup(
    val id: String,
    val title: String,
    val addresses: List<String>,
) {
    fun containsAddress(address: String): Boolean {
        val key = address.uppercase()
        return addresses.any { it.uppercase() == key }
    }

    companion object {
        fun fromJson(raw: String): List<SenderGroup> {
            if (raw.isBlank()) {
                return emptyList()
            }

            val array = JSONArray(raw)
            return buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val addressesJson = obj.getJSONArray("addresses")
                    val addresses = buildList {
                        for (j in 0 until addressesJson.length()) {
                            add(addressesJson.getString(j).uppercase())
                        }
                    }
                    add(
                        SenderGroup(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            addresses = addresses,
                        )
                    )
                }
            }
        }

        fun toJson(groups: List<SenderGroup>): String {
            val array = JSONArray()
            for (group in groups) {
                array.put(
                    JSONObject().apply {
                        put("id", group.id)
                        put("title", group.title)
                        put("addresses", JSONArray(group.addresses))
                    }
                )
            }
            return array.toString()
        }
    }
}
