package chat.to.server.bot.cache

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LastReceivedMessagesCacheTest {

    private val cache = LastReceivedMessagesCache(10)

    @Test
    internal fun `add message identifier and assert it is in the cache`() {
        cache.addMessageIdentifierToCache("test-identifier")
        assertThat(cache.containsMessageIdentifier("test-identifier")).isTrue()
    }

    @Test
    internal fun `contains message identifier with message is not existing`() {
        assertThat(cache.containsMessageIdentifier("not-existing")).isFalse()
    }

    @Test
    internal fun `verify max size`() {
        for (counter in 1..10) {
            cache.addMessageIdentifierToCache("test-identifier-$counter")
            assertThat(cache.size).isEqualTo(counter)
        }

        cache.addMessageIdentifierToCache("max-size-reached")
        assertThat(cache.size).isEqualTo(10)
    }

    @Test
    internal fun `disable cache by setting maxCacheSize to 0`() {
        val cache = LastReceivedMessagesCache(0)

        cache.addMessageIdentifierToCache("a-message-identifier")
        assertThat(cache.containsMessageIdentifier("a-message-identifier")).isFalse()
        assertThat(cache.size).isEqualTo(0)
    }
}