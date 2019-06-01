package chat.to.server.bot.configuration

class WeMaLaConfiguration(val bot: Bot,
                          val server: Server)

class Server(val url: String)

class Bot(val identifier: String,
          val password: String,
          val username: String)