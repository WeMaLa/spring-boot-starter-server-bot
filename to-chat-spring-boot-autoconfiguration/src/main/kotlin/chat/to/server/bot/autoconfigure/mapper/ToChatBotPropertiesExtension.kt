package chat.to.server.bot.autoconfigure.mapper

import chat.to.server.bot.autoconfigure.properties.ToChatBotProperties
import chat.to.server.bot.configuration.WeMaLaConfiguration

fun ToChatBotProperties.toWeMaLaConfiguration() = WeMaLaConfiguration(this.bot.toWeMaLaBot(), this.server.toWeMaLaServer())
private fun ToChatBotProperties.Bot?.toWeMaLaBot() = chat.to.server.bot.configuration.Bot(this!!.identifier, this.password, this.alias)
private fun ToChatBotProperties.Server?.toWeMaLaServer() = chat.to.server.bot.configuration.Server(this!!.url)