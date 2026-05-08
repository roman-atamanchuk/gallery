package model;

import java.util.List;
import util.ModelFactory;

/**
 * Central graph data structure for the gallery.
 * This is the public entry point for adding rooms and creating connections
 * between them while individual Room objects simply store adjacency data.
 */
public class Graph {

    private final List<Room> rooms;

    public Graph() {
        this.rooms = ModelFactory.createList();
    }

    public List<Room> getRooms() {
        return rooms;
    }

    /**
     * Adds a room node to the graph.
     */
    public void addRoom(Room room) {
        if (room != null && !rooms.contains(room)) {
            rooms.add(room);
        }
    }

    /**
     * Adds a one-way weighted connection between two rooms.
     */
    public void addConnection(Room source, Room destination, int distance, int interestScore) {
        connectRoomsDirected(source, destination, distance, interestScore);
    }

    /**
     * Creates a one-way weighted connection between two rooms.
     */
    public void connectRoomsDirected(Room source, Room destination, int distance, int interestScore) {
        if (source == null || destination == null) {
            return;
        }
        addRoom(source);
        addRoom(destination);
        source.addConnection(new Edge(destination, distance, interestScore));
    }

    /**
     * Creates a two-way weighted connection between two rooms.
     */
    public void connectRoomsUndirected(Room source, Room destination, int distance, int interestScore) {
        connectRoomsDirected(source, destination, distance, interestScore);
        connectRoomsDirected(destination, source, distance, interestScore);
    }

    /**
     * Convenience overload for a one-way connection with a default interest score of zero.
     */
    public void addConnection(Room source, Room destination, int distance) {
        addConnection(source, destination, distance, 0);
    }

    /**
     * Convenience overload for a two-way connection with a default interest score of zero.
     */
    public void connectRoomsUndirected(Room source, Room destination, int distance) {
        connectRoomsUndirected(source, destination, distance, 0);
    }
}
