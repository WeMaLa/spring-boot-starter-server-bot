package chat.to.server.bot.authentication

import org.springframework.stereotype.Component

@Component
class LastBotStatusForTesting : BotStatusChangedListener {
    final var lastBotStatus: BotStatus? = null
        private set

    override fun botStatusChanged(botStatus: BotStatus) {
        lastBotStatus = botStatus
    }

    fun clear() {
        lastBotStatus = null
    }

}