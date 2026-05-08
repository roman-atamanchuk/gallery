package service;

import model.Exhibit;
import model.Graph;
import model.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads room and connection data from resource files so the graph can be updated
 * without changing Java code.
 */
public final class GalleryDataLoader {

    private static final String ROOMS_RESOURCE = "/data/rooms.csv";
    private static final String CONNECTIONS_RESOURCE = "/data/connections.csv";
    private static final String EXHIBITS_RESOURCE = "/data/exhibits.csv";

    private GalleryDataLoader() {
        // Utility class.
    }

    /**
     * Loads the full main-floor graph from CSV resource files.
     */
    public static Graph loadFullGraph() {
        Graph graph = new Graph();
        Map<String, Room> roomsById = loadRooms(graph);
        loadExhibits(roomsById);
        loadConnections(graph, roomsById);
        return graph;
    }

    private static Map<String, Room> loadRooms(Graph graph) {
        Map<String, Room> roomsById = new LinkedHashMap<>();

        try (BufferedReader reader = openCsvReader(ROOMS_RESOURCE)) {
            reader.readLine(); // Skip header row.
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(",", -1);
                String id = columns[0].trim();
                String name = columns[1].trim();
                String category = columns[2].trim();
                int interestScore = Integer.parseInt(columns[3].trim());
                List<String> featuredArtists = parseFeaturedArtists(columns[4].trim());

                Room room = new Room(id, name, category, interestScore, featuredArtists);
                graph.addRoom(room);
                roomsById.put(id, room);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load room data from " + ROOMS_RESOURCE, exception);
        }

        return roomsById;
    }

    private static void loadConnections(Graph graph, Map<String, Room> roomsById) {
        try (BufferedReader reader = openCsvReader(CONNECTIONS_RESOURCE)) {
            reader.readLine(); // Skip header row.
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(",", -1);
                Room sourceRoom = roomsById.get(columns[0].trim());
                Room destinationRoom = roomsById.get(columns[1].trim());
                int distance = Integer.parseInt(columns[2].trim());
                int interestScore = Math.max(sourceRoom.getInterestScore(), destinationRoom.getInterestScore());
                graph.connectRoomsUndirected(sourceRoom, destinationRoom, distance, interestScore);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load connection data from " + CONNECTIONS_RESOURCE, exception);
        }
    }

    private static void loadExhibits(Map<String, Room> roomsById) {
        try (BufferedReader reader = openCsvReader(EXHIBITS_RESOURCE)) {
            reader.readLine(); // Skip header row.
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(",", -1);
                String exhibitId = columns[0].trim();
                String title = columns[1].trim();
                String artist = columns[2].trim();
                String roomId = columns[3].trim();

                Room room = roomsById.get(roomId);
                if (room == null) {
                    throw new IllegalStateException("Exhibit " + exhibitId + " refers to unknown room " + roomId);
                }

                room.addExhibit(new Exhibit(exhibitId, title, artist));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load exhibit data from " + EXHIBITS_RESOURCE, exception);
        }
    }

    private static BufferedReader openCsvReader(String resourcePath) {
        InputStream inputStream = GalleryDataLoader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalStateException("Missing resource: " + resourcePath);
        }
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    private static List<String> parseFeaturedArtists(String rawValue) {
        if (rawValue.isBlank()) {
            return List.of();
        }

        List<String> artists = new ArrayList<>();
        for (String token : rawValue.split("\\|")) {
            String trimmedToken = token.trim();
            if (!trimmedToken.isEmpty()) {
                artists.add(trimmedToken);
            }
        }
        return artists;
    }
}
