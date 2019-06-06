package chat.to.server.bot.autoconfigure.scheduling

import chat.to.server.bot.Message

interface MessageReceiver {

    fun receiveMessage(message: Message)

}