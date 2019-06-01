package chat.to.server.bot.autoconfigure

import org.jetbrains.annotations.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "chat.to")
@Validated
class ToChatBotProperties {

    // TODO replace by constructor parameter when using spring boot 2.2.x
    @NotNull
    var bot: Bot? = null

    // TODO replace by constructor parameter when using spring boot 2.2.x
    @NotNull
    var server: Server? = null
}

class Server {

    // TODO replace by constructor parameter when using spring boot 2.2.x
    @NotBlank
    lateinit var url: String

}

class Bot {

    // TODO replace by constructor parameter when using spring boot 2.2.x
    @NotBlank
    lateinit var identifier: String

    // TODO replace by constructor parameter when using spring boot 2.2.x
    @NotBlank
    lateinit var password: String

    // TODO replace by constructor parameter when using spring boot 2.2.x
    @NotBlank
    lateinit var username: String

    // TODO replace by constructor parameter when using spring boot 2.2.x
    var maxCacheSize: Int = 1000

}