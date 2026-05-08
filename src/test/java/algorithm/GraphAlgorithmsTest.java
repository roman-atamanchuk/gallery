package algorithm;

import model.Graph;
import model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.SampleGalleryData;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphAlgorithmsTest {

    private Graph graph;
    private Room room2;
    private Room room4;
    private Room room11;
    private Room room14;

    @BeforeEach
    void setUp() {
        graph = SampleGalleryData.createSampleGraph();
        room2 = findRoom("2");
        room4 = findRoom("4");
        room11 = findRoom("11");
        room14 = findRoom("14");
    }

    @Test
    void dfs_returnsMultiplePaths() {
        DFS dfs = new DFS();

        List<List<Room>> allPaths = dfs.findAllPaths(room4, room11);
        Set<String> pathStrings = allPaths.stream()
                .map(this::toPathString)
                .collect(Collectors.toSet());

        assertTrue(pathStrings.contains("4 -> 5 -> 11"));
        assertTrue(pathStrings.contains("4 -> 6 -> 10 -> 11"));
        assertTrue(pathStrings.contains("4 -> 2 -> 12 -> 11"));
        assertTrue(allPaths.size() >= 3);
    }

    @Test
    void bfs_returnsShortestPathByEdgeCount() {
        BFS bfs = new BFS();

        List<Room> shortestPath = bfs.findShortestPath(room2, room14);

        assertEquals("2 -> 12 -> 14", toPathString(shortestPath));
    }

    @Test
    void dijkstra_returnsShortestPathByDistance() {
        Dijkstra dijkstra = new Dijkstra();

        List<Room> shortestPath = dijkstra.findShortestPath(room2, room14);

        assertEquals("2 -> 12 -> 14", toPathString(shortestPath));
    }

    @Test
    void mostInterestingPath_prefersHigherInterestWhenGraphDiffers() {
        Graph customGraph = new Graph();
        Room start = new Room("A", "Start");
        Room end = new Room("D", "End");
        Room shortMiddle = new Room("B", "Short");
        Room scenic1 = new Room("C", "Scenic 1");
        Room scenic2 = new Room("E", "Scenic 2");

        customGraph.connectRoomsUndirected(start, shortMiddle, 1, 1);
        customGraph.connectRoomsUndirected(shortMiddle, end, 1, 1);
        customGraph.connectRoomsUndirected(start, scenic1, 1, 5);
        customGraph.connectRoomsUndirected(scenic1, scenic2, 1, 5);
        customGraph.connectRoomsUndirected(scenic2, end, 1, 5);

        Dijkstra dijkstra = new Dijkstra();
        List<Room> interestingPath = dijkstra.findMostInterestingPath(start, end);

        assertEquals("A -> C -> E -> D", toPathString(interestingPath));
    }

    private Room findRoom(String id) {
        return graph.getRooms().stream()
                .filter(room -> room.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private String toPathString(List<Room> route) {
        return route.stream()
                .map(Room::getId)
                .collect(Collectors.joining(" -> "));
    }
}
