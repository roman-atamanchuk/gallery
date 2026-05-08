package service;

import model.Graph;
import model.Room;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleGalleryDataTest {

    @Test
    void printAdjacencyList_matchesExpectedSampleGraph() {
        Graph graph = SampleGalleryData.createSampleGraph();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
            SampleGalleryData.printAdjacencyList(graph);
        } finally {
            System.setOut(originalOut);
        }

        String expectedOutput = String.join(System.lineSeparator(),
                "2: 4 12",
                "4: 2 5 6",
                "5: 4 11",
                "6: 4 7 8 10",
                "7: 6",
                "8: 6 9",
                "9: 8 10",
                "10: 6 9 11",
                "11: 5 10 12 14",
                "12: 2 11 14",
                "14: 11 12"
        ) + System.lineSeparator();

        assertEquals(expectedOutput, outputStream.toString(StandardCharsets.UTF_8));
    }

    @Test
    void createFullGraph_loadsResourceBackedMainFloorData() {
        Graph graph = SampleGalleryData.createFullGraph();

        assertEquals(65, graph.getRooms().size());
        int totalExhibitCount = graph.getRooms().stream()
                .mapToInt(room -> room.getExhibits().size())
                .sum();

        Room room14 = graph.getRooms().stream()
                .filter(room -> room.getId().equals("14"))
                .findFirst()
                .orElseThrow();

        assertEquals("Renaissance", room14.getCategory());
        assertEquals(5, room14.getInterestScore());
        assertFalse(room14.getFeaturedArtists().isEmpty());
        assertTrue(totalExhibitCount >= 120);
        assertTrue(room14.getExhibits().size() >= 2);
        assertTrue(room14.getConnections().stream().anyMatch(edge -> edge.getDestination().getId().equals("29")));
    }
}
