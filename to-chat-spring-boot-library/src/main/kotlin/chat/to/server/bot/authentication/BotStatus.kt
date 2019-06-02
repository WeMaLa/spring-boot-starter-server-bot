package chat.to.server.bot.authentication

enum class BotStatus {

    OK,
    STARTING,
    AUTHENTICATION_FAILED,
    REGISTRATION_FAILED,
    RECEIVE_MESSAGES_FAILED,
    MARK_MESSAGES_FAILED

}