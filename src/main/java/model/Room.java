package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a room or exhibit node in the gallery graph.
 * It stores the room identity plus the adjacency-list style collection
 * of outgoing weighted connections.
 */
public class Room {
    private final List<Edge> connections;
    private final String id;
    private String name;

    private String category;
    private int interestScore;
    private final List<String> featuredArtists;
    private final List<Exhibit> exhibits;


    public Room(String id, String name) {
        this(id, name, "", 0, List.of());
    }

    public Room(String id, String name, String category, int interestScore, List<String> featuredArtists) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = name;
        this.category = category;
        this.interestScore = interestScore;
        this.featuredArtists = new ArrayList<>(featuredArtists == null ? List.of() : featuredArtists);
        this.exhibits = new ArrayList<>();
        this.connections = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getInterestScore() {
        return interestScore;
    }

    public void setInterestScore(int interestScore) {
        this.interestScore = interestScore;
    }

    public List<String> getFeaturedArtists() {
        return Collections.unmodifiableList(featuredArtists);
    }

    public List<Exhibit> getExhibits() {
        return Collections.unmodifiableList(exhibits);
    }

    public List<Edge> getConnections() {
        return connections;
    }

    /**
     * Adds an already-created edge to this room's adjacency list.
     * Graph is intended to be the public entry point for building connections.
     */
    void addConnection(Edge edge) {
        connections.add(edge);
    }

    /**
     * Stores an exhibit inside the room so exhibit selections can map back to
     * the room graph for routing.
     */
    public void addExhibit(Exhibit exhibit) {
        exhibits.add(exhibit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Room)) {
            return false;
        }

        Room other = (Room) obj;

        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
