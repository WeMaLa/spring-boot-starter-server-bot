package chat.to.server.bot

import chat.to.server.bot.authentication.ServerAuthenticationExchangeService
import chat.to.server.bot.configuration.WeMaLaConfiguration
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

class MessageSender(private var botConfiguration: WeMaLaConfiguration,
                    private var restTemplate: RestTemplate,
                    private var serverAuthenticationExchangeService: ServerAuthenticationExchangeService) {

    private val log = LoggerFactory.getLogger(MessageSender::class.java)

    fun sendMessage(channelIdentifier: String, message: String) {
        val token = serverAuthenticationExchangeService.authenticate()

        if (channelIdentifier.isBlank()) {
            log.warn("Could not send message '$message' to channel because channel identifier is blank")
        } else if (message.isBlank()) {
            log.warn("Could not send blank message to channel '$channelIdentifier")
        } else {
            if (token != null) {
                val url = botConfiguration.server.url + "/api/message"
                val httpEntity = createHttpEntity(token, SendMessageRequestBody(message, channelIdentifier))
                try {
                    restTemplate.exchange(url, HttpMethod.POST, httpEntity, Void::class.java)
                } catch (e: Exception) {
                    log.error("Could not send message '$message' to channel '$channelIdentifier' because of an exception", e)
                }
            } else {
                log.error("Could not send message '$message' to channel '$channelIdentifier' because authentication failed")
            }
        }
    }

    private fun createHttpEntity(token: String?, body: Any? = null): HttpEntity<Any> {
        val httpHeaders = HttpHeaders()
        httpHeaders.set("Authorization", token)
        return if (body != null) HttpEntity(body, httpHeaders) else HttpEntity(httpHeaders)
    }

    data class SendMessageRequestBody(val text: String, val channelIdentifier: String)

}