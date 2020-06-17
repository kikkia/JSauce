package com.kikkia.jsauce.models

import java.time.Instant
import java.util.*

/**
 * Tracks rate limiting for getting that sauce. (SauceNao has very non-traditional rate limiting)
 * 2 rate limits, short (30 seconds) and long (24 hours)
 */
class SauceLimit constructor(private var longLimit: Int, private var shortLimit: Int){
    var longQueue: Queue<Instant> = LinkedList() // Tracks last requests 24 hour period
    var shortQueue: Queue<Instant> = LinkedList() // Tracks last requests 30sec period

    private val longRefresh = 60 * 60 * 24
    private val shortRefresh = 30

    fun canSend() : Boolean {
        // Clear out our ratelimit tracking queues
        val now = Instant.now()
        if (!longQueue.isEmpty()) {
            do {
                if (longQueue.peek().isBefore(now.minusSeconds(longRefresh.toLong()))) {
                    longQueue.poll()
                    longLimit++
                }
            } while (!longQueue.isEmpty() && longQueue.peek().isBefore(now.minusSeconds(longRefresh.toLong())))
        }
        if (!shortQueue.isEmpty()) {
            do {
                if (shortQueue.peek().isBefore(now.minusSeconds(shortRefresh.toLong()))) {
                    shortQueue.poll()
                    shortLimit++
                }
            } while (!shortQueue.isEmpty() && shortQueue.peek().isBefore(now.minusSeconds(shortRefresh.toLong())))
        }

        val canSend = shortLimit > 0 && longLimit > 0
        if (canSend) {
            shortLimit--
            shortQueue.offer(Instant.now())
            longLimit--
            longQueue.offer(Instant.now())
        }

        return canSend
    }

    fun setRemaining(long: Int, short: Int) {
        this.longLimit = long
        this.shortLimit = short
    }
}