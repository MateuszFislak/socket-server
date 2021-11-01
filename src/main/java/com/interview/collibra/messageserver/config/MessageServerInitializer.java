package com.interview.collibra.messageserver.config;

import com.interview.collibra.messageserver.MessageServer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MessageServerInitializer {

    private final MessageServer messageSErver;

    public MessageServerInitializer(MessageServer messageSErver) {
        this.messageSErver = messageSErver;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startSocket() throws IOException {
        messageSErver.start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stopSocket() throws IOException {
        messageSErver.stop();
    }
}
