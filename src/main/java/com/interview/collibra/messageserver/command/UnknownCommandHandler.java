package com.interview.collibra.messageserver.command;

import java.util.List;
import java.util.function.Consumer;

public class UnknownCommandHandler implements CommandHandler {

    private final String UNRECOGNIZABLE_COMMAND_MESSAGE = "SORRY, I DID NOT UNDERSTAND THAT";
    private final Consumer<String> messageSender;

    public UnknownCommandHandler(Consumer<String> messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public void handle(List<String> params) {
        messageSender.accept(UNRECOGNIZABLE_COMMAND_MESSAGE);
    }
}
