package chat.to.server.bot.autoconfigure

import chat.to.server.bot.Message
import chat.to.server.bot.MessageReader
import chat.to.server.bot.MessageSender
import chat.to.server.bot.authentication.BotStatus
import chat.to.server.bot.authentication.BotStatusChangedListener
import chat.to.server.bot.authentication.ServerAuthenticationExchangeService
import chat.to.server.bot.authentication.ServerRegistrationExchangeService
import chat.to.server.bot.cache.BotStatusCache
import chat.to.server.bot.cache.LastReceivedMessagesCache
import chat.to.server.bot.configuration.WeMaLaConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@ConditionalOnClass(ToChatBotProperties::class)
@EnableConfigurationProperties(ToChatBotProperties::class)
@EnableScheduling
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
    @ConditionalOnMissingBean
    fun botStatusChangedListener() = object : BotStatusChangedListener {
        override fun botStatusChanged(botStatus: BotStatus) {
            when (botStatus) {
                BotStatus.OK, BotStatus.STARTING -> log.info("Bot status changed to '$botStatus'. To handle this change implement custom bean of BotStatusChangedListener.")
                else -> log.error("Bot status changed to '$botStatus'. To handle this change implement custom bean of BotStatusChangedListener.")
            }
        }
    }

    // TODO test me!
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    fun botStatusCache(botStatusChangedListener: BotStatusChangedListener) = BotStatusCache(botStatusChangedListener)

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

    @Bean
    @ConditionalOnMissingBean
    fun scheduler(botStatusCache: BotStatusCache, messageReceiver: MessageReceiver, serverAuthenticationExchangeService: ServerAuthenticationExchangeService, lastReceivedMessagesCache: LastReceivedMessagesCache) = MessageReceiveScheduler(messageReceiver, MessageReader(toChatBotProperties.toWeMaLaConfiguration(), RestTemplateBuilder().build(), botStatusCache, serverAuthenticationExchangeService, lastReceivedMessagesCache))

    private fun ToChatBotProperties.toWeMaLaConfiguration() = WeMaLaConfiguration(this.bot.toWeMaLaBot(), this.server.toWeMaLaServer())
    private fun Bot?.toWeMaLaBot() = chat.to.server.bot.configuration.Bot(this!!.identifier, this.password, this.username)
    private fun Server?.toWeMaLaServer() = chat.to.server.bot.configuration.Server(this!!.url)

}