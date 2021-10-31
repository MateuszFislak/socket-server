package com.interview.collibra;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MessageServer {

    public static final String FAREWELL_MESSAGE_PATTERN = "BYE %s, WE SPOKE FOR %s MS";
    public static final String UNRECOGNIZABLE_COMMAND_MESSAGE = "SORRY, I DID NOT UNDERSTAND THAT";
    public static final String INITIAL_MESSAGE_PATTERN = "HI, I AM %s";
    public static final int SESSION_TIMEOUT = 30;
    public static final String TERMINATION_COMMAND = "BYE MATE!";
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Instant timeOfLastMessage;
    private Instant timeOfSessionStart;
    private String clientName;

    public void init() throws IOException {
        serverSocket = new ServerSocket(5000);
        clientSocket = serverSocket.accept();
        timeOfSessionStart = Instant.now();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        sendMessage(String.format(INITIAL_MESSAGE_PATTERN, UUID.randomUUID()));
    }

    public void sendMessage(String message) {
        System.out.printf("[Server] -> %s%n", message);
        out.println(message);
    }


    @Async
    public void listenToMessages() throws IOException {
        String inputLine;
        while (in != null & (inputLine = in.readLine()) != null) {
            timeOfLastMessage = Instant.now();
            System.out.printf("[Server] <- %s%n", inputLine);
            if (inputLine.startsWith("HI, I AM ")) {
                clientName = inputLine.split("HI, I AM ")[1];
                sendMessage(String.format("HI %s", clientName));
                continue;
            }
            if (TERMINATION_COMMAND.equals(inputLine)) {
                terminateClientSession();
                continue;
            }
            sendMessage(UNRECOGNIZABLE_COMMAND_MESSAGE);
        }
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    public void listenForSessionTimeout() throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            final Duration durationFromLastMessage = Duration.between(timeOfLastMessage, Instant.now());
            if ((durationFromLastMessage.getSeconds() > SESSION_TIMEOUT)) {
                terminateClientSession();
            }
        }
    }

    private void terminateClientSession() throws IOException {
        final Duration sessionDuration = Duration.between(timeOfSessionStart, Instant.now());
        String farewellMessage = String.format(FAREWELL_MESSAGE_PATTERN, clientName, sessionDuration.toMillis());
        sendMessage(farewellMessage);
        closeClient();

    }


    public void stop() throws IOException {
        closeClient();
        closeServer();
    }

    private void closeClient() throws IOException {
        clientSocket.close();
        out.close();
    }

    private void closeServer() throws IOException {
        in.close();
        serverSocket.close();
    }
}