package service;

import model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RouteServiceTest {

    private RouteService routeService;
    private Room room2;
    private Room room10;
    private Room room12;
    private Room room14;

    @BeforeEach
    void setUp() {
        routeService = new RouteService();
        room2 = routeService.findRoomById("2");
        room10 = routeService.findRoomById("10");
        room12 = routeService.findRoomById("12");
        room14 = routeService.findRoomById("14");
    }

    @Test
    void avoidRoom_changesShortestRoute() {
        List<Room> route = routeService.getShortestRouteBFS(room2, room14, List.of(), List.of(room12));

        assertEquals("2 -> 4 -> 5 -> 11 -> 14", toPathString(route));
    }

    @Test
    void waypointRoute_combinesSegmentsInOrder() {
        List<Room> route = routeService.getShortestRouteBFS(room2, room14, List.of(room10), List.of());

        assertEquals("2 -> 4 -> 6 -> 10 -> 11 -> 14", toPathString(route));
    }

    @Test
    void exhibitSelection_resolvesBackToContainingRoom() {
        Room selectedRoom = routeService.findRoomForSelection("Exhibit EX-14-1 - Titian highlight in Room 14 (Titian, Room 14)");

        assertNotNull(selectedRoom);
        assertEquals("14", selectedRoom.getId());
    }

    private String toPathString(List<Room> route) {
        return route.stream()
                .map(Room::getId)
                .collect(Collectors.joining(" -> "));
    }
}
