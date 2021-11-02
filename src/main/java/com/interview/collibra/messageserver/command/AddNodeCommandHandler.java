package com.interview.collibra.messageserver.command;

import com.interview.collibra.messageserver.GraphService;

import java.util.List;
import java.util.function.Consumer;

public class AddNodeCommandHandler implements CommandHandler {

    private final String NODE_ADDED = "NODE ADDED";
    private final String NODE_EXISTS = "ERROR: NODE ALREADY EXISTS";

    private final Consumer<String> messageSender;
    private final GraphService graphService;

    public AddNodeCommandHandler(Consumer<String> messageSender, GraphService graphService) {
        this.messageSender = messageSender;
        this.graphService = graphService;
    }

    @Override
    public void handle(List<String> params) {
        final String nodeName = params.get(0);
        final boolean success = graphService.addNode(nodeName);
        if (success) {
            messageSender.accept(NODE_ADDED);
        } else {
            messageSender.accept(NODE_EXISTS);
        }
    }
}
