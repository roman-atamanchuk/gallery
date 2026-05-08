package model;

import java.util.Objects;

/**
 * Represents an art exhibit associated with a room node in the gallery graph.
 * Routing still happens at room level, but exhibits can be used as user-facing
 * endpoints that map back to the room that contains them.
 */
public class Exhibit {

    private final String id;
    private final String title;
    private final String artist;

    public Exhibit(String id, String title, String artist) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.artist = Objects.requireNonNull(artist, "artist must not be null");
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}
