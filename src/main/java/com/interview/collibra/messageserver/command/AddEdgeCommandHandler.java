package com.interview.collibra.messageserver.command;

import com.interview.collibra.messageserver.GraphService;

import java.util.List;
import java.util.function.Consumer;

public class AddEdgeCommandHandler implements CommandHandler {

    public final String EDGE_ADDED = "EDGE ADDED";
    public final String NODE_NOT_FOUND = "ERROR: NODE NOT FOUND";
    private final Consumer<String> messageSender;
    private final GraphService graphService;

    public AddEdgeCommandHandler(Consumer<String> messageSender, GraphService graphService) {
        this.messageSender = messageSender;
        this.graphService = graphService;
    }

    @Override
    public void handle(List<String> params) {
        final String firstEdge = params.get(0);
        final String secondEdge = params.get(1);
        final Integer weight = Integer.valueOf(params.get(2));
        final boolean success = graphService.addEdge(firstEdge, secondEdge, weight);
        if (success) {
            messageSender.accept(EDGE_ADDED);
        } else {
            messageSender.accept(NODE_NOT_FOUND);
        }
    }
}
