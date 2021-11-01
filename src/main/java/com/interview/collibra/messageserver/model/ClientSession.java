package com.interview.collibra.messageserver.model;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ClientSession {

    private final UUID id;
    private final Socket socket;
    private final PrintWriter outputStream;
    private final BufferedReader inputStream;
    private final Instant startTime;

    public ClientSession(Socket socket) throws IOException {
        this.id = UUID.randomUUID();
        this.socket = socket;
        this.outputStream = new PrintWriter(socket.getOutputStream(), true);
        this.inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.startTime = Instant.now();
    }

    public void close() throws IOException {
        socket.close();
        outputStream.close();
        inputStream.close();
    }

    public void write(String message) {
        outputStream.println(message);
    }

    public String read() throws IOException {
        return inputStream.readLine();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }
}
