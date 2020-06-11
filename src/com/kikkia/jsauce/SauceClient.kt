package com.kikkia.jsauce

import com.kikkia.jsauce.models.Sauce

class SauceClient private constructor(builder: Builder) {
    val token: String?
    val proxyUrl: String?
    var authedLongLimit: Int
    var baseLongLimit: Int

    init {
        this.token = builder.token
        this.proxyUrl = builder.proxyUrl
        this.baseLongLimit = 100 // Base requests allowed to ip in a 24 hour period without auth
        this.authedLongLimit = 200 // Base requests allowed to ip when authed in 24 hours
    }

    fun getSauce(url: String) : Sauce {
        
    }

    class Builder {
        var proxyUrl: String? = null
        var token: String? = null

        fun setToken(token: String) : Builder {
            this.token = token
            return this
        }

        fun setProxyUrl(url: String) : Builder {
            this.proxyUrl = url
            return this
        }

        fun build(): SauceClient {
            return SauceClient(this)
        }
    }
}