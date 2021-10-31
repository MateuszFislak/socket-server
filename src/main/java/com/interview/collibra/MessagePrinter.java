package com.interview.collibra;

import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class MessagePrinter {

    private final String FAREWELL_FORMAT_PATTERN = "BYE {0}, WE SPOKE FOR {1} MS";
    private final String UNRECOGNIZABLE_COMMAND_MESSAGE = "SORRY, I DID NOT UNDERSTAND THAT";
    private final String INITIAL_MESSAGE_FORMAT_PATTERN = "HI, I AM {0}";
    private final String GREETING_MESSAGE_PATTERN = "HI {0}";

    public String getInitialMessage(UUID id) {
        return MessageFormat.format(INITIAL_MESSAGE_FORMAT_PATTERN, id);
    }

    public String getGreetingMessage(String recipient) {
        return MessageFormat.format(GREETING_MESSAGE_PATTERN, recipient);
    }

    public String getUnrecognizedCommandMessage() {
        return UNRECOGNIZABLE_COMMAND_MESSAGE;
    }

    public String getFarewellMessage(String recipient, Instant conversationStart) {
        final Duration duration = Duration.between(conversationStart, Instant.now());
        return MessageFormat.format(FAREWELL_FORMAT_PATTERN, recipient, duration.toMillis());
    }
}
