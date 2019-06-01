package de.larmic.sample.bot

import chat.to.server.bot.Message
import chat.to.server.bot.MessageSender
import chat.to.server.bot.autoconfigure.MessageReceiver
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class GreeterSampleApplication {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun receiver(messageSender: MessageSender) = object : MessageReceiver {
        override fun receiveMessage(message: Message) {
            log.info("Message '${message.text}' from '${message.createDate}' received")
            messageSender.sendMessage(message.links.channel.identifier, "Hi there! Message '${message.text}' received!")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<GreeterSampleApplication>(*args)
}