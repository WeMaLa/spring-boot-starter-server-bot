package chat.to.server.bot.authentication

import chat.to.server.bot.cache.BotStatusCache
import chat.to.server.bot.configuration.Bot
import chat.to.server.bot.configuration.Server
import chat.to.server.bot.configuration.WeMaLaConfiguration
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators

@ExtendWith(SpringExtension::class)
@SpringBootTest
internal class ServerRegistrationExchangeServiceTest {

    private val restTemplate = RestTemplateBuilder().build()
    private val server = MockRestServiceServer.bindTo(restTemplate).build()
    private val botStatusCache = BotStatusCache(mock())
    private lateinit var service: ServerRegistrationExchangeService

    @BeforeEach
    fun setUp() {
        val configuration = WeMaLaConfiguration(Bot("unit@test.bot", "unit-test-bot-password", "unit-test-bot-username"), Server("http://server.unit.test/"))
        service = ServerRegistrationExchangeService(configuration, botStatusCache, restTemplate)
    }

    @Test
    fun `register light bot on wemala server`() {
        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/user"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("email", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("username", IsEqual.equalTo<String>("unit-test-bot-username")))
                .andRespond(MockRestResponseCreators.withSuccess())

        assertThat(service.registerBot()).isTrue()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.STARTING)

        server.verify()
    }

    @Test
    fun `register light bot on wemala server and server responds bad request`() {
        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/user"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("email", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("username", IsEqual.equalTo<String>("unit-test-bot-username")))
                .andRespond(MockRestResponseCreators.withBadRequest())

        assertThat(service.registerBot()).isFalse()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.REGISTRATION_FAILED)

        server.verify()
    }

    @Test
    fun `register light bot on wemala server and server responds conflict`() {
        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/user"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("email", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("username", IsEqual.equalTo<String>("unit-test-bot-username")))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.CONFLICT))

        assertThat(service.registerBot()).isFalse()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.REGISTRATION_FAILED)

        server.verify()
    }
}