package chat.to.server.bot.cache

import org.slf4j.LoggerFactory

class LastReceivedMessagesCache(private val maxCacheSize: Int) {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val cachedMessageIdentifiers = mutableListOf<String>()

    init {
        if (maxCacheSize <= 0) {
            log.debug("Initializing LastReceivedMessagesCache - Cache Disabled!")
        } else {
            log.debug("Initializing LastReceivedMessagesCache with max size $maxCacheSize")
        }
    }

    val size: Int
        get() = cachedMessageIdentifiers.size

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