package chat.to.server.bot.authentication

import chat.to.server.bot.cache.BotStatusCache
import chat.to.server.bot.configuration.Bot
import chat.to.server.bot.configuration.Server
import chat.to.server.bot.configuration.WeMaLaConfiguration
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.IsEqual.equalTo
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
import org.springframework.test.web.client.response.MockRestResponseCreators.*

@ExtendWith(SpringExtension::class)
@SpringBootTest
@DisplayName("Register bot on server with")
internal class ServerRegistrationExchangeServiceTest {

    private val restTemplate = RestTemplateBuilder().build()
    private val server = MockRestServiceServer.bindTo(restTemplate).build()
    private val botStatusCache = BotStatusCache(mockk(relaxed = true))
    private lateinit var service: ServerRegistrationExchangeService

    @BeforeEach
    fun setUp() {
        val configuration = WeMaLaConfiguration(Bot("unit@test.bot", "unit-test-bot-password", "unit-test-bot-alias", "unit-test-bot-description"), Server("http://server.unit.test/"))
        service = ServerRegistrationExchangeService(configuration, botStatusCache, restTemplate)
    }

    @Test
    fun `all is fine`() {
        server.expect(requestTo("http://server.unit.test/api/bot"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath<String>("identifier", equalTo<String>("unit@test.bot")))
                .andExpect(jsonPath<String>("password", equalTo<String>("unit-test-bot-password")))
                .andExpect(jsonPath<String>("alias", equalTo<String>("unit-test-bot-alias")))
                .andExpect(jsonPath<String>("description", equalTo<String>("unit-test-bot-description")))
                .andRespond(withSuccess())

        assertThat(service.registerBot()).isTrue()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.STARTING)

        server.verify()
    }

    @Test
    internal fun `no description`() {
        val configuration = WeMaLaConfiguration(Bot("unit@test.bot", "unit-test-bot-password", "unit-test-bot-alias"), Server("http://server.unit.test/"))
        val service = ServerRegistrationExchangeService(configuration, botStatusCache, restTemplate)

        server.expect(requestTo("http://server.unit.test/api/bot"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath<String>("identifier", equalTo<String>("unit@test.bot")))
                .andExpect(jsonPath<String>("password", equalTo<String>("unit-test-bot-password")))
                .andExpect(jsonPath<String>("alias", equalTo<String>("unit-test-bot-alias")))
                .andExpect(jsonPath<String>("description", equalTo<String>(null)))
                .andRespond(withSuccess())

        assertThat(service.registerBot()).isTrue()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.STARTING)

        server.verify()
    }

    @Test
    fun `server responds bad request`() {
        server.expect(requestTo("http://server.unit.test/api/bot"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath<String>("identifier", equalTo<String>("unit@test.bot")))
                .andExpect(jsonPath<String>("password", equalTo<String>("unit-test-bot-password")))
                .andExpect(jsonPath<String>("alias", equalTo<String>("unit-test-bot-alias")))
                .andRespond(withBadRequest())

        assertThat(service.registerBot()).isFalse()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.REGISTRATION_FAILED)

        server.verify()
    }

    @Test
    fun `and server responds conflict`() {
        server.expect(requestTo("http://server.unit.test/api/bot"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath<String>("identifier", equalTo<String>("unit@test.bot")))
                .andExpect(jsonPath<String>("password", equalTo<String>("unit-test-bot-password")))
                .andExpect(jsonPath<String>("alias", equalTo<String>("unit-test-bot-alias")))
                .andRespond(withStatus(HttpStatus.CONFLICT))

        assertThat(service.registerBot()).isFalse()
        assertThat(botStatusCache.botStatus).isEqualTo(BotStatus.REGISTRATION_FAILED)

        server.verify()
    }
}