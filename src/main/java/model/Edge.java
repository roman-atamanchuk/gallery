package model;

import java.util.Objects;

/**
 * Represents a weighted connection between two rooms in the graph.
 * This is the assignment equivalent of the lecture's separate edge/link object.
 * Distance can model travel cost, while interestScore can support scenic routes.
 */
public class Edge {

    private final Room destination;
    private final int distance;
    private final int interestScore;

    public Edge(Room destination, int distance) {
        this(destination, distance, 0);
    }

    public Edge(Room destination, int distance, int interestScore) {
        this.destination = Objects.requireNonNull(destination, "destination must not be null");
        this.distance = distance;
        this.interestScore = interestScore;
    }

    public Room getDestination() {
        return destination;
    }

    public int getDistance() {
        return distance;
    }

    public int getInterestScore() {
        return interestScore;
    }
}
