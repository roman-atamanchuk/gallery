package util;

import model.Exhibit;
import model.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Shared utility placeholder for small reusable helper operations.
 * Keep algorithm implementations out of this class and reserve it for
 * formatting, validation, parsing, and simple cross-cutting support methods.
 */
public final class RouteHelper {

    private RouteHelper() {
        // Prevent instantiation of utility classes.
    }

    /**
     * Placeholder for locating a room by its identifier inside a collection.
     */
    public static Room findRoomById(List<Room> rooms, String roomId) {
        if (rooms == null || roomId == null || roomId.isBlank()) {
            return null;
        }

        String normalizedSelection = normalizeSelectionToken(roomId);

        for (Room room : rooms) {
            if (room.getId().toLowerCase(Locale.ROOT).equals(normalizedSelection)
                    || room.getName().toLowerCase(Locale.ROOT).equals(normalizedSelection)
                    || ("room " + room.getId()).toLowerCase(Locale.ROOT).equals(normalizedSelection)) {
                return room;
            }
        }

        for (Room room : rooms) {
            for (Exhibit exhibit : room.getExhibits()) {
                if (exhibit.getId().toLowerCase(Locale.ROOT).equals(normalizedSelection)
                        || exhibit.getTitle().toLowerCase(Locale.ROOT).equals(normalizedSelection)
                        || ("exhibit " + exhibit.getId()).toLowerCase(Locale.ROOT).equals(normalizedSelection)) {
                    return room;
                }
            }
        }

        return null;
    }

    /**
     * Placeholder for converting user text input into a list of preferences.
     */
    public static List<String> parsePreferredArtists(String rawInput) {
        return parseCommaSeparatedValues(rawInput);
    }

    /**
     * Converts a comma-separated room input string into room references from the graph.
     */
    public static List<Room> parseRoomSelections(List<Room> rooms, String rawInput) {
        if (rooms == null || rawInput == null || rawInput.isBlank()) {
            return List.of();
        }

        List<Room> selectedRooms = new ArrayList<>();
        for (String token : parseCommaSeparatedValues(rawInput)) {
            Room room = findRoomById(rooms, token);
            if (room != null && !selectedRooms.contains(room)) {
                selectedRooms.add(room);
            }
        }
        return selectedRooms;
    }

    /**
     * Parses a positive integer and falls back to the supplied default if invalid.
     */
    public static int parsePositiveInteger(String rawInput, int defaultValue) {
        if (rawInput == null || rawInput.isBlank()) {
            return defaultValue;
        }

        try {
            int value = Integer.parseInt(rawInput.trim());
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    /**
     * Returns a simple bonus score when a room category matches the user's artist interests.
     */
    public static int getPreferredArtistBonus(Room room, List<String> preferredArtists) {
        if (room == null || preferredArtists == null || preferredArtists.isEmpty()) {
            return 0;
        }

        List<String> categoryArtists = room.getFeaturedArtists();
        int matches = 0;
        for (String preferredArtist : preferredArtists) {
            String normalizedPreferredArtist = preferredArtist.toLowerCase(Locale.ROOT);
            for (String categoryArtist : categoryArtists) {
                if (categoryArtist.toLowerCase(Locale.ROOT).contains(normalizedPreferredArtist)
                        || normalizedPreferredArtist.contains(categoryArtist.toLowerCase(Locale.ROOT))) {
                    matches++;
                    break;
                }
            }
        }

        return matches;
    }

    /**
     * Placeholder for formatting a single route for UI display.
     */
    public static String formatRoute(List<Room> route) {
        if (route == null || route.isEmpty()) {
            return "No route found.";
        }

        return route.stream()
                .map(Room::getId)
                .collect(Collectors.joining(" -> "));
    }

    /**
     * Placeholder for formatting multiple routes for UI display.
     */
    public static String formatRoutes(List<List<Room>> routes) {
        if (routes == null || routes.isEmpty()) {
            return "No routes found.";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < routes.size(); i++) {
            builder.append("Path ").append(i + 1).append(": ")
                    .append(formatRoute(routes.get(i)));

            if (i < routes.size() - 1) {
                builder.append(System.lineSeparator());
            }
        }
        return builder.toString();
    }

    /**
     * Placeholder for checking whether a user has supplied valid room input.
     */
    public static boolean hasValidRoomSelection(Room start, Room end) {
        return start != null && end != null;
    }

    /**
     * Formats a lightweight list of exhibits found in rooms along the route.
     */
    public static String formatExhibitsOnRoute(List<Room> route) {
        if (route == null || route.isEmpty()) {
            return "Exhibits on route: none";
        }

        List<String> exhibits = new ArrayList<>();
        for (Room room : route) {
            for (Exhibit exhibit : room.getExhibits()) {
                exhibits.add(exhibit.getTitle() + " - " + exhibit.getArtist() + " (Room " + room.getId() + ")");
            }
        }

        if (exhibits.isEmpty()) {
            return "Exhibits on route: none";
        }

        return "Exhibits on route:" + System.lineSeparator() + String.join(System.lineSeparator(), exhibits);
    }

    private static List<String> parseCommaSeparatedValues(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (String token : rawInput.split(",")) {
            String trimmedToken = token.trim();
            if (!trimmedToken.isEmpty()) {
                values.add(trimmedToken);
            }
        }
        return values;
    }

    private static String normalizeSelectionToken(String rawSelection) {
        String normalizedSelection = rawSelection.trim().toLowerCase(Locale.ROOT);
        int separatorIndex = normalizedSelection.indexOf(" - ");
        if (separatorIndex >= 0) {
            normalizedSelection = normalizedSelection.substring(0, separatorIndex).trim();
        }
        return normalizedSelection;
    }
}
