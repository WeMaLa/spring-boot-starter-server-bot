package chat.to.server.bot.cache

import chat.to.server.bot.authentication.BotStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BotStatusCacheTest {

    private val cache = BotStatusCache()

    @Test
    internal fun `verify initial bot status`() {
        assertThat(cache.botStatus).isEqualTo(BotStatus.STARTING)
    }

    @Test
    internal fun `all is fine (ok)`() {
        cache.ok()
        assertThat(cache.botStatus).isEqualTo(BotStatus.OK)
    }

    @Test
    internal fun `authentication failed`() {
        cache.authenticationFailed()
        assertThat(cache.botStatus).isEqualTo(BotStatus.AUTHENTICATION_FAILED)
    }

    @Test
    internal fun `registration failed`() {
        cache.registrationFailed()
        assertThat(cache.botStatus).isEqualTo(BotStatus.REGISTRATION_FAILED)
    }

    @Test
    internal fun `mark messages as read failed`() {
        cache.markMessagesAsReadFailed()
        assertThat(cache.botStatus).isEqualTo(BotStatus.MARK_MESSAGES_FAILED)
    }

    @Test
    internal fun `receive messages failed`() {
        cache.receiveMessagesFailed()
        assertThat(cache.botStatus).isEqualTo(BotStatus.RECEIVE_MESSAGES_FAILED)
    }
}