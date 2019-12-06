package chat.to.server.bot.serializer

import com.fasterxml.jackson.core.JsonParser
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Month

internal class ISO8601DateTimeDeserializerTest {

    private val deserializer = ISO8601DateTimeDeserializer()

    @Test
    internal fun `deserialize valid value`() {
        val localDateTime = deserializer.deserialize("2019-05-25T20:18:55-00:00".jsonParserMock(), null)

        assertThat(localDateTime.year).isEqualTo(2019)
        assertThat(localDateTime.month).isEqualTo(Month.MAY)
        assertThat(localDateTime.dayOfMonth).isEqualTo(25)
        assertThat(localDateTime.hour).isEqualTo(20)
        assertThat(localDateTime.minute).isEqualTo(18)
        assertThat(localDateTime.second).isEqualTo(55)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "  2019-05-25T20:18:55-00:00",
        "  2019-05-25T20:18:55-00:00     ",
        "2019-05-25T20:18:55-00:00  "])
    internal fun `deserialize values with spaces`(dateAsString: String) {
        val localDateTime = deserializer.deserialize(dateAsString.jsonParserMock(), null)

        assertThat(localDateTime.year).isEqualTo(2019)
        assertThat(localDateTime.month).isEqualTo(Month.MAY)
        assertThat(localDateTime.dayOfMonth).isEqualTo(25)
        assertThat(localDateTime.hour).isEqualTo(20)
        assertThat(localDateTime.minute).isEqualTo(18)
        assertThat(localDateTime.second).isEqualTo(55)
    }

    // JsonParser constructor is protected. So using a mock.
    private fun String.jsonParserMock(): JsonParser {
        val that = this
        return mockk {
            every { valueAsString } returns  that
        }
    }
}