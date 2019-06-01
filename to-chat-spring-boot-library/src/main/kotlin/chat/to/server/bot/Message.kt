package chat.to.server.bot

import chat.to.server.bot.serializer.ISO8601DateTimeDeserializer
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.LocalDateTime

class Message @JsonCreator private constructor(@JsonProperty("identifier") val identifier: String,
                                               @JsonProperty("text") val text: String,
                                               @JsonProperty("createDate") @JsonDeserialize(using = ISO8601DateTimeDeserializer::class) val createDate: LocalDateTime,
                                               @JsonProperty("_links") val links: Links) {
    class Links @JsonCreator private constructor(@JsonProperty("channel") val channel: Link,
                                                 @JsonProperty("sender") val sender: Link) {
        class Link @JsonCreator private constructor(@JsonProperty("href") val href: String) {
            val identifier: String
                get() = this.href.substring(13)
        }
    }
}