package algorithm;

import model.Edge;
import model.Room;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Depth-first search for retrieving multiple valid room-to-room routes.
 * This follows the lecture approach of recursively exploring adjacency lists
 * while avoiding cycles through an encountered/visited collection.
 */
public class DFS {

    /**
     * Finds all possible paths between a start room and an end room.
     */
    public List<List<Room>> findAllPaths(Room start, Room end) {
        return findAllPaths(start, end, Integer.MAX_VALUE);
    }

    /**
     * Finds up to a maximum number of possible paths between a start room and an end room.
     */
    public List<List<Room>> findAllPaths(Room start, Room end, int maxRoutes) {
        return findAllPaths(start, end, maxRoutes, Set.of());
    }

    /**
     * Finds all possible paths while skipping specified rooms.
     */
    public List<List<Room>> findAllPaths(Room start, Room end, int maxRoutes, Set<Room> roomsToAvoid) {
        if (start == null || end == null || maxRoutes <= 0) {
            return List.of();
        }

        Set<Room> avoidedRooms = roomsToAvoid == null ? Set.of() : roomsToAvoid;
        if (avoidedRooms.contains(start) || avoidedRooms.contains(end)) {
            return List.of();
        }

        List<List<Room>> allPaths = new ArrayList<>();
        depthFirstSearch(start, end, maxRoutes, avoidedRooms, new HashSet<>(), new ArrayList<>(), allPaths);
        return allPaths;
    }

    private void depthFirstSearch(Room current,
                                  Room target,
                                  int maxRoutes,
                                  Set<Room> roomsToAvoid,
                                  Set<Room> visited,
                                  List<Room> currentPath,
                                  List<List<Room>> allPaths) {
        if (allPaths.size() >= maxRoutes || roomsToAvoid.contains(current)) {
            return;
        }

        visited.add(current);
        currentPath.add(current);

        if (current.equals(target)) {
            allPaths.add(new ArrayList<>(currentPath));
        } else {
            for (Edge edge : current.getConnections()) {
                Room nextRoom = edge.getDestination();
                if (!visited.contains(nextRoom) && !roomsToAvoid.contains(nextRoom)) {
                    depthFirstSearch(nextRoom, target, maxRoutes, roomsToAvoid, visited, currentPath, allPaths);
                }
            }
        }

        currentPath.remove(currentPath.size() - 1);
        visited.remove(current);
    }
}
