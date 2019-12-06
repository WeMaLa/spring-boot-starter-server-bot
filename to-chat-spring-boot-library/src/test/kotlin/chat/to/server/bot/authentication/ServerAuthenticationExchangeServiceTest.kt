package chat.to.server.bot.authentication

import chat.to.server.bot.cache.BotStatusCache
import chat.to.server.bot.configuration.Bot
import chat.to.server.bot.configuration.Server
import chat.to.server.bot.configuration.WeMaLaConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators

@ExtendWith(SpringExtension::class)
@SpringBootTest
internal class ServerAuthenticationExchangeServiceTest {

    private val restTemplate = RestTemplateBuilder().build()
    private val server = MockRestServiceServer.bindTo(restTemplate).build()
    private val serverRegistrationExchangeServiceMock = mockk<ServerRegistrationExchangeService>()
    private val botStatusCache = BotStatusCache(mockk(relaxed = true))
    private lateinit var service: ServerAuthenticationExchangeService

    @BeforeEach
    fun setUp() {
        val configuration = WeMaLaConfiguration(Bot("unit@test.bot", "unit-test-bot-password", "unit-test-bot-alias"), Server("http://server.unit.test/"))
        service = ServerAuthenticationExchangeService(configuration, restTemplate, botStatusCache, serverRegistrationExchangeServiceMock)
    }

    @Test
    fun `authenticate light bot on wemala server`() {
        val response = ServerAuthenticationExchangeService.JwtAuthenticationResponse()
        response.token = "unit-test-auth-token"
        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/auth/login"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("identifier", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andRespond(MockRestResponseCreators.withSuccess(ObjectMapper().writeValueAsString(response), MediaType.APPLICATION_JSON))

        assertThat(service.authenticate()).isEqualTo("unit-test-auth-token")
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.STARTING)

        server.verify()
    }

    @Test
    fun `authenticate light bot on wemala server and servers first time responds unauthorized`() {
        val response = ServerAuthenticationExchangeService.JwtAuthenticationResponse()
        response.token = "unit-test-auth-token"
        every { serverRegistrationExchangeServiceMock.registerBot() } returns true

        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/auth/login"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("identifier", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andRespond(MockRestResponseCreators.withUnauthorizedRequest())

        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/auth/login"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("identifier", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andRespond(MockRestResponseCreators.withSuccess(ObjectMapper().writeValueAsString(response), MediaType.APPLICATION_JSON))

        assertThat(service.authenticate()).isEqualTo("unit-test-auth-token")
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.STARTING)

        server.verify()
        verify { serverRegistrationExchangeServiceMock.registerBot() }
    }

    @Test
    fun `authenticate light bot on wemala server and servers responds unauthorized and registration failed too`() {
        val response = ServerAuthenticationExchangeService.JwtAuthenticationResponse()
        response.token = "unit-test-auth-token"
        every { serverRegistrationExchangeServiceMock.registerBot() } returns false

        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/auth/login"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("identifier", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andRespond(MockRestResponseCreators.withUnauthorizedRequest())

        assertThat(service.authenticate()).isNull()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.AUTHENTICATION_FAILED)

        server.verify()
        verify { serverRegistrationExchangeServiceMock.registerBot() }
    }

    @Test
    fun `authenticate light bot on wemala server and servers responds bad request`() {
        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/auth/login"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("identifier", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andRespond(MockRestResponseCreators.withBadRequest())

        assertThat(service.authenticate()).isNull()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.AUTHENTICATION_FAILED)

        server.verify()
    }

    @Test
    fun `authenticate light bot on wemala server and servers responds conflict`() {
        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/auth/login"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("identifier", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.CONFLICT))

        assertThat(service.authenticate()).isNull()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.AUTHENTICATION_FAILED)

        server.verify()
    }

}