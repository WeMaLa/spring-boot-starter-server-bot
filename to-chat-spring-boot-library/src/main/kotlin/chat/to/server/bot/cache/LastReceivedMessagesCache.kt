package chat.to.server.bot.cache

import chat.to.server.bot.mapper.formatUTCDateToISO8601
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class LastReceivedMessagesCache(private val maxCacheSize: Int,
                                private var lastIso8601ServerDate: LocalDateTime? = null) {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val cachedMessageIdentifiers = mutableListOf<String>()

    init {
        if (maxCacheSize <= 0) {
            log.debug("Initializing LastReceivedMessagesCache - Cache Disabled!")
        } else {
            log.debug("Initializing LastReceivedMessagesCache with max size $maxCacheSize")
        }
    }

    val lastIso8601ServerDateWithBuffer: String?
        get() = lastIso8601ServerDate?.minus(500L, ChronoUnit.MILLIS)?.formatUTCDateToISO8601()

    val size: Int
        get() = cachedMessageIdentifiers.size

    fun updateLastIso8601ServerDate(lastIso8601ServerDate: LocalDateTime?) {
        this.lastIso8601ServerDate = lastIso8601ServerDate
    }

    fun addMessageIdentifierToCache(identifier: String) {
        if (maxCacheSize <= 0) {
            return
        }

        if (size >= maxCacheSize) {
            val removeMessageIdentifier = cachedMessageIdentifiers.removeAt(0)
            log.debug("Maximum cache size reached. First message identifier '$removeMessageIdentifier' removed")
        }

        cachedMessageIdentifiers.add(identifier)

        log.debug("Added message identifier '$identifier' to cache (actual cache size $size)")
    }

    fun containsMessageIdentifier(identifier: String) = cachedMessageIdentifiers.contains(identifier)

}