package com.interview.collibra.messageserver.command;

import com.interview.collibra.messageserver.GraphService;

import java.util.List;
import java.util.function.Consumer;

public class RemoveNodeCommandHandler implements CommandHandler {

    public final String NODE_REMOVED = "NODE REMOVED";
    public final String NODE_NOT_FOUND = "ERROR: NODE NOT FOUND";

    private final Consumer<String> messageSender;
    private final GraphService graphService;

    public RemoveNodeCommandHandler(Consumer<String> messageSender, GraphService graphService) {
        this.messageSender = messageSender;
        this.graphService = graphService;
    }

    @Override
    public void handle(List<String> params) {
        final String nodeName = params.get(0);
        final boolean success = graphService.removeNode(nodeName);
        if (success) {
            messageSender.accept(NODE_REMOVED);
        } else {
            messageSender.accept(NODE_NOT_FOUND);
        }
    }
}
