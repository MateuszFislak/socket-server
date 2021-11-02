package com.interview.collibra.messageserver.command;

import java.util.List;

public interface CommandHandler {
    void handle(List<String> params);
}
