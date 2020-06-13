package com.kikkia.jsauce

import com.kikkia.jsauce.models.Endpoint
import com.kikkia.jsauce.models.Sauce
import com.kikkia.jsauce.models.SauceLimit
import com.kikkia.jsauce.models.exceptions.NoSauceFoundException
import com.kikkia.jsauce.models.exceptions.SauceException
import com.kikkia.jsauce.models.exceptions.TooMuchSauceException
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import java.lang.Exception
import org.json.JSONObject


class SauceClient private constructor(builder: Builder) {
    private val token: String?
    private val proxies: List<Endpoint>
    private val authedLocalLimit: SauceLimit
    private val baseLocalLimit: SauceLimit


    init {
        this.token = builder.getToken()
        this.proxies = builder.getProxies()
        this.baseLocalLimit = SauceLimit(100, 4)
        this.authedLocalLimit = SauceLimit(200, 5)
    }

    fun getSauce(url: String) : Sauce {
        // Try proxies first
        if (proxies.isNotEmpty()) {
            for (e in proxies) {
                if (!e.rateLimit.canSend())
                    continue
                try {
                    return getSauceProxy(e, url)
                } catch (e : Exception) {
                    continue // Try the next proxy
                }
            }
        }

        if (token != null && authedLocalLimit.canSend()) {
            try {
                return getSauceRequest(url, token)
            } catch (e : Exception) {
                // Do nothing, fallback to unauthed
            }
        }

        if (baseLocalLimit.canSend()) {
            try {
                return getSauceRequest(url)
            } catch (e: Exception) {
                throw SauceException("Failed to get sauce")
            }
        } else {
            throw TooMuchSauceException("Tried to get sauce but no method had the usage left to do so.")
        }
    }

    // Sends a basic request through a proxy endpoint
    fun getSauceProxy(endpoint: Endpoint, url: String) : Sauce {
        val json = JSONObject()
        json.put("url", url)
        val entity = StringEntity(json.toString())

        HttpClients.createDefault().use { client ->
            val post = HttpPost(endpoint.url)
            post.addHeader("Content-type", "application/json")
            post.entity = (entity)
            val response = client.execute(post)

            return mapToSauce(response, endpoint)
        }
    }

    private fun getSauceRequest(url: String) : Sauce {
        return getSauceRequest(url, null)
    }

    private fun getSauceRequest(url: String, token: String?) : Sauce {
        HttpClients.createDefault().use { client ->
            var snUrl = "https://saucenao.com/search.php?db=999&output_type=2&testmode=0&numres=1&url=" +
                    url
            if (token != null)
                snUrl += "&api_key=$token"
            val get = HttpGet(snUrl)
            val response = client.execute(get)

            return mapToSauce(response)
        }
    }

    private fun mapToSauce(response: HttpResponse) : Sauce {
        return mapToSauce(response, null)
    }

    private fun mapToSauce(response: HttpResponse, endpoint: Endpoint?) : Sauce {
        when {
            response.statusLine.statusCode == 429 -> {
                throw TooMuchSauceException("RatelimitHit")
            }
            response.statusLine.statusCode != 200 -> {
                throw SauceException("Hit status code ${response.statusLine.statusCode} on request to proxy")
            }
            else -> {
                val json = JSONObject(response.entity.toString())

                endpoint?.rateLimit?.setRemaining(json.getInt("long_remaining"), json.getInt("short_remaining"))

                if (json.getJSONArray("results").isEmpty) {
                    throw NoSauceFoundException("No results found")
                }

                val result = json.getJSONArray("results").getJSONObject(0)
                val header = result.getJSONObject("header")
                val data = result.getJSONObject("data")
                return Sauce(
                    header.getString("thumbnail"),
                    header.getString("similarity").toDouble(),
                    header.getString("index_name"),
                    data.getString("ext_urls"),
                    data.getString("title"),
                    data.getString("author_name"),
                    data.getString("author_url")
                )
            }
        }
    }

    class Builder {
        private var proxies: MutableList<Endpoint> = ArrayList()
        private var token: String? = null

        fun setToken(token: String) : Builder {
            this.token = token
            return this
        }

        fun addProxy(url: String) : Builder {
            this.proxies.add(Endpoint(url, SauceLimit(100, 4)))
            return this
        }

        fun getProxies() : MutableList<Endpoint> {
            return proxies
        }

        fun getToken() : String? {
            return token
        }

        fun build(): SauceClient {
            return SauceClient(this)
        }
    }
}