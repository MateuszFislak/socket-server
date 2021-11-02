package com.interview.collibra.messageserver.command;

import com.interview.collibra.messageserver.GraphService;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FindShortestPathCommandHandler implements CommandHandler {

    public final String NODE_NOT_FOUND = "ERROR: NODE NOT FOUND";

    private final Consumer<String> messageSender;
    private final GraphService graphService;

    public FindShortestPathCommandHandler(Consumer<String> messageSender, GraphService graphService) {
        this.messageSender = messageSender;
        this.graphService = graphService;
    }

    @Override
    public void handle(List<String> params) {
        final String firstNode = params.get(0);
        final String secondNode = params.get(1);
        final Optional<Integer> shortestPath = graphService.findShortestPath(firstNode, secondNode);
        shortestPath.map(Object::toString).ifPresentOrElse(
                messageSender,
                () -> messageSender.accept(NODE_NOT_FOUND)
        );
    }
}
