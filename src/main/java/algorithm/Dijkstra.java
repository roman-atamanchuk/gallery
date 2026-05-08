package algorithm;

import model.Edge;
import model.Room;
import util.RouteHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Dijkstra-based route calculations for weighted gallery paths.
 * Algorithm state is kept local to each search in maps and queues.
 */
public class Dijkstra {

    private record DistanceState(Room room, int cost) {
    }

    private record ScenicState(Room room, int scenicCost, int distanceCost) {
    }

    /**
     * Finds the shortest weighted path between two rooms.
     */
    public List<Room> findShortestPath(Room start, Room end) {
        return findShortestPath(start, end, Set.of());
    }

    /**
     * Finds the shortest weighted path while skipping specified rooms.
     */
    public List<Room> findShortestPath(Room start, Room end, Set<Room> roomsToAvoid) {
        if (start == null || end == null) {
            return List.of();
        }

        Set<Room> avoidedRooms = roomsToAvoid == null ? Set.of() : roomsToAvoid;
        if (avoidedRooms.contains(start) || avoidedRooms.contains(end)) {
            return List.of();
        }
        if (start.equals(end)) {
            return List.of(start);
        }

        Map<Room, Integer> distances = new HashMap<>();
        Map<Room, Room> previousRooms = new HashMap<>();
        PriorityQueue<DistanceState> queue = new PriorityQueue<>(Comparator.comparingInt(DistanceState::cost));

        distances.put(start, 0);
        queue.add(new DistanceState(start, 0));

        while (!queue.isEmpty()) {
            DistanceState currentState = queue.poll();
            Room currentRoom = currentState.room();
            int currentCost = currentState.cost();

            if (currentCost > distances.getOrDefault(currentRoom, Integer.MAX_VALUE)) {
                continue;
            }
            if (currentRoom.equals(end)) {
                break;
            }

            for (Edge edge : currentRoom.getConnections()) {
                Room nextRoom = edge.getDestination();
                if (avoidedRooms.contains(nextRoom)) {
                    continue;
                }

                int newCost = currentCost + edge.getDistance();
                if (newCost < distances.getOrDefault(nextRoom, Integer.MAX_VALUE)) {
                    distances.put(nextRoom, newCost);
                    previousRooms.put(nextRoom, currentRoom);
                    queue.add(new DistanceState(nextRoom, newCost));
                }
            }
        }

        return rebuildPath(start, end, previousRooms, distances.containsKey(end));
    }

    /**
     * Finds a route that prefers higher-interest connections.
     * Interest is converted into a non-negative Dijkstra cost as:
     * transformedCost = maxInterestScoreOnReachableGraph - edgeInterestScore + 1
     * so that higher-interest edges become cheaper for the search.
     */
    public List<Room> findMostInterestingPath(Room start, Room end) {
        return findMostInterestingPath(start, end, List.of(), Set.of());
    }

    /**
     * Finds a route that prefers higher-interest connections while skipping specified rooms.
     */
    public List<Room> findMostInterestingPath(Room start, Room end, Set<Room> roomsToAvoid) {
        return findMostInterestingPath(start, end, List.of(), roomsToAvoid);
    }

    /**
     * Finds a route that prefers higher-interest connections and relevant artist categories.
     */
    public List<Room> findMostInterestingPath(Room start,
                                              Room end,
                                              List<String> preferredArtists,
                                              Set<Room> roomsToAvoid) {
        if (start == null || end == null) {
            return List.of();
        }

        Set<Room> avoidedRooms = roomsToAvoid == null ? Set.of() : roomsToAvoid;
        if (avoidedRooms.contains(start) || avoidedRooms.contains(end)) {
            return List.of();
        }
        if (start.equals(end)) {
            return List.of(start);
        }

        int maxInterestScore = findMaxInterestScore(start, avoidedRooms);
        Map<Room, Integer> scenicCosts = new HashMap<>();
        Map<Room, Integer> distanceCosts = new HashMap<>();
        Map<Room, Room> previousRooms = new HashMap<>();
        PriorityQueue<ScenicState> queue = new PriorityQueue<>(
                Comparator.comparingInt(ScenicState::scenicCost).thenComparingInt(ScenicState::distanceCost)
        );

        scenicCosts.put(start, 0);
        distanceCosts.put(start, 0);
        queue.add(new ScenicState(start, 0, 0));

        while (!queue.isEmpty()) {
            ScenicState currentState = queue.poll();
            Room currentRoom = currentState.room();
            int currentScenicCost = currentState.scenicCost();
            int currentDistanceCost = currentState.distanceCost();

            if (currentScenicCost > scenicCosts.getOrDefault(currentRoom, Integer.MAX_VALUE)) {
                continue;
            }
            if (currentScenicCost == scenicCosts.getOrDefault(currentRoom, Integer.MAX_VALUE)
                    && currentDistanceCost > distanceCosts.getOrDefault(currentRoom, Integer.MAX_VALUE)) {
                continue;
            }
            if (currentRoom.equals(end)) {
                break;
            }

            for (Edge edge : currentRoom.getConnections()) {
                Room nextRoom = edge.getDestination();
                if (avoidedRooms.contains(nextRoom)) {
                    continue;
                }

                int transformedInterestCost = maxInterestScore - edge.getInterestScore() + 1;
                int preferredArtistBonus = RouteHelper.getPreferredArtistBonus(nextRoom, preferredArtists);
                int weightedScenicCost = Math.max(1, transformedInterestCost - preferredArtistBonus);
                int newScenicCost = currentScenicCost + weightedScenicCost;
                int newDistanceCost = currentDistanceCost + edge.getDistance();
                int existingScenicCost = scenicCosts.getOrDefault(nextRoom, Integer.MAX_VALUE);
                int existingDistanceCost = distanceCosts.getOrDefault(nextRoom, Integer.MAX_VALUE);

                if (newScenicCost < existingScenicCost
                        || (newScenicCost == existingScenicCost && newDistanceCost < existingDistanceCost)) {
                    scenicCosts.put(nextRoom, newScenicCost);
                    distanceCosts.put(nextRoom, newDistanceCost);
                    previousRooms.put(nextRoom, currentRoom);
                    queue.add(new ScenicState(nextRoom, newScenicCost, newDistanceCost));
                }
            }
        }

        return rebuildPath(start, end, previousRooms, scenicCosts.containsKey(end));
    }

    public List<Room> findMostInterestingPath(Room start, Room end, List<String> preferredArtists) {
        return findMostInterestingPath(start, end, preferredArtists, Set.of());
    }

    private int findMaxInterestScore(Room start, Set<Room> roomsToAvoid) {
        int maxInterestScore = 0;
        Deque<Room> agenda = new ArrayDeque<>();
        Set<Room> encountered = new HashSet<>();
        agenda.add(start);
        encountered.add(start);

        while (!agenda.isEmpty()) {
            Room currentRoom = agenda.removeFirst();
            for (Edge edge : currentRoom.getConnections()) {
                maxInterestScore = Math.max(maxInterestScore, edge.getInterestScore());
                Room nextRoom = edge.getDestination();
                if (!roomsToAvoid.contains(nextRoom) && encountered.add(nextRoom)) {
                    agenda.addLast(nextRoom);
                }
            }
        }

        return maxInterestScore;
    }

    private List<Room> rebuildPath(Room start, Room end, Map<Room, Room> previousRooms, boolean foundPath) {
        if (!foundPath) {
            return List.of();
        }

        List<Room> route = new ArrayList<>();
        Room currentRoom = end;
        route.add(currentRoom);

        while (!currentRoom.equals(start)) {
            currentRoom = previousRooms.get(currentRoom);
            if (currentRoom == null) {
                return List.of();
            }
            route.add(0, currentRoom);
        }

        return route;
    }
}
