package de.larmic.sample.bot;

import chat.to.server.bot.MessageSender;
import chat.to.server.bot.autoconfigure.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SampleApplication {

    private final Logger log = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    @Bean
    public MessageReceiver receiver(final MessageSender messageExchangeService) {
        return message -> {
            log.info("Message '{}' from '{}' received", message.getText(), message.getCreateDate());
            messageExchangeService.sendMessage(message.getLinks().getChannel().getIdentifier(), "Hi there! Message '${message.text}' received! ¯\\_(ツ)_/¯");
        };
    }
}