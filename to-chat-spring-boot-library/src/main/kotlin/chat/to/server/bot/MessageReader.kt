package chat.to.server.bot

import chat.to.server.bot.authentication.BotStatus
import chat.to.server.bot.authentication.BotStatusChangedListener
import chat.to.server.bot.authentication.ServerAuthenticationExchangeService
import chat.to.server.bot.cache.LastReceivedMessagesCache
import chat.to.server.bot.configuration.WeMaLaConfiguration
import chat.to.server.bot.mapper.parseISO8601Date
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

class MessageReader(private var botConfiguration: WeMaLaConfiguration,
                    private var restTemplate: RestTemplate,
                    private var botStatusChangedListener: BotStatusChangedListener,
                    private var serverAuthenticationExchangeService: ServerAuthenticationExchangeService,
                    private var lastReceivedMessagesCache: LastReceivedMessagesCache) {

    private val log = LoggerFactory.getLogger(MessageReader::class.java)

    fun retrieveMessages(): List<Message> {
        val token = serverAuthenticationExchangeService.authenticate()

        return if (token != null) {
            val httpEntity = createHttpEntity(token)
            val messages = loadUnreadMessages(httpEntity)

            return messages
                    .distinctBy { it.identifier }
                    .filter { m -> !lastReceivedMessagesCache.containsMessageIdentifier(m.identifier) }
                    .onEach { m -> lastReceivedMessagesCache.addMessageIdentifierToCache(m.identifier) }
                    .onEach { m -> markAsRead(m.identifier, httpEntity) }
        } else {
            emptyList()
        }
    }

    private fun markAsRead(messageIdentifier: String, httpEntity: HttpEntity<Any>) {
        try {
            val url = botConfiguration.server.url + "/api/message/$messageIdentifier/read"
            restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, String::class.java)
            botStatusChangedListener.botStatusChanged(BotStatus.OK)
        } catch (e: Exception) {
            if (e is HttpStatusCodeException) {
                log.error("Mark message '$messageIdentifier' as read on wemala server failed with code '${e.statusCode}' and message '${e.message}'")
            } else {
                log.error("Mark message '$messageIdentifier' as read on wemala server failed with message '${e.message}'")
            }
            botStatusChangedListener.botStatusChanged(BotStatus.MARK_MESSAGES_FAILED)
        }
    }

    private fun loadUnreadMessages(httpEntity: HttpEntity<Any>): List<Message> {
        return try {
            val url = botConfiguration.loadMessagesUrl()
            log.debug("Retrieve messages from $url")
            val response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, MessageResponse::class.java)
            lastReceivedMessagesCache.updateLastIso8601ServerDate(response.iso8601Date())
            val messages = response.body?.content!!.asList()
            // TODO only update BotStatus if changed
            botStatusChangedListener.botStatusChanged(BotStatus.OK)
            return messages
        } catch (e: Exception) {
            if (e is HttpStatusCodeException) {
                log.error("Retrieve message from wemala server failed with code '${e.statusCode}' and message '${e.message}'")
            } else {
                log.error("Retrieve message from wemala server failed with message '${e.message}'")
            }
            botStatusChangedListener.botStatusChanged(BotStatus.RECEIVE_MESSAGES_FAILED)
            emptyList()
        }
    }

    private fun WeMaLaConfiguration.loadMessagesUrl() = if (lastReceivedMessagesCache.lastIso8601ServerDateWithBuffer == null) "${this.server.url}/api/messages?status=SEND&status=RECEIVED" else "${this.server.url}/api/messages?status=SEND&status=RECEIVED&lastUpdatedSince=${lastReceivedMessagesCache.lastIso8601ServerDateWithBuffer}"
    private fun ResponseEntity<MessageResponse>.iso8601Date() = this.headers["date-iso8601"]?.get(0).parseISO8601Date()

    private fun createHttpEntity(token: String?, body: Any? = null): HttpEntity<Any> {
        val httpHeaders = HttpHeaders()
        httpHeaders.set("Authorization", token)
        return if (body != null) HttpEntity(body, httpHeaders) else HttpEntity(httpHeaders)
    }

    class MessageResponse {
        var content: Array<Message> = arrayOf()
    }

}