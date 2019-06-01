package chat.to.server.bot.authentication

import chat.to.server.bot.configuration.Bot
import chat.to.server.bot.configuration.Server
import chat.to.server.bot.configuration.WeMaLaConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
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
@SpringBootTest(classes = [LastBotStatusForTesting::class])
internal class ServerAuthenticationExchangeServiceTest {

    @Autowired
    private lateinit var lastBotStatusForTesting: LastBotStatusForTesting

    private val restTemplate = RestTemplateBuilder().build()
    private val server = MockRestServiceServer.bindTo(restTemplate).build()
    private val serverRegistrationExchangeServiceMock = mock<ServerRegistrationExchangeService>()
    private lateinit var service: ServerAuthenticationExchangeService

    @BeforeEach
    fun setUp() {
        val configuration = WeMaLaConfiguration(Bot("unit@test.bot", "unit-test-bot-password", "unit-test-bot-username"), Server("http://server.unit.test/"))
        service = ServerAuthenticationExchangeService(configuration, restTemplate, lastBotStatusForTesting, serverRegistrationExchangeServiceMock)
        lastBotStatusForTesting.clear()
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
        assertThat(lastBotStatusForTesting.lastBotStatus).isNull()

        server.verify()
    }

    @Test
    fun `authenticate light bot on wemala server and servers first time responds unauthorized`() {
        val response = ServerAuthenticationExchangeService.JwtAuthenticationResponse()
        response.token = "unit-test-auth-token"
        whenever(serverRegistrationExchangeServiceMock.registerBot()).thenReturn(true)

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
        assertThat(lastBotStatusForTesting.lastBotStatus).isNull()

        server.verify()
        verify(serverRegistrationExchangeServiceMock).registerBot()
    }

    @Test
    fun `authenticate light bot on wemala server and servers responds unauthorized and registration failed too`() {
        val response = ServerAuthenticationExchangeService.JwtAuthenticationResponse()
        response.token = "unit-test-auth-token"
        whenever(serverRegistrationExchangeServiceMock.registerBot()).thenReturn(false)

        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/auth/login"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("identifier", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andRespond(MockRestResponseCreators.withUnauthorizedRequest())

        assertThat(service.authenticate()).isNull()
        assertThat(lastBotStatusForTesting.lastBotStatus).isEqualTo(BotStatus.AUTHENTICATION_FAILED)

        server.verify()
        verify(serverRegistrationExchangeServiceMock).registerBot()
    }

    @Test
    fun `authenticate light bot on wemala server and servers responds bad request`() {
        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/auth/login"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("identifier", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andRespond(MockRestResponseCreators.withBadRequest())

        assertThat(service.authenticate()).isNull()
        assertThat(lastBotStatusForTesting.lastBotStatus).isEqualTo(BotStatus.AUTHENTICATION_FAILED)

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
        assertThat(lastBotStatusForTesting.lastBotStatus).isEqualTo(BotStatus.AUTHENTICATION_FAILED)

        server.verify()
    }

}