package chat.to.server.bot.cache

import chat.to.server.bot.authentication.BotStatus
import chat.to.server.bot.authentication.BotStatusChangedListener
import org.slf4j.LoggerFactory

class BotStatusCache(private val botStatusChangedListener: BotStatusChangedListener) {

    private val log = LoggerFactory.getLogger(this::class.java)

    private var status: BotStatus = BotStatus.STARTING
        set(value) {
            if (field != value) {
                field = value
                botStatusChangedListener.botStatusChanged(status)
            }
        }

    init {
        log.debug("Initializing BotStatusCache with $status")
        botStatusChangedListener.botStatusChanged(status)
    }

    val botStatus: BotStatus
        get() = status

    fun ok() {
        status = BotStatus.OK
    }

    fun authenticationFailed() {
        status = BotStatus.AUTHENTICATION_FAILED
    }

    fun registrationFailed() {
        status = BotStatus.REGISTRATION_FAILED
    }

    fun markMessagesAsReadFailed() {
        status = BotStatus.MARK_MESSAGES_FAILED
    }

    fun receiveMessagesFailed() {
        status = BotStatus.RECEIVE_MESSAGES_FAILED
    }

}