package com.kikkia.jsauce.models

class Sauce(val indexName: String, val similarity: Double, val thumbnail: String) {
    var extUrl: String? = null
    var author: String? = null
    var episode: String? = null
    var characters: String? = null
    var source: String? = null
    var title: String? = null
}