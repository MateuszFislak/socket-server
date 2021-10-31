package com.interview.collibra;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Service
public class MessageServer {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void init() throws IOException {
        serverSocket = new ServerSocket(5000);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Async
    public void listenToMessages() throws IOException {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(String.format("[Server] <- %s", inputLine));
            if (".".equals(inputLine)) {
                out.println("good bye");
                break;
            }
            String response = String.format("Hello %s", inputLine);
            System.out.println(String.format("[Server] -> %s", response));
            out.println(response);
        }
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
}