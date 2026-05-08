package algorithm;

import model.Edge;
import model.Room;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Breadth-first search for retrieving the shortest route by edge count.
 * The implementation mirrors the lecture's agenda-of-path-lists approach.
 */
public class BFS {

    /**
     * Finds the shortest path between two rooms using edge count.
     */
    public List<Room> findShortestPath(Room start, Room end) {
        return findShortestPath(start, end, Set.of());
    }

    /**
     * Finds the shortest path between two rooms while skipping specified rooms.
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

        Deque<List<Room>> agenda = new ArrayDeque<>();
        Set<Room> encountered = new HashSet<>();
        agenda.addLast(new ArrayList<>(List.of(start)));
        encountered.add(start);

        while (!agenda.isEmpty()) {
            List<Room> currentPath = agenda.removeFirst();
            Room currentRoom = currentPath.get(currentPath.size() - 1);

            if (currentRoom.equals(end)) {
                return currentPath;
            }

            for (Edge edge : currentRoom.getConnections()) {
                Room nextRoom = edge.getDestination();
                if (!avoidedRooms.contains(nextRoom) && encountered.add(nextRoom)) {
                    List<Room> nextPath = new ArrayList<>(currentPath);
                    nextPath.add(nextRoom);
                    agenda.addLast(nextPath);
                }
            }
        }

        return List.of();
    }
}
