package chat.to.server.bot.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDateTime
import java.time.Month

internal class Iso8601DateMapperTest {

    @Nested
    @DisplayName("map String to LocalDateTime with")
    inner class StringToLocalDateTime {

        @Test
        internal fun `all is fine`() {
            val localDateTime = "2019-05-25T20:18:55-00:00".parseISO8601Date()!!

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
        internal fun spaces(dateAsString: String) {
            val localDateTime = dateAsString.parseISO8601Date()!!

            assertThat(localDateTime.year).isEqualTo(2019)
            assertThat(localDateTime.month).isEqualTo(Month.MAY)
            assertThat(localDateTime.dayOfMonth).isEqualTo(25)
            assertThat(localDateTime.hour).isEqualTo(20)
            assertThat(localDateTime.minute).isEqualTo(18)
            assertThat(localDateTime.second).isEqualTo(55)
        }

        @Test
        internal fun `value is null`() {
            assertThat(null.parseISO8601Date()).isNull()
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "  ",
            "     "])
        internal fun `value is blank`(dateAsString: String) {
            assertThat(dateAsString.parseISO8601Date()).isNull()
        }
    }

    @Nested
    @DisplayName("map LocalDateTime to String with")
    inner class LocalDateTimeToString {

        @Test
        internal fun `all is fine`() {
            val date = LocalDateTime.of(2019, Month.FEBRUARY, 12, 11, 13, 15, 16)

            assertThat(date.formatUTCDateToISO8601()).isEqualTo("2019-02-12T11:13:15.000000016-00:00")
        }
    }
}