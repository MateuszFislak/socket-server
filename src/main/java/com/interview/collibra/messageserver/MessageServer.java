package com.interview.collibra.messageserver;

import com.interview.collibra.messageserver.model.ClientSession;
import com.interview.collibra.messageserver.model.Command;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@Slf4j
public class MessageServer {

    public static final int SESSION_TIMEOUT_SECONDS = 30;
    private final MessagePrinter messagePrinter;
    private final CommandRecognizer commandRecognizer;
    private final AsyncTaskExecutor asyncTaskExecutor;
    private final GraphService graphService;
    private ServerSocket serverSocket;
    private ClientSession clientSession;
    private String clientName;

    public MessageServer(MessagePrinter messagePrinter,
                         CommandRecognizer commandRecognizer, @Qualifier("messageServerTaskExecutor") AsyncTaskExecutor asyncTaskExecutor, GraphService graphService) {
        this.messagePrinter = messagePrinter;
        this.commandRecognizer = commandRecognizer;
        this.asyncTaskExecutor = asyncTaskExecutor;
        this.graphService = graphService;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(50000);
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
        final Pair<Command, List<String>> commandWithParams = commandRecognizer.recognize(message);
        final Command command = commandWithParams.getLeft();
        final List<String> params = commandWithParams.getRight();

        switch (command) {
            case CLIENT_GREETING: {
                clientName = params.get(0);
                String responseMessage = messagePrinter.getGreetingMessage(clientName);
                sendMessage(responseMessage);
                break;
            }
            case ADD_NODE: {
                final String nodeName = params.get(0);
                final boolean success = graphService.addNode(nodeName);
                if (success) {
                    sendMessage(messagePrinter.NODE_ADDED);
                } else {
                    sendMessage(messagePrinter.NODE_EXISTS);
                }
                break;
            }
            case ADD_EDGE: {
                final String firstEdge = params.get(0);
                final String secondEdge = params.get(1);
                final Integer weight = Integer.valueOf(params.get(2));
                final boolean success = graphService.addEdge(firstEdge, secondEdge, weight);
                if (success) {
                    sendMessage(messagePrinter.EDGE_ADDED);
                } else {
                    sendMessage(messagePrinter.NODE_NOT_FOUND);
                }
                break;
            }
            case REMOVE_NODE: {
                final String nodeName = params.get(0);
                final boolean success = graphService.removeNode(nodeName);
                if (success) {
                    sendMessage(messagePrinter.NODE_REMOVED);
                } else {
                    sendMessage(messagePrinter.NODE_NOT_FOUND);
                }
                break;
            }
            case REMOVE_EDGE: {
                final String firstEdge = params.get(0);
                final String secondEdge = params.get(1);
                final boolean success = graphService.removeEdge(firstEdge, secondEdge);
                if (success) {
                    sendMessage(messagePrinter.EDGE_REMOVED);
                } else {
                    sendMessage(messagePrinter.NODE_NOT_FOUND);
                }
                break;
            }
            case SHORTEST_PATH: {
                final String firstNode = params.get(0);
                final String secondNode = params.get(1);
                final Optional<Integer> shortestPath = graphService.findShortestPath(firstNode, secondNode);
                shortestPath.map(Object::toString).ifPresentOrElse(this::sendMessage, () -> {
                    sendMessage(messagePrinter.NODE_NOT_FOUND);
                });
                break;
            }
            case UNKNOWN: {
                sendMessage(messagePrinter.UNRECOGNIZABLE_COMMAND_MESSAGE);
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
