package chat.to.server.bot.mapper

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String?.parseISO8601Date(): LocalDateTime? {
    if (this == null || this.isBlank()) {
        return null
    }

    val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
    return LocalDateTime.parse(this.trim { it <= ' ' }, timeFormatter)
}

fun LocalDateTime.formatUTCDateToISO8601() = DateTimeFormatter.ISO_DATE_TIME.format(this) + "-00:00"