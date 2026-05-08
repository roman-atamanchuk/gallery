package service;

import model.Edge;
import model.Graph;
import model.Room;

/**
 * Builds sample graph data inspired by the National Gallery upper-floor map.
 * The room connections are approximate and intended for graph-testing work.
 */
public final class SampleGalleryData {

    private SampleGalleryData() {
        // Prevent instantiation of utility-style service classes.
    }

    /**
     * Creates a small sample graph using a subset of Renaissance rooms.
     */
    public static Graph createSampleGraph() {
        Graph graph = new Graph();

        Room r2 = new Room("2", "Renaissance Room 2");
        Room r4 = new Room("4", "Renaissance Room 4");
        Room r5 = new Room("5", "Renaissance Room 5");
        Room r6 = new Room("6", "Renaissance Room 6");
        Room r7 = new Room("7", "Renaissance Room 7");
        Room r8 = new Room("8", "Renaissance Room 8");
        Room r9 = new Room("9", "Renaissance Room 9");
        Room r10 = new Room("10", "Renaissance Room 10");
        Room r11 = new Room("11", "Renaissance Room 11");
        Room r12 = new Room("12", "Renaissance Room 12");
        Room r14 = new Room("14", "Renaissance Room 14");

        graph.addRoom(r2);
        graph.addRoom(r4);
        graph.addRoom(r5);
        graph.addRoom(r6);
        graph.addRoom(r7);
        graph.addRoom(r8);
        graph.addRoom(r9);
        graph.addRoom(r10);
        graph.addRoom(r11);
        graph.addRoom(r12);
        graph.addRoom(r14);

        graph.connectRoomsUndirected(r2, r4, 1, 5);   // approximate based on map layout
        graph.connectRoomsUndirected(r2, r12, 1, 5);  // approximate based on map layout
        graph.connectRoomsUndirected(r4, r5, 1, 5);   // approximate based on map layout
        graph.connectRoomsUndirected(r4, r6, 1, 5);   // approximate based on map layout
        graph.connectRoomsUndirected(r5, r11, 1, 5);  // approximate based on map layout
        graph.connectRoomsUndirected(r6, r7, 1, 5);   // approximate based on map layout
        graph.connectRoomsUndirected(r6, r8, 1, 5);   // approximate based on map layout
        graph.connectRoomsUndirected(r6, r10, 1, 5);  // approximate based on map layout
        graph.connectRoomsUndirected(r8, r9, 1, 5);   // approximate based on map layout
        graph.connectRoomsUndirected(r9, r10, 1, 5);  // approximate based on map layout
        graph.connectRoomsUndirected(r10, r11, 1, 5); // approximate based on map layout
        graph.connectRoomsUndirected(r11, r12, 1, 5); // approximate based on map layout
        graph.connectRoomsUndirected(r11, r14, 1, 5); // approximate based on map layout
        graph.connectRoomsUndirected(r12, r14, 1, 5); // approximate based on map layout

        return graph;
    }

    /**
     * Creates a larger graph for the visible main-floor rooms on the National Gallery map.
     */
    public static Graph createFullGraph() {
        return GalleryDataLoader.loadFullGraph();
    }

    /**
     * Prints a simple adjacency list using room ids only.
     */
    public static void printAdjacencyList(Graph graph) {
        for (Room room : graph.getRooms()) {
            StringBuilder line = new StringBuilder();
            line.append(room.getId()).append(":");

            for (Edge edge : room.getConnections()) {
                line.append(" ").append(edge.getDestination().getId());
            }

            System.out.println(line);
        }
    }
}
