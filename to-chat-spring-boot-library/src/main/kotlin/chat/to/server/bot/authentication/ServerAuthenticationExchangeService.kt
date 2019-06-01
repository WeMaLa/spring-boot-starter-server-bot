package chat.to.server.bot.authentication

import chat.to.server.bot.configuration.WeMaLaConfiguration
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate

class ServerAuthenticationExchangeService(private var botConfiguration: WeMaLaConfiguration,
                                          private var restTemplate: RestTemplate,
                                          private var botStatusChangedListener: BotStatusChangedListener,
                                          private var serverRegistrationExchangeService: ServerRegistrationExchangeService) {

    private val log = LoggerFactory.getLogger(ServerAuthenticationExchangeService::class.java)

    fun authenticate(): String? {
        return try {
            return authenticate(botConfiguration.bot.identifier, botConfiguration.bot.password)
        } catch (e: Exception) {
            if (e is HttpStatusCodeException) {
                log.error("Authenticaton bot on wemala server '${botConfiguration.server.url}/api/auth/login' failed with code '${e.statusCode}' and message '${e.message}'")

                if (e.statusCode == HttpStatus.UNAUTHORIZED) {
                    log.info("Received UNAUTHORIZED while authentication. Register a new bot")
                    if (serverRegistrationExchangeService.registerBot()) {
                        try {
                            return authenticate(botConfiguration.bot.identifier, botConfiguration.bot.password)
                        } catch (e: HttpClientErrorException) {
                            log.error("Second authenticaton bot on wemala server failed with code '${e.statusCode}' and message '${e.message}'")
                        }
                    }
                }
            } else {
                log.error("Authenticaton bot on wemala server failed with message '${e.message}'")
            }

            botStatusChangedListener.botStatusChanged(BotStatus.AUTHENTICATION_FAILED)
            null
        } catch (e: ResourceAccessException) {
            log.error("Authenticaton bot on wemala server failed with message '${e.message}'")
            botStatusChangedListener.botStatusChanged(BotStatus.AUTHENTICATION_FAILED)
            null
        }
    }

    private fun authenticate(identifier: String, password: String): String {
        val httpEntity = HttpEntity<Any>(UserAuthenticationRequest(identifier, password))
        val exchange = restTemplate.exchange(botConfiguration.server.url + "/api/auth/login", HttpMethod.POST, httpEntity, JwtAuthenticationResponse::class.java)
        return exchange.body!!.token
    }

    data class UserAuthenticationRequest internal constructor(val identifier: String, val password: String)

    class JwtAuthenticationResponse {
        var token: String = ""
    }
}