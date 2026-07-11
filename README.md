# National Gallery Route Finder

A JavaFX desktop application that finds routes around the main floor (level 2) of the National Gallery, London, using custom graph data structures and classic traversal/shortest-path algorithms.

Rooms are modelled as graph nodes, connections between rooms as weighted edges, and exhibits as artworks stored inside rooms. The app can resolve routes between rooms or specific exhibits, avoid rooms, pass through waypoints, and draw the result on the gallery map.

## Features

- **Single valid route** between any two rooms/exhibits
- **Multiple route permutations** via depth-first search (DFS)
- **Shortest route by steps** via breadth-first search (BFS)
- **Shortest route by distance** and **most interesting route** (weighted by exhibit interest score and preferred artists) via Dijkstra's algorithm
- **Pixel-level BFS** over a black-and-white traversal image of the map, with the resulting path drawn back onto the map
- Waypoint support and room/exhibit avoidance
- JavaFX GUI with route controls, graph overview, and interactive map display
- Editable CSV "database" (`rooms.csv`, `connections.csv`, `exhibits.csv`, `room_coordinates.csv`)

## Tech Stack

Java 21 · JavaFX 21 · Maven · JUnit 5 · JMH (microbenchmarks)

## Project Structure

```
src/main/java/
  algorithm/   BFS, DFS, Dijkstra, PixelBFS
  model/       Room, Edge, Exhibit, Graph
  service/     RouteService, GalleryDataLoader, RoomCoordinateLoader
  controller/  MainController (JavaFX)
  util/        MapMaskBuilder, RouteHelper, ModelFactory
src/main/resources/
  data/        CSV "database" files
  view/        FXML layout
src/test/java/  JUnit tests + JMH benchmarks
docs/          Full assignment report
```

## Running

```bash
mvn javafx:run
```

## Testing

```bash
mvn test
```

Includes JUnit coverage for the graph algorithms, route service, sample data loading, and pixel BFS, plus JMH benchmarks for DFS/BFS/Dijkstra and the service-level route methods.

> Note: the JavaFX dependencies in `pom.xml` are pinned to the `mac-aarch64` classifier. On another OS/architecture, change the classifier (or drop it and let the OS-specific JavaFX artifact resolve automatically).

See [`docs/assignment_report.md`](docs/assignment_report.md) for the full write-up, including algorithm details and marking-scheme coverage.
