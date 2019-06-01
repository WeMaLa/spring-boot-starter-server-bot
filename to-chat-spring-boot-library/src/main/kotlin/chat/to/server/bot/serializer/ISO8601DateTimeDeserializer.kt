package chat.to.server.bot.serializer

import chat.to.server.bot.mapper.parseISO8601Date
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ISO8601DateTimeDeserializer : JsonDeserializer<LocalDateTime>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): LocalDateTime {
        return parser.valueAsString.parseISO8601Date()!!
    }
}