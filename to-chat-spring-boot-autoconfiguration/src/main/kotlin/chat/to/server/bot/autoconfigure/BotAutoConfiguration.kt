package chat.to.server.bot.autoconfigure

import chat.to.server.bot.Message
import chat.to.server.bot.MessageSender
import chat.to.server.bot.authentication.ServerAuthenticationExchangeService
import chat.to.server.bot.authentication.ServerRegistrationExchangeService
import chat.to.server.bot.autoconfigure.mapper.toWeMaLaConfiguration
import chat.to.server.bot.autoconfigure.properties.ToChatBotProperties
import chat.to.server.bot.autoconfigure.scheduling.MessageReceiver
import chat.to.server.bot.cache.BotStatusCache
import chat.to.server.bot.cache.LastReceivedMessagesCache
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
@EnableConfigurationProperties(ToChatBotProperties::class)
class BotAutoConfiguration(private val toChatBotProperties: ToChatBotProperties) {

    private val log = LoggerFactory.getLogger(this::class.java)

    // TODO test me!
    @Bean
    @ConditionalOnMissingBean
    fun receiver() = object : MessageReceiver {
        override fun receiveMessage(message: Message) {
            log.warn("Received message '${message.identifier}' is ignored because chat.to.server.bot.autoconfigure. Receiver bean is not found.")
        }
    }

    // TODO test me!
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    fun lastReceivedMessagesCache() = LastReceivedMessagesCache(toChatBotProperties.bot.maxCacheSize)

    @Bean
    @ConditionalOnMissingBean
    fun serverRegistrationBean(botStatusCache: BotStatusCache) = ServerRegistrationExchangeService(toChatBotProperties.toWeMaLaConfiguration(), botStatusCache, RestTemplateBuilder().build())

    @Bean
    @ConditionalOnMissingBean
    fun serverAuthenticationBean(botStatusCache: BotStatusCache, serverRegistrationExchangeService: ServerRegistrationExchangeService) = ServerAuthenticationExchangeService(toChatBotProperties.toWeMaLaConfiguration(), RestTemplateBuilder().build(), botStatusCache, serverRegistrationExchangeService)

    @Bean
    @ConditionalOnMissingBean
    fun messageSender(serverAuthenticationExchangeService: ServerAuthenticationExchangeService) = MessageSender(toChatBotProperties.toWeMaLaConfiguration(), RestTemplateBuilder().build(), serverAuthenticationExchangeService)

}