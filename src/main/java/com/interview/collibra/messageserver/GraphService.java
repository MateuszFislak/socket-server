package com.interview.collibra.messageserver;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.springframework.stereotype.Service;

@Service
public class GraphService {

    private final DirectedWeightedMultigraph<String, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);

    public boolean addNode(String name) {
        return graph.addVertex(name);
    }

    public boolean addEdge(String first, String second, Integer weight) {
        try {
            final DefaultWeightedEdge edge = graph.addEdge(first, second);
            graph.setEdgeWeight(edge, weight);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean removeNode(String name) {
        return graph.removeVertex(name);
    }

    public boolean removeEdge(String first, String second) {
        final DefaultWeightedEdge removedEdge = graph.removeEdge(first, second);
        return removedEdge != null;
    }

}
