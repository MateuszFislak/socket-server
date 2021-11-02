package com.interview.collibra.messageserver.command;

import com.interview.collibra.messageserver.GraphService;

import java.util.List;
import java.util.function.Consumer;

public class RemoveEdgeCommandHandler implements CommandHandler {

    public final String EDGE_REMOVED = "EDGE REMOVED";
    public final String NODE_NOT_FOUND = "ERROR: NODE NOT FOUND";

    private final Consumer<String> messageSender;
    private final GraphService graphService;

    public RemoveEdgeCommandHandler(Consumer<String> messageSender, GraphService graphService) {
        this.messageSender = messageSender;
        this.graphService = graphService;
    }

    @Override
    public void handle(List<String> params) {
        final String firstEdge = params.get(0);
        final String secondEdge = params.get(1);
        final boolean success = graphService.removeEdge(firstEdge, secondEdge);
        if (success) {
            messageSender.accept(EDGE_REMOVED);
        } else {
            messageSender.accept(NODE_NOT_FOUND);
        }
    }
}
