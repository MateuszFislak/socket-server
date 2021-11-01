package com.interview.collibra.messageserver;

import com.interview.collibra.messageserver.model.ClientSession;
import com.interview.collibra.messageserver.model.Command;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.*;

@Service
@Slf4j
public class MessageServer {

    public static final int SESSION_TIMEOUT_SECONDS = 30;
    private final MessagePrinter messagePrinter;
    private final AsyncTaskExecutor asyncTaskExecutor;
    private ServerSocket serverSocket;
    private ClientSession clientSession;
    private String clientName;

    public MessageServer(MessagePrinter messagePrinter,
                         @Qualifier("messageServerTaskExecutor") AsyncTaskExecutor asyncTaskExecutor) {
        this.messagePrinter = messagePrinter;
        this.asyncTaskExecutor = asyncTaskExecutor;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(5000);
        beginClientSessionAsync();
    }

    public void stop() throws IOException {
        closeClient();
        closeServer();
    }

    private void beginClientSessionAsync() {
        asyncTaskExecutor.execute(() -> {
            try {
                initClientSession();
                listenForMessages();
            } catch (TimeoutException e) {
                terminateClientSession();
                beginClientSessionAsync();
            } catch (Exception e) {
                log.error("Unexpected during communication with client", e);
            }
        });
    }

    private void initClientSession() throws IOException {
        Socket clientSocket = serverSocket.accept();
        clientSession = new ClientSession(clientSocket);
        final String message = messagePrinter.getInitialMessage(clientSession.getId());
        sendMessage(message);
    }


    private void listenForMessages() throws ExecutionException, InterruptedException, TimeoutException {
        while (clientSession != null) {
            String message = runWithTimeout(clientSession::read, Duration.ofSeconds(SESSION_TIMEOUT_SECONDS));
            log.info("[Server] <- {}", message);
            respondClientMessage(message);
        }
    }

    private void respondClientMessage(String message) {
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
                beginClientSessionAsync();
                break;
            }
        }
    }

    private void terminateClientSession() {
        String responseMessage = messagePrinter.getFarewellMessage(clientName, clientSession.getStartTime());
        sendMessage(responseMessage);
        closeClient();
    }

    private void sendMessage(String message) {
        log.info("[Server] -> {}", message);
        clientSession.write(message);
    }

    private void closeClient() {
        try {
            clientSession.close();
        } catch (IOException e) {
            log.error("Could not close client session", e);
        }
        clientSession = null;
        clientName = null;
    }

    private void closeServer() throws IOException {
        serverSocket.close();
    }

    private String runWithTimeout(Callable<String> task, Duration timeout) throws ExecutionException, InterruptedException, TimeoutException {
        final Future<String> futureResult = asyncTaskExecutor.submit(task);
        return futureResult.get(timeout.toSeconds(), TimeUnit.SECONDS);
    }

}
