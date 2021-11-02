package com.interview.collibra.messageserver;

import com.interview.collibra.messageserver.command.*;
import com.interview.collibra.messageserver.model.ClientSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
public class MessageServer {

    public static final int SESSION_TIMEOUT_SECONDS = 30;
    private final CommandRecognizer commandRecognizer;
    private final AsyncTaskExecutor asyncTaskExecutor;
    private final GraphService graphService;
    private ServerSocket serverSocket;
    private ClientSession clientSession;
    private String clientName;

    private Map<Command, CommandHandler> handlers;

    public MessageServer(CommandRecognizer commandRecognizer,
                         @Qualifier("messageServerTaskExecutor") AsyncTaskExecutor asyncTaskExecutor,
                         GraphService graphService) {
        this.commandRecognizer = commandRecognizer;
        this.asyncTaskExecutor = asyncTaskExecutor;
        this.graphService = graphService;
    }

    @PostConstruct
    public void initializeHandlersMap() {
        handlers = Map.of(
                Command.GREET_CLIENT, new GreetClientCommandHandler(this::sendMessage),
                Command.ADD_NODE, new AddNodeCommandHandler(this::sendMessage, graphService),
                Command.ADD_EDGE, new AddEdgeCommandHandler(this::sendMessage, graphService),
                Command.REMOVE_NODE, new RemoveNodeCommandHandler(this::sendMessage, graphService),
                Command.REMOVE_EDGE, new RemoveEdgeCommandHandler(this::sendMessage, graphService),
                Command.FIND_SHORTEST_PATH, new FindShortestPathCommandHandler(this::sendMessage, graphService),
                Command.UNKNOWN, new UnknownCommandHandler(this::sendMessage)
        );
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(50000);
        beginClientSessionAsync();
    }

    public void stop() throws IOException {
        closeClient();
        serverSocket.close();
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
        introduceYourself();
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
        if (command == Command.GREET_CLIENT) {
            clientName = params.get(0);
        }
        if (command == Command.TERMINATE) {
            terminateClientSession();
            beginClientSessionAsync();
            return;
        }
        handlers.get(command).handle(params);
    }

    private void terminateClientSession() {
        sendFarewell();
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

    private String runWithTimeout(Callable<String> task, Duration timeout) throws ExecutionException, InterruptedException, TimeoutException {
        final Future<String> futureResult = asyncTaskExecutor.submit(task);
        return futureResult.get(timeout.toSeconds(), TimeUnit.SECONDS);
    }

    private void introduceYourself() {
        final String message = MessageFormat.format("HI, I AM {0}", clientSession.getId());
        sendMessage(message);
    }

    private void sendFarewell() {
        final Duration duration = Duration.between(clientSession.getStartTime(), Instant.now());
        final String message = MessageFormat.format("BYE {0}, WE SPOKE FOR {1} MS", clientName, String.valueOf(duration.toMillis()));
        sendMessage(message);
    }
}
