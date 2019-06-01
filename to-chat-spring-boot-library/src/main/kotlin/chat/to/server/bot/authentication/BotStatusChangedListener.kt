package chat.to.server.bot.authentication

interface BotStatusChangedListener {

    fun botStatusChanged(botStatus: BotStatus)

}