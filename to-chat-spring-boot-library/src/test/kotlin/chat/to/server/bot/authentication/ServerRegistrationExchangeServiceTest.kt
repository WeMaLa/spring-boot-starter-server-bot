package chat.to.server.bot.authentication

import chat.to.server.bot.configuration.Bot
import chat.to.server.bot.configuration.Server
import chat.to.server.bot.configuration.WeMaLaConfiguration
import org.assertj.core.api.Assertions
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [LastBotStatusForTesting::class])
internal class ServerRegistrationExchangeServiceTest {

    @Autowired
    private lateinit var lastBotStatusForTesting: LastBotStatusForTesting

    private val restTemplate = RestTemplateBuilder().build()
    private val server = MockRestServiceServer.bindTo(restTemplate).build()
    private lateinit var service: ServerRegistrationExchangeService

    @BeforeEach
    fun setUp() {
        val configuration = WeMaLaConfiguration(Bot("unit@test.bot", "unit-test-bot-password", "unit-test-bot-username"), Server("http://server.unit.test/"))
        service = ServerRegistrationExchangeService(configuration, lastBotStatusForTesting, restTemplate)
        lastBotStatusForTesting.clear()
    }

    @Test
    fun `register light bot on wemala server`() {
        server.expect(MockRestRequestMatchers.requestTo("http://server.unit.test/api/user"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("email", IsEqual.equalTo<String>("unit@test.bot")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("password", IsEqual.equalTo<String>("unit-test-bot-password")))
                .andExpect(MockRestRequestMatchers.jsonPath<String>("username", IsEqual.equalTo<String>("unit-test-bot-username")))
                .andRespond(MockRestResponseCreators.withSuccess())

        Assertions.assertThat(service.registerBot()).isTrue()
        Assertions.assertThat(lastBotStatusForTesting.lastBotStatus).isNull()

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

        Assertions.assertThat(service.registerBot()).isFalse()
        Assertions.assertThat(lastBotStatusForTesting.lastBotStatus).isEqualTo(BotStatus.REGISTRATION_FAILED)

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

        Assertions.assertThat(service.registerBot()).isFalse()
        Assertions.assertThat(lastBotStatusForTesting.lastBotStatus).isEqualTo(BotStatus.REGISTRATION_FAILED)

        server.verify()
    }
}