package chat.to.server.bot.autoconfigure

import chat.to.server.bot.MessageReader
import org.springframework.scheduling.annotation.Scheduled

class MessageReceiveScheduler(private val messageReceiver: MessageReceiver,
                              private val messageReader: MessageReader) {

    @Scheduled(cron = "\${chat.to.server.polling:*/5 * * * * *}")
    fun scheduleReceiveMessages() {
        messageReader.retrieveMessages().forEach(messageReceiver::receiveMessage)
    }

}