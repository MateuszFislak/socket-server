package com.interview.collibra.messageserver;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public Optional<Integer> findShortestPath(String start, String end) {
        DijkstraShortestPath<String, DefaultWeightedEdge> shortestPath = new DijkstraShortestPath<>(graph);
        try {
            final GraphPath<String, DefaultWeightedEdge> path = shortestPath.getPath(start, end);
            return Optional.ofNullable(path).map(GraphPath::getWeight).map(Double::intValue).or(() -> Optional.of(Integer.MAX_VALUE));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
