package service;

import javafx.geometry.Point2D;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads approximate room-center positions on the level 2 map for visual route drawing.
 */
public final class RoomCoordinateLoader {

    private static final String COORDINATES_RESOURCE = "/data/room_coordinates.csv";

    private RoomCoordinateLoader() {
        // Utility class.
    }

    /**
     * Loads approximate room-center coordinates keyed by room id.
     */
    public static Map<String, Point2D> loadRoomCoordinates() {
        Map<String, Point2D> coordinates = new LinkedHashMap<>();

        try (BufferedReader reader = openCsvReader()) {
            reader.readLine(); // Skip header row.
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(",", -1);
                String id = columns[0].trim();
                double x = Double.parseDouble(columns[1].trim());
                double y = Double.parseDouble(columns[2].trim());
                coordinates.put(id, new Point2D(x, y));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load room coordinates from " + COORDINATES_RESOURCE, exception);
        }

        return coordinates;
    }

    private static BufferedReader openCsvReader() {
        InputStream inputStream = RoomCoordinateLoader.class.getResourceAsStream(COORDINATES_RESOURCE);
        if (inputStream == null) {
            throw new IllegalStateException("Missing resource: " + COORDINATES_RESOURCE);
        }
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }
}
