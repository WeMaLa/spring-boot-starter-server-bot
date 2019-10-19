package chat.to.server.bot.autoconfigure.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "chat.to")
@ConstructorBinding
@Validated
class ToChatBotProperties(val bot: Bot, val server: Server) {

    class Server(@NotBlank val url: String)

    class Bot(@NotBlank val identifier: String,
              @NotBlank val password: String,
              @NotBlank val alias: String,
              val description: String? = null,
              val maxCacheSize: Int = 1000)
}