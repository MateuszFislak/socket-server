package com.interview.collibra.messageserver;

import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class MessagePrinter {

    private final String FAREWELL_FORMAT_PATTERN = "BYE {0}, WE SPOKE FOR {1} MS";
    private final String INITIAL_MESSAGE_FORMAT_PATTERN = "HI, I AM {0}";
    private final String GREETING_MESSAGE_PATTERN = "HI {0}";
    public final String UNRECOGNIZABLE_COMMAND_MESSAGE = "SORRY, I DID NOT UNDERSTAND THAT";
    public final String NODE_ADDED = "NODE ADDED";
    public final String NODE_EXISTS = "ERROR: NODE ALREADY EXISTS";
    public final String EDGE_ADDED = "EDGE ADDED";
    public final String NODE_NOT_FOUND = "ERROR: NODE NOT FOUND";
    public final String NODE_REMOVED = "NODE REMOVED";
    public final String EDGE_REMOVED = "EDGE REMOVED";

    public String getInitialMessage(UUID id) {
        return MessageFormat.format(INITIAL_MESSAGE_FORMAT_PATTERN, id);
    }

    public String getGreetingMessage(String recipient) {
        return MessageFormat.format(GREETING_MESSAGE_PATTERN, recipient);
    }

    public String getFarewellMessage(String recipient, Instant conversationStart) {
        final Duration duration = Duration.between(conversationStart, Instant.now());
        return MessageFormat.format(FAREWELL_FORMAT_PATTERN, recipient, duration.toMillis());
    }
}
