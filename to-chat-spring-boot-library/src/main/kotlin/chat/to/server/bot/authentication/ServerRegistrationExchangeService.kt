package chat.to.server.bot.authentication

import chat.to.server.bot.cache.BotStatusCache
import chat.to.server.bot.configuration.WeMaLaConfiguration
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

class ServerRegistrationExchangeService(private var botConfiguration: WeMaLaConfiguration,
                                        private var botStatusCache: BotStatusCache,
                                        private var restTemplate: RestTemplate) {

    private val log = LoggerFactory.getLogger(ServerRegistrationExchangeService::class.java)

    fun registerBot(): Boolean {
        log.info("Register new bot on wemala server")
        val httpEntity = HttpEntity<Any>(BotRegistrationRequest(botConfiguration.bot.identifier, botConfiguration.bot.password, botConfiguration.bot.alias, botConfiguration.bot.description))

        return try {
            restTemplate.exchange(botConfiguration.server.url + "/api/bot", HttpMethod.POST, httpEntity, Any::class.java)
            true
        } catch (e: Exception) {
            if (e is HttpStatusCodeException) {
                log.error("Register bot on wemala server failed with code '${e.statusCode}' and message '${e.message}'")
            } else {
                log.error("Register bot on wemala server failed with message '${e.message}'")
            }
            botStatusCache.registrationFailed()
            false
        }
    }

    private data class BotRegistrationRequest(val identifier: String, val password: String, val alias: String, val description: String? = null)

}