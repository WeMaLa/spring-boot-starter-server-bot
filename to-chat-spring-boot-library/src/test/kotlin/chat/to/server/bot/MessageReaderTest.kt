package chat.to.server.bot

import chat.to.server.bot.authentication.BotStatus
import chat.to.server.bot.authentication.ServerAuthenticationExchangeService
import chat.to.server.bot.cache.BotStatusCache
import chat.to.server.bot.cache.LastReceivedMessagesCache
import chat.to.server.bot.configuration.Bot
import chat.to.server.bot.configuration.Server
import chat.to.server.bot.configuration.WeMaLaConfiguration
import chat.to.server.bot.mapper.formatUTCDateToISO8601
import chat.to.server.bot.mapper.parseISO8601Date
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.ExpectedCount.once
import org.springframework.test.web.client.ExpectedCount.twice
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import java.time.temporal.ChronoUnit

@ExtendWith(SpringExtension::class)
@SpringBootTest
@DisplayName("Retrieve messages with")
internal class MessageReaderTest {

    private val restTemplate = RestTemplateBuilder().build()
    private val server = MockRestServiceServer.bindTo(restTemplate).build()
    private val serverAuthenticationExchangeServiceMock = mockk<ServerAuthenticationExchangeService>()
    private val botStatusCache = BotStatusCache(mockk(relaxed = true))
    private val lastReceivedMessagesCache = LastReceivedMessagesCache(100)
    private lateinit var reader: MessageReader


    @BeforeEach
    fun setUp() {
        val configuration = WeMaLaConfiguration(Bot("unit@test.bot", "unit-test-bot-password", "unit-test-bot-alias"), Server("http://server.unit.test/"))
        reader = MessageReader(configuration, restTemplate, botStatusCache, serverAuthenticationExchangeServiceMock, lastReceivedMessagesCache)
    }

    @Test
    fun `lastIso8601ServerDate is not set`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns "unit-test-auth-token"

        val httpHeaders = HttpHeaders()
        httpHeaders.set("content-type", "application/json;charset=UTF-8 ")
        httpHeaders.set("date", "Tue, 12 Dec 2017 19:59:50 GMT")
        httpHeaders.set("date-iso8601", "2017-12-12T19:59:50.099-00:00")
        val response = withStatus(HttpStatus.OK)
                .body(createResponse())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders)
        server.expect(requestTo("http://server.unit.test/api/messages?status=SEND&status=RECEIVED"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)
        server.expect(requestTo("http://server.unit.test/api/message/AWA6_vR3A1S3ubG7cRd1/read"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)
        server.expect(requestTo("http://server.unit.test/api/message/AWA6_o33A1S3ubG7cRdz/read"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)

        val messages = reader.retrieveMessages()
        assertThat(messages)
                .extracting(
                        Message::identifier.name,
                        Message::text.name,
                        Message::createDate.name)
                .containsExactly(
                        tuple("AWA6_vR3A1S3ubG7cRd1", "message2", "2019-05-25T20:42:25-00:00".parseISO8601Date()),
                        tuple("AWA6_o33A1S3ubG7cRdz", "message1", "2019-05-25T20:18:55-00:00".parseISO8601Date()))
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.channel.href).isEqualTo("/api/channel/AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.channel.identifier).isEqualTo("AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.sender.href).isEqualTo("/api/contact/admin@iconect.io")
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.sender.identifier).isEqualTo("admin@iconect.io")
        assertThat(messages.findByIdentifier("AWA6_o33A1S3ubG7cRdz").links.channel.href).isEqualTo("/api/channel/AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages.findByIdentifier("AWA6_o33A1S3ubG7cRdz").links.channel.identifier).isEqualTo("AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages.findByIdentifier("AWA6_o33A1S3ubG7cRdz").links.sender.href).isEqualTo("/api/contact/admin@iconect.io")
        assertThat(messages.findByIdentifier("AWA6_o33A1S3ubG7cRdz").links.sender.identifier).isEqualTo("admin@iconect.io")
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.OK)

        server.verify()
    }

    @Test
    fun `lastIso8601ServerDate is already set`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns "unit-test-auth-token"
        lastReceivedMessagesCache.updateLastIso8601ServerDate("2017-12-12T19:58:50.099-00:00".parseISO8601Date())

        val httpHeaders = HttpHeaders()
        httpHeaders.set("content-type", "application/json;charset=UTF-8 ")
        httpHeaders.set("date", "Tue, 12 Dec 2017 19:59:50 GMT")
        httpHeaders.set("date-iso8601", "2017-12-12T19:59:50.099-00:00")
        val response = withStatus(HttpStatus.OK)
                .body(createResponse())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders)
        server.expect(requestTo("http://server.unit.test/api/messages?status=SEND&status=RECEIVED&lastUpdatedSince=" + "2017-12-12T19:58:50.099-00:00".parseISO8601Date()!!.minus(500L, ChronoUnit.MILLIS).formatUTCDateToISO8601()))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)
        server.expect(requestTo("http://server.unit.test/api/message/AWA6_vR3A1S3ubG7cRd1/read"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)
        server.expect(requestTo("http://server.unit.test/api/message/AWA6_o33A1S3ubG7cRdz/read"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)

        val messages = reader.retrieveMessages()
        assertThat(messages)
                .extracting(
                        Message::identifier.name,
                        Message::text.name,
                        Message::createDate.name)
                .containsExactly(
                        tuple("AWA6_vR3A1S3ubG7cRd1", "message2", "2019-05-25T20:42:25-00:00".parseISO8601Date()),
                        tuple("AWA6_o33A1S3ubG7cRdz", "message1", "2019-05-25T20:18:55-00:00".parseISO8601Date()))
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.channel.href).isEqualTo("/api/channel/AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.channel.identifier).isEqualTo("AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.sender.href).isEqualTo("/api/contact/admin@iconect.io")
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.sender.identifier).isEqualTo("admin@iconect.io")
        assertThat(messages.findByIdentifier("AWA6_o33A1S3ubG7cRdz").links.channel.href).isEqualTo("/api/channel/AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages.findByIdentifier("AWA6_o33A1S3ubG7cRdz").links.channel.identifier).isEqualTo("AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages.findByIdentifier("AWA6_o33A1S3ubG7cRdz").links.sender.href).isEqualTo("/api/contact/admin@iconect.io")
        assertThat(messages.findByIdentifier("AWA6_o33A1S3ubG7cRdz").links.sender.identifier).isEqualTo("admin@iconect.io")
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.OK)

        server.verify()
    }

    @Test
    fun `message is retrieved twice in one response`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns "unit-test-auth-token"

        val httpHeaders = HttpHeaders()
        httpHeaders.set("content-type", "application/json;charset=UTF-8 ")
        httpHeaders.set("date", "Tue, 12 Dec 2017 19:59:50 GMT")
        httpHeaders.set("date-iso8601", "2017-12-12T19:59:50.099-00:00")
        val response = withStatus(HttpStatus.OK)
                .body(createResponseWithSameMessage())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders)
        server.expect(requestTo("http://server.unit.test/api/messages?status=SEND&status=RECEIVED"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)
        server.expect(once(), requestTo("http://server.unit.test/api/message/AWA6_vR3A1S3ubG7cRd1/read"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)

        val messages = reader.retrieveMessages()

        assertThat(messages.size).isEqualTo(1)
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").identifier).isEqualTo("AWA6_vR3A1S3ubG7cRd1")
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").text).isEqualTo("message2")
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").createDate).isEqualTo("2019-05-25T20:42:25-00:00".parseISO8601Date())
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.channel.href).isEqualTo("/api/channel/AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.channel.identifier).isEqualTo("AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.sender.href).isEqualTo("/api/contact/admin@iconect.io")
        assertThat(messages.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.sender.identifier).isEqualTo("admin@iconect.io")
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.OK)

        server.verify()
    }

    @Test
    fun `message is retrieved twice in two response`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns "unit-test-auth-token"

        val httpHeaders = HttpHeaders()
        httpHeaders.set("content-type", "application/json;charset=UTF-8 ")
        httpHeaders.set("date", "Tue, 12 Dec 2017 19:59:50 GMT")
        val response = withStatus(HttpStatus.OK)
                .body(createResponseWithSameMessage())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders)
        server.expect(twice(), requestTo("http://server.unit.test/api/messages?status=SEND&status=RECEIVED"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)
        server.expect(once(), requestTo("http://server.unit.test/api/message/AWA6_vR3A1S3ubG7cRd1/read"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)

        val messages1 = reader.retrieveMessages()
        val messages2 = reader.retrieveMessages()

        assertThat(messages2.size).isEqualTo(0)
        assertThat(messages1.size).isEqualTo(1)
        assertThat(messages1.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").identifier).isEqualTo("AWA6_vR3A1S3ubG7cRd1")
        assertThat(messages1.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").text).isEqualTo("message2")
        assertThat(messages1.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").createDate).isEqualTo("2019-05-25T20:42:25-00:00".parseISO8601Date())
        assertThat(messages1.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.channel.href).isEqualTo("/api/channel/AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages1.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.channel.identifier).isEqualTo("AWA6_ozSA1S3ubG7cRdx")
        assertThat(messages1.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.sender.href).isEqualTo("/api/contact/admin@iconect.io")
        assertThat(messages1.findByIdentifier("AWA6_vR3A1S3ubG7cRd1").links.sender.identifier).isEqualTo("admin@iconect.io")
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.OK)

        server.verify()
    }

    @Test
    fun `server responds bad request`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns "unit-test-auth-token"

        server.expect(requestTo("http://server.unit.test/api/messages?status=SEND&status=RECEIVED"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(MockRestResponseCreators.withBadRequest())

        assertThat(reader.retrieveMessages()).isEmpty()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.RECEIVE_MESSAGES_FAILED)

        server.verify()
    }

    @Test
    fun `mark messages responds bad request`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns "unit-test-auth-token"

        val response = withStatus(HttpStatus.OK)
                .body(createResponse())
                .contentType(MediaType.APPLICATION_JSON)
        server.expect(requestTo("http://server.unit.test/api/messages?status=SEND&status=RECEIVED"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)
        server.expect(requestTo("http://server.unit.test/api/message/AWA6_vR3A1S3ubG7cRd1/read"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)
        server.expect(requestTo("http://server.unit.test/api/message/AWA6_o33A1S3ubG7cRdz/read"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(MockRestResponseCreators.withBadRequest())

        assertThat(reader.retrieveMessages())
                .extracting("identifier", "text")
                .containsExactly(
                        tuple("AWA6_vR3A1S3ubG7cRd1", "message2"),
                        tuple("AWA6_o33A1S3ubG7cRdz", "message1"))
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.MARK_MESSAGES_FAILED)

        server.verify()
    }

    @Test
    fun `authentication fails`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns null

        assertThat(reader.retrieveMessages()).isEmpty()
    }

    @Test
    fun `messages are empty`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns "unit-test-auth-token"

        val httpHeaders = HttpHeaders()
        httpHeaders.set("content-type", "application/json;charset=UTF-8 ")
        httpHeaders.set("date", "Tue, 12 Dec 2017 19:59:50 GMT")
        httpHeaders.set("date-iso8601", "2017-12-12T19:59:50.099-00:00")
        val response = withStatus(HttpStatus.OK)
                .body(createEmptyMessagesResponse())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders)
        server.expect(requestTo("http://server.unit.test/api/messages?status=SEND&status=RECEIVED"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andRespond(response)

        assertThat(reader.retrieveMessages()).isEmpty()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.OK)
    }

    private fun List<Message>.findByIdentifier(identifier: String) = this.first { it.identifier == identifier }

    private fun createResponse(): String {
        return "{\n" +
                "  \"content\": [\n" +
                "    {\n" +
                "      \"identifier\": \"AWA6_vR3A1S3ubG7cRd1\",\n" +
                "      \"text\": \"message2\",\n" +
                "      \"createDate\": \"2019-05-25T20:42:25-00:00\",\n" +
                "      \"status\": \"RECEIVED\",\n" +
                "      \"_links\": {\n" +
                "        \"self\": {\n" +
                "          \"href\": \"/api/message/AWA6_vR3A1S3ubG7cRd1\"\n" +
                "        },\n" +
                "        \"channel\": {\n" +
                "          \"href\": \"/api/channel/AWA6_ozSA1S3ubG7cRdx\"\n" +
                "        },\n" +
                "        \"sender\": {\n" +
                "          \"href\": \"/api/contact/admin@iconect.io\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"identifier\": \"AWA6_o33A1S3ubG7cRdz\",\n" +
                "      \"text\": \"message1\",\n" +
                "      \"createDate\": \"2019-05-25T20:18:55-00:00\",\n" +
                "      \"status\": \"RECEIVED\",\n" +
                "      \"_links\": {\n" +
                "        \"self\": {\n" +
                "          \"href\": \"/api/message/AWA6_o33A1S3ubG7cRdz\"\n" +
                "        },\n" +
                "        \"channel\": {\n" +
                "          \"href\": \"/api/channel/AWA6_ozSA1S3ubG7cRdx\"\n" +
                "        },\n" +
                "        \"sender\": {\n" +
                "          \"href\": \"/api/contact/admin@iconect.io\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"last\": true,\n" +
                "  \"totalElements\": 2,\n" +
                "  \"totalPages\": 1,\n" +
                "  \"first\": true,\n" +
                "  \"sort\": null,\n" +
                "  \"numberOfElements\": 2,\n" +
                "  \"size\": 0,\n" +
                "  \"number\": 0\n" +
                "}"
    }

    private fun createResponseWithSameMessage(): String {
        return "{\n" +
                "  \"content\": [\n" +
                "    {\n" +
                "      \"identifier\": \"AWA6_vR3A1S3ubG7cRd1\",\n" +
                "      \"text\": \"message2\",\n" +
                "      \"createDate\": \"2019-05-25T20:42:25-00:00\",\n" +
                "      \"status\": \"RECEIVED\",\n" +
                "      \"_links\": {\n" +
                "        \"self\": {\n" +
                "          \"href\": \"/api/message/AWA6_vR3A1S3ubG7cRd1\"\n" +
                "        },\n" +
                "        \"channel\": {\n" +
                "          \"href\": \"/api/channel/AWA6_ozSA1S3ubG7cRdx\"\n" +
                "        },\n" +
                "        \"sender\": {\n" +
                "          \"href\": \"/api/contact/admin@iconect.io\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"identifier\": \"AWA6_vR3A1S3ubG7cRd1\",\n" +
                "      \"text\": \"message2\",\n" +
                "      \"createDate\": \"2019-05-25T20:42:25-00:00\",\n" +
                "      \"status\": \"RECEIVED\",\n" +
                "      \"_links\": {\n" +
                "        \"self\": {\n" +
                "          \"href\": \"/api/message/AWA6_vR3A1S3ubG7cRd1\"\n" +
                "        },\n" +
                "        \"channel\": {\n" +
                "          \"href\": \"/api/channel/AWA6_ozSA1S3ubG7cRdx\"\n" +
                "        },\n" +
                "        \"sender\": {\n" +
                "          \"href\": \"/api/contact/admin@iconect.io\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"last\": true,\n" +
                "  \"totalElements\": 2,\n" +
                "  \"totalPages\": 1,\n" +
                "  \"first\": true,\n" +
                "  \"sort\": null,\n" +
                "  \"numberOfElements\": 2,\n" +
                "  \"size\": 0,\n" +
                "  \"number\": 0\n" +
                "}"
    }

    private fun createEmptyMessagesResponse(): String {
        return "{\n" +
                "  \"content\": [],\n" +
                "  \"last\": true,\n" +
                "  \"totalElements\": 3,\n" +
                "  \"totalPages\": 1,\n" +
                "  \"first\": true,\n" +
                "  \"sort\": null,\n" +
                "  \"numberOfElements\": 3,\n" +
                "  \"size\": 0,\n" +
                "  \"number\": 0\n" +
                "}"
    }
}