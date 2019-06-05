package chat.to.server.bot.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@ContextConfiguration(classes = [BotAutoConfiguration::class])
internal class ToChatBotPropertiesTest {

    @Autowired
    private lateinit var properties: ToChatBotProperties

    @Test
    internal fun `verify properties are injectable`() {
        assertThat(properties).isNotNull
        assertThat(properties.bot.identifier).isEqualTo("unit@test.bot")
        assertThat(properties.bot.alias).isEqualTo("unit-test-bot-alias")
        assertThat(properties.bot.description).isEqualTo("unit-test-bot-description")
        assertThat(properties.bot.password).isEqualTo("unit-test-bot-password")
        assertThat(properties.bot.maxCacheSize).isEqualTo(1000)
        assertThat(properties.server.url).isEqualTo("http://server.unit.test/")
    }
}