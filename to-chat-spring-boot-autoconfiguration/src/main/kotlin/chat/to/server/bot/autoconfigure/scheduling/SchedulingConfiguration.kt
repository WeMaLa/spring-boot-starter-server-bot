package chat.to.server.bot.autoconfigure.scheduling

import chat.to.server.bot.MessageReader
import chat.to.server.bot.authentication.ServerAuthenticationExchangeService
import chat.to.server.bot.autoconfigure.mapper.toWeMaLaConfiguration
import chat.to.server.bot.autoconfigure.properties.ToChatBotProperties
import chat.to.server.bot.cache.BotStatusCache
import chat.to.server.bot.cache.LastReceivedMessagesCache
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@ConditionalOnProperty(
        value = ["chat.to.bot.scheduler.enable"], havingValue = "true", matchIfMissing = true
)
@Configuration
@EnableScheduling
class SchedulingConfiguration(private val toChatBotProperties: ToChatBotProperties) {

    @Bean
    @ConditionalOnMissingBean
    fun scheduler(botStatusCache: BotStatusCache, messageReceiver: MessageReceiver, serverAuthenticationExchangeService: ServerAuthenticationExchangeService, lastReceivedMessagesCache: LastReceivedMessagesCache) = MessageReceiveScheduler(messageReceiver, MessageReader(toChatBotProperties.toWeMaLaConfiguration(), RestTemplateBuilder().build(), botStatusCache, serverAuthenticationExchangeService, lastReceivedMessagesCache))

}