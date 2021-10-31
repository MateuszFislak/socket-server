package com.interview.collibra;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class MessageServer {

    public static final int SESSION_TIMEOUT_SECONDS = 30;
    private final MessagePrinter messagePrinter;
    private final TaskExecutor taskExecutor;
    private final TaskScheduler taskScheduler;
    private ServerSocket serverSocket;


    volatile private ClientSession clientSession;
    volatile private Instant timeOfLastMessage;
    private String clientName;

    public MessageServer(TaskExecutor taskExecutor, MessagePrinter messagePrinter, TaskScheduler taskScheduler) {
        this.taskExecutor = taskExecutor;
        this.messagePrinter = messagePrinter;
        this.taskScheduler = taskScheduler;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(5000);
        startClientSessionAsync();
    }

    private void startClientSessionAsync() {
        taskExecutor.execute(() -> {
            initClientSession();
            startSessionTimeoutListener();
            listenForMessages();
        });
    }

    @SneakyThrows
    private void initClientSession() {
        Socket clientSocket = serverSocket.accept();
        clientSession = new ClientSession(clientSocket);
        final String message = messagePrinter.getInitialMessage(clientSession.getId());
        sendMessage(message);
    }


    @SneakyThrows
    private void listenForMessages() {
        String message;
        while (clientSession != null && (message = clientSession.read()) != null) {
            timeOfLastMessage = Instant.now();
            log.info("[Server] <- {}", message);
            respondClientMessage(message);
        }
    }

    private void respondClientMessage(String message) throws IOException {
        final Command command = CommandRecognizer.recognize(message);
        switch (command) {
            case CLIENT_GREETING: {
                clientName = message.split(CommandRecognizer.CLIENT_GREETING_MESSAGE)[1];
                String responseMessage = messagePrinter.getGreetingMessage(clientName);
                sendMessage(responseMessage);
                break;
            }
            case UNKNOWN: {
                String responseMessage = messagePrinter.getUnrecognizedCommandMessage();
                sendMessage(responseMessage);
                break;
            }
            case TERMINATION: {
                terminateClientSession();
                startClientSessionAsync();
                break;
            }

        }
    }


    private void startSessionTimeoutListener() {
        taskScheduler.scheduleAtFixedRate(() -> {
            if (clientSession != null) {
                final Duration durationFromLastMessage = Duration.between(timeOfLastMessage, Instant.now());
                if ((durationFromLastMessage.getSeconds() > SESSION_TIMEOUT_SECONDS)) {
                    terminateClientSession();
                    startClientSessionAsync();
                }
            }
        }, Duration.ofMillis(500));
    }

    @SneakyThrows
    private void terminateClientSession(){
        String responseMessage = messagePrinter.getFarewellMessage(clientName, clientSession.getStartTime());
        sendMessage(responseMessage);
        closeClient();
    }

    public void sendMessage(String message) {
        log.info("[Server] -> {}", message);
        clientSession.write(message);
    }

    public void stop() throws IOException {
        closeClient();
        closeServer();
    }

    private void closeClient() throws IOException {
        clientSession.close();
        clientSession = null;
        clientName = null;
    }

    private void closeServer() throws IOException {
        serverSocket.close();
    }
}
