package chat.to.server.bot.autoconfigure.status

import chat.to.server.bot.authentication.BotStatus
import chat.to.server.bot.authentication.BotStatusChangedListener
import chat.to.server.bot.cache.BotStatusCache
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class BotStatusConfiguration {

    private val log = LoggerFactory.getLogger(this::class.java)

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

}