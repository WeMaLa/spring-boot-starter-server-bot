package chat.to.server.bot.autoconfigure

import chat.to.server.bot.Message
import chat.to.server.bot.MessageReader
import chat.to.server.bot.autoconfigure.scheduling.MessageReceiveScheduler
import chat.to.server.bot.autoconfigure.scheduling.MessageReceiver
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MessageReceiveSchedulerTest {

    private val receiver = MyCustomMessageReceiver()
    private val messageReaderMock = mock<MessageReader>()
    private val scheduler = MessageReceiveScheduler(receiver, messageReaderMock)

    @Test
    internal fun schedule() {
        val message1 = createMessage("test-message-1")
        val message2 = createMessage("test-message-2")

        whenever(messageReaderMock.retrieveMessages())
                .thenReturn(listOf(message1, message2))

        scheduler.scheduleReceiveMessages()

        assertThat(receiver.messages)
                .extracting(Message::identifier.name)
                .containsExactlyInAnyOrder("test-message-1", "test-message-2")
    }

    private fun createMessage(identifier: String) = mock<Message>() {
        on { this.identifier } doReturn identifier
    }

    class MyCustomMessageReceiver : MessageReceiver {
        val messages = mutableListOf<Message>()

        override fun receiveMessage(message: Message) {
            messages.add(message)
        }

    }

}