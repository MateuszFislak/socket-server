package com.interview.collibra.messageserver.command;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Consumer;

public class GreetClientCommandHandler implements CommandHandler {

    private final String GREETING_MESSAGE_PATTERN = "HI {0}";
    private final Consumer<String> messageSender;

    public GreetClientCommandHandler(Consumer<String> messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public void handle(List<String> params) {
        final String message = MessageFormat.format(GREETING_MESSAGE_PATTERN, params.get(0));
        messageSender.accept(message);
    }
}
