package chat.to.server.bot.cache

import chat.to.server.bot.authentication.BotStatus
import chat.to.server.bot.authentication.BotStatusChangedListener
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BotStatusCacheTest {

    private val botStatusChangedListener = TestBotStatusChangeListener()
    private val cache = BotStatusCache(botStatusChangedListener)

    @Test
    internal fun `verify initial bot status`() {
        assertThat(cache.botStatus).isEqualTo(BotStatus.STARTING)
        assertThat(botStatusChangedListener.numberOfListenerCalls).isEqualTo(1)
        assertThat(botStatusChangedListener.botStatus).isEqualTo(BotStatus.STARTING)
    }

    @Test
    internal fun `all is fine (ok)`() {
        cache.ok()
        assertThat(cache.botStatus).isEqualTo(BotStatus.OK)
        assertThat(botStatusChangedListener.numberOfListenerCalls).isEqualTo(2) // one for init. one for status changed
        assertThat(botStatusChangedListener.botStatus).isEqualTo(BotStatus.OK)
    }

    @Test
    internal fun `authentication failed`() {
        cache.authenticationFailed()
        assertThat(cache.botStatus).isEqualTo(BotStatus.AUTHENTICATION_FAILED)
        assertThat(botStatusChangedListener.numberOfListenerCalls).isEqualTo(2) // one for init. one for status changed
        assertThat(botStatusChangedListener.botStatus).isEqualTo(BotStatus.AUTHENTICATION_FAILED)
    }

    @Test
    internal fun `registration failed`() {
        cache.registrationFailed()
        assertThat(cache.botStatus).isEqualTo(BotStatus.REGISTRATION_FAILED)
        assertThat(botStatusChangedListener.numberOfListenerCalls).isEqualTo(2) // one for init. one for status changed
        assertThat(botStatusChangedListener.botStatus).isEqualTo(BotStatus.REGISTRATION_FAILED)
    }

    @Test
    internal fun `mark messages as read failed`() {
        cache.markMessagesAsReadFailed()
        assertThat(cache.botStatus).isEqualTo(BotStatus.MARK_MESSAGES_FAILED)
        assertThat(botStatusChangedListener.numberOfListenerCalls).isEqualTo(2) // one for init. one for status changed
        assertThat(botStatusChangedListener.botStatus).isEqualTo(BotStatus.MARK_MESSAGES_FAILED)
    }

    @Test
    internal fun `receive messages failed`() {
        cache.receiveMessagesFailed()
        assertThat(cache.botStatus).isEqualTo(BotStatus.RECEIVE_MESSAGES_FAILED)
        assertThat(botStatusChangedListener.numberOfListenerCalls).isEqualTo(2) // one for init. one for status changed
        assertThat(botStatusChangedListener.botStatus).isEqualTo(BotStatus.RECEIVE_MESSAGES_FAILED)
    }

    @Test
    internal fun `update bot status cache twice with different status calls`() {
        cache.ok()
        cache.receiveMessagesFailed()
        assertThat(botStatusChangedListener.numberOfListenerCalls).isEqualTo(3) // one for init. two for status changed
    }

    @Test
    internal fun `update bot status cache twice with same status calls`() {
        cache.registrationFailed()
        cache.registrationFailed()
        assertThat(botStatusChangedListener.numberOfListenerCalls).isEqualTo(2) // one for init. one for status changed
    }

    internal class TestBotStatusChangeListener : BotStatusChangedListener {

        var botStatus: BotStatus? = null
        var numberOfListenerCalls = 0

        override fun botStatusChanged(botStatus: BotStatus) {
            this.botStatus = botStatus
            numberOfListenerCalls++;
        }

    }
}