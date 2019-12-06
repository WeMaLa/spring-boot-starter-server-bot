package chat.to.server.bot

import chat.to.server.bot.authentication.ServerAuthenticationExchangeService
import chat.to.server.bot.configuration.Bot
import chat.to.server.bot.configuration.Server
import chat.to.server.bot.configuration.WeMaLaConfiguration
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.core.Is
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus

@ExtendWith(SpringExtension::class)
@SpringBootTest
@DisplayName("Send message with")
internal class MessageSenderTest {

    private val restTemplate = RestTemplateBuilder().build()
    private val server = MockRestServiceServer.bindTo(restTemplate).build()
    private val serverAuthenticationExchangeServiceMock = mockk<ServerAuthenticationExchangeService>()
    private lateinit var service: MessageSender

    @BeforeEach
    fun setUp() {
        val configuration = WeMaLaConfiguration(Bot("unit@test.bot", "unit-test-bot-password", "unit-test-bot-alias"), Server("http://server.unit.test/"))
        service = MessageSender(configuration, restTemplate, serverAuthenticationExchangeServiceMock)
    }

    @Test
    fun `all is fine`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns "unit-test-auth-token"

        val messageContent = "unit-test-message-text"
        val channelIdentifier = "unit-test-channel-identifier"

        server.expect(requestTo("http://server.unit.test/api/message"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "unit-test-auth-token"))
                .andExpect(jsonPath("text", Is.`is`(messageContent)))
                .andExpect(jsonPath("channelIdentifier", Is.`is`(channelIdentifier)))
                .andRespond(withStatus(HttpStatus.OK))

        service.sendMessage(channelIdentifier, messageContent)

        server.verify()
    }

    @Test
    fun `authentication fails`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns null

        service.sendMessage("unit-test-channel-identifier", "unit-test-message-text")

        server.verify() // no server call
    }

    @Test
    fun `channel identifier is empty`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns "unit-test-auth-token"

        service.sendMessage("", "unit-test-message-text")

        server.verify() // no server call
    }

    @Test
    fun `message content is empty`() {
        every { serverAuthenticationExchangeServiceMock.authenticate() } returns "unit-test-auth-token"

        service.sendMessage("unit-test-channel-identifier", "")

        server.verify() // no server call
    }
}