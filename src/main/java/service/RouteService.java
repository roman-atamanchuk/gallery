package service;

import algorithm.BFS;
import algorithm.DFS;
import algorithm.Dijkstra;
import model.Graph;
import model.Room;
import util.RouteHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Coordinates route-related use cases between the UI layer and algorithm classes.
 * The lecture code focuses on standalone graph methods; this service adapts that
 * style into a cleaner JavaFX application structure.
 */
public class RouteService {

    private Graph graph;
    private DFS dfs;
    private BFS bfs;
    private Dijkstra dijkstra;

    public RouteService() {
        this(SampleGalleryData.createFullGraph(), new DFS(), new BFS(), new Dijkstra());
    }

    public RouteService(Graph graph, DFS dfs, BFS bfs, Dijkstra dijkstra) {
        this.graph = graph;
        this.dfs = dfs;
        this.bfs = bfs;
        this.dijkstra = dijkstra;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public DFS getDfs() {
        return dfs;
    }

    public void setDfs(DFS dfs) {
        this.dfs = dfs;
    }

    public BFS getBfs() {
        return bfs;
    }

    public void setBfs(BFS bfs) {
        this.bfs = bfs;
    }

    public Dijkstra getDijkstra() {
        return dijkstra;
    }

    public void setDijkstra(Dijkstra dijkstra) {
        this.dijkstra = dijkstra;
    }

    /**
     * Returns all available routes between two rooms.
     */
    public List<List<Room>> getAllRoutes(Room start, Room end) {
        return getAllRoutes(start, end, List.of(), List.of(), Integer.MAX_VALUE);
    }

    /**
     * Returns all available routes while supporting waypoints and rooms to avoid.
     */
    public List<List<Room>> getAllRoutes(Room start,
                                         Room end,
                                         List<Room> waypoints,
                                         List<Room> roomsToAvoid,
                                         int maxRoutes) {
        if (!RouteHelper.hasValidRoomSelection(start, end) || maxRoutes <= 0) {
            return List.of();
        }

        List<Room> routeTargets = buildRouteTargets(waypoints, end);
        Set<Room> avoidedRooms = buildAvoidSet(start, end, routeTargets, roomsToAvoid);
        List<List<Room>> combinedRoutes = new ArrayList<>();
        combinedRoutes.add(new ArrayList<>(List.of(start)));

        Room segmentStart = start;
        for (Room segmentEnd : routeTargets) {
            List<List<Room>> segmentRoutes = dfs.findAllPaths(segmentStart, segmentEnd, maxRoutes, avoidedRooms);
            if (segmentRoutes.isEmpty()) {
                return List.of();
            }

            combinedRoutes = mergeRoutePermutations(combinedRoutes, segmentRoutes, maxRoutes);
            if (combinedRoutes.isEmpty()) {
                return List.of();
            }
            segmentStart = segmentEnd;
        }

        return combinedRoutes;
    }

    /**
     * Returns the shortest route using breadth-first search.
     */
    public List<Room> getShortestRouteBFS(Room start, Room end) {
        return getShortestRouteBFS(start, end, List.of(), List.of());
    }

    /**
     * Returns the shortest route using breadth-first search with optional waypoints and avoid rooms.
     */
    public List<Room> getShortestRouteBFS(Room start, Room end, List<Room> waypoints, List<Room> roomsToAvoid) {
        return buildSegmentedRoute(start, end, waypoints, roomsToAvoid, RouteAlgorithm.BFS);
    }

    /**
     * Returns the shortest weighted route using Dijkstra's algorithm.
     */
    public List<Room> getShortestRouteDijkstra(Room start, Room end) {
        return getShortestRouteDijkstra(start, end, List.of(), List.of());
    }

    /**
     * Returns the shortest weighted route using Dijkstra's algorithm with optional waypoints and avoid rooms.
     */
    public List<Room> getShortestRouteDijkstra(Room start, Room end, List<Room> waypoints, List<Room> roomsToAvoid) {
        return buildSegmentedRoute(start, end, waypoints, roomsToAvoid, RouteAlgorithm.DIJKSTRA);
    }

    /**
     * Returns a route tailored to visitor interests.
     */
    public List<Room> getInterestingRoute(Room start, Room end, List<String> preferredArtists) {
        return getInterestingRoute(start, end, preferredArtists, List.of(), List.of());
    }

    /**
     * Returns an interest-weighted route with optional waypoints and rooms to avoid.
     */
    public List<Room> getInterestingRoute(Room start,
                                          Room end,
                                          List<String> preferredArtists,
                                          List<Room> waypoints,
                                          List<Room> roomsToAvoid) {
        return buildSegmentedRoute(start, end, preferredArtists, waypoints, roomsToAvoid, RouteAlgorithm.INTERESTING);
    }

    /**
     * Finds a room in the current graph by its room id.
     */
    public Room findRoomById(String roomId) {
        if (graph == null) {
            return null;
        }
        return RouteHelper.findRoomById(graph.getRooms(), roomId);
    }

    /**
     * Resolves a user-visible room or exhibit selection back to the room node
     * used by the graph algorithms.
     */
    public Room findRoomForSelection(String selection) {
        return findRoomById(selection);
    }

    /**
     * Returns all selectable route endpoints, including rooms and exhibits.
     */
    public List<String> getSelectableLocations() {
        List<String> locations = new ArrayList<>();
        if (graph == null) {
            return locations;
        }

        for (Room room : graph.getRooms()) {
            locations.add("Room " + room.getId() + " - " + room.getName() + " [" + room.getCategory() + "]");
            room.getExhibits().forEach(exhibit ->
                    locations.add(
                            "Exhibit " + exhibit.getId() + " - " + exhibit.getTitle()
                                    + " (" + exhibit.getArtist() + ", Room " + room.getId() + ")"
                    )
            );
        }

        return locations;
    }

    private List<Room> buildSegmentedRoute(Room start,
                                           Room end,
                                           List<String> preferredArtists,
                                           List<Room> waypoints,
                                           List<Room> roomsToAvoid,
                                           RouteAlgorithm algorithm) {
        if (!RouteHelper.hasValidRoomSelection(start, end)) {
            return List.of();
        }

        List<Room> routeTargets = buildRouteTargets(waypoints, end);
        Set<Room> avoidedRooms = buildAvoidSet(start, end, routeTargets, roomsToAvoid);
        List<Room> fullRoute = new ArrayList<>();
        Room segmentStart = start;

        for (Room segmentEnd : routeTargets) {
            List<Room> segmentRoute = switch (algorithm) {
                case BFS -> bfs.findShortestPath(segmentStart, segmentEnd, avoidedRooms);
                case DIJKSTRA -> dijkstra.findShortestPath(segmentStart, segmentEnd, avoidedRooms);
                case INTERESTING -> dijkstra.findMostInterestingPath(segmentStart, segmentEnd, preferredArtists, avoidedRooms);
            };

            if (segmentRoute.isEmpty()) {
                return List.of();
            }

            appendSegment(fullRoute, segmentRoute);
            segmentStart = segmentEnd;
        }

        return fullRoute;
    }

    private List<Room> buildSegmentedRoute(Room start,
                                           Room end,
                                           List<Room> waypoints,
                                           List<Room> roomsToAvoid,
                                           RouteAlgorithm algorithm) {
        return buildSegmentedRoute(start, end, List.of(), waypoints, roomsToAvoid, algorithm);
    }

    private List<Room> buildRouteTargets(List<Room> waypoints, Room end) {
        List<Room> targets = new ArrayList<>();
        if (waypoints != null) {
            targets.addAll(waypoints);
        }
        targets.add(end);
        return targets;
    }

    private Set<Room> buildAvoidSet(Room start, Room end, List<Room> routeTargets, List<Room> roomsToAvoid) {
        Set<Room> avoidedRooms = new HashSet<>();
        if (roomsToAvoid != null) {
            avoidedRooms.addAll(roomsToAvoid);
        }
        avoidedRooms.remove(start);
        avoidedRooms.remove(end);
        avoidedRooms.removeAll(routeTargets);
        return avoidedRooms;
    }

    private List<List<Room>> mergeRoutePermutations(List<List<Room>> existingRoutes,
                                                    List<List<Room>> nextSegmentRoutes,
                                                    int maxRoutes) {
        List<List<Room>> mergedRoutes = new ArrayList<>();
        for (List<Room> existingRoute : existingRoutes) {
            for (List<Room> nextSegmentRoute : nextSegmentRoutes) {
                if (mergedRoutes.size() >= maxRoutes) {
                    return mergedRoutes;
                }

                List<Room> candidateRoute = new ArrayList<>(existingRoute);
                appendSegment(candidateRoute, nextSegmentRoute);
                mergedRoutes.add(candidateRoute);
            }
        }
        return mergedRoutes;
    }

    private void appendSegment(List<Room> fullRoute, List<Room> segmentRoute) {
        if (fullRoute.isEmpty()) {
            fullRoute.addAll(segmentRoute);
            return;
        }

        for (int i = 1; i < segmentRoute.size(); i++) {
            fullRoute.add(segmentRoute.get(i));
        }
    }

    private enum RouteAlgorithm {
        BFS,
        DIJKSTRA,
        INTERESTING
    }
}
