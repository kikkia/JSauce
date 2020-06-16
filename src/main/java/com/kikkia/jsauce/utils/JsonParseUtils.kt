package com.kikkia.jsauce.utils

import com.kikkia.jsauce.models.Sauce
import org.json.JSONObject

class JsonParseUtils {
    companion object {
        fun getSauceForData(header: JSONObject, data: JSONObject) : Sauce {
            // Map the sauce to a given category
            val sauce = Sauce(header.getString("index_name")
                    .split(": ")[1]
                    .split("-")[0],
                header.getString("similarity").toDouble(),
                header.getString("thumbnail"))

            when (header.getInt("index_id")) {
                5, 8, 20, 31, 32, 33 -> {
                    sauce.author = data.getString("member_name")
                    sauce.extUrl = getExtUrl(data)
                    sauce.title = data.getString("title")
                }
                9 -> {
                    sauce.author = data.getString("creator")
                    sauce.extUrl = getExtUrl(data)
                    sauce.source = data.getString("source")
                    sauce.characters = data.getString("characters")
                }
                10 -> {
                    // Drawr is rip, not supporting
                    sauce.extUrl = getExtUrl(data)
                    sauce.author = data.getString("member_name")
                }
                11 -> {
                    sauce.extUrl = getExtUrl(data)
                    sauce.title = data.getString("title")
                }
                12, 25, 26, 27, 29, 30 -> {
                    // Struggling to get a hit on this index, just guessing
                    sauce.extUrl = getExtUrl(data)
                    sauce.author = data.getString("creator")
                    sauce.characters = data.getString("characters")
                    sauce.source = data.getString("source")
                }
                16, 19 -> {
                    sauce.extUrl = getExtUrl(data)
                    sauce.title = data.getString("source")
                    sauce.author = data.getString("creator")
                }
                18 -> {
                    sauce.title = data.getString("eng_name")
                    sauce.author = data.getJSONArray("creator").getString(0)
                }
                21, 22, 23, 24, 36 -> {
                    sauce.extUrl = getExtUrl(data)
                    sauce.title = data.getString("source")
                    sauce.episode = data.getString("part")
                }
                34 -> {
                    sauce.extUrl = getExtUrl(data)
                    sauce.title = data.getString("title")
                    sauce.author = data.getString("author_name")
                }
                35 -> {
                    sauce.extUrl = getExtUrl(data)
                    sauce.author = data.getString("pawoo_user_display_name")
                }
                37 -> {
                    sauce.extUrl = getExtUrl(data)
                    sauce.author = data.getString("author")
                    sauce.title = data.getString("source")
                    sauce.episode = data.getString("part")
                }
            }
            return sauce
        }

        private fun getExtUrl(data: JSONObject) : String {
            return  data.getJSONArray("ext_urls").getString(0)
        }
    }
}