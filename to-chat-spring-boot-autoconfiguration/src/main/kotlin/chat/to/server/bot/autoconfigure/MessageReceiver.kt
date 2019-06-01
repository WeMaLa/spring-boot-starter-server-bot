package chat.to.server.bot.autoconfigure

import chat.to.server.bot.Message

interface MessageReceiver {

    fun receiveMessage(message: Message)

}