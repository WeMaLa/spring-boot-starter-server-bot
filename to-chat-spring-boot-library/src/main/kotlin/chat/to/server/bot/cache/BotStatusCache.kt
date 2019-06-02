package chat.to.server.bot.cache

import chat.to.server.bot.authentication.BotStatus

class BotStatusCache {

    private var status: BotStatus = BotStatus.STARTING

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