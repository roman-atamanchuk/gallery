package controller;

import algorithm.PixelBFS;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import model.Edge;
import model.Room;
import service.RoomCoordinateLoader;
import service.RouteService;
import util.MapMaskBuilder;
import util.RouteHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles JavaFX UI events for the main route finder screen.
 * Business logic should remain in the service layer.
 */
public class MainController {

    @FXML
    private ComboBox<String> startRoomComboBox;

    @FXML
    private ComboBox<String> endRoomComboBox;

    @FXML
    private TextField preferredArtistsField;

    @FXML
    private TextField waypointRoomsField;

    @FXML
    private TextField avoidRoomsField;

    @FXML
    private TextField maxRoutesField;

    @FXML
    private TextArea resultArea;

    @FXML
    private ListView<String> graphOverviewListView;

    @FXML
    private ImageView mapImageView;

    @FXML
    private Canvas routeCanvas;

    private RouteService routeService;
    private PixelBFS pixelBfs;
    private Image displayedMapImage;
    private Image pixelTraversalImage;
    private Map<String, Point2D> roomCoordinates;
    private Rectangle2D traversableMapBounds;
    private List<List<Room>> visibleGraphRoutes = List.of();
    private List<Point2D> visiblePixelRoute = List.of();
    private Point2D pixelStartPoint;
    private Point2D pixelEndPoint;
    private PixelSelectionMode pixelSelectionMode = PixelSelectionMode.NONE;

    /**
     * Called by JavaFX after the FXML has been loaded.
     * Use this later to wire UI components to the service layer.
     */
    @FXML
    public void initialize() {
        routeService = new RouteService();
        pixelBfs = new PixelBFS();
        roomCoordinates = RoomCoordinateLoader.loadRoomCoordinates();
        displayedMapImage = mapImageView.getImage();
        populateRoomSelectors();
        populateGraphOverview();
        resizeCanvasToImage();
        traversableMapBounds = buildTraversableBounds();
        pixelTraversalImage = MapMaskBuilder.createBlackAndWhiteWalkabilityMap(displayedMapImage);
        redrawOverlay();
        resultArea.setText("Main-floor graph loaded. Choose rooms or exhibits, then compare route types or run pixel BFS on the map.");
    }

    /**
     * Handles the action for finding all possible routes between two rooms.
     */
    @FXML
    public void onFindRoute() {
        Room start = routeService.findRoomForSelection(startRoomComboBox.getValue());
        Room end = routeService.findRoomForSelection(endRoomComboBox.getValue());
        if (!RouteHelper.hasValidRoomSelection(start, end)) {
            resultArea.setText("Please choose valid start and destination rooms.");
            return;
        }

        List<Room> waypoints = parseWaypointRooms();
        List<Room> roomsToAvoid = parseAvoidRooms();
        int maxRoutes = RouteHelper.parsePositiveInteger(maxRoutesField.getText(), 10);
        List<List<Room>> allRoutes = routeService.getAllRoutes(start, end, waypoints, roomsToAvoid, maxRoutes);
        visibleGraphRoutes = allRoutes;
        visiblePixelRoute = List.of();
        redrawOverlay();
        resultArea.setText(RouteHelper.formatRoutes(allRoutes));
    }

    /**
     * Handles the action for finding the shortest route.
     */
    @FXML
    public void onFindShortest() {
        Room start = routeService.findRoomForSelection(startRoomComboBox.getValue());
        Room end = routeService.findRoomForSelection(endRoomComboBox.getValue());
        if (!RouteHelper.hasValidRoomSelection(start, end)) {
            resultArea.setText("Please choose valid start and destination rooms.");
            return;
        }

        List<Room> waypoints = parseWaypointRooms();
        List<Room> roomsToAvoid = parseAvoidRooms();
        List<Room> bfsRoute = routeService.getShortestRouteBFS(start, end, waypoints, roomsToAvoid);
        List<Room> dijkstraRoute = routeService.getShortestRouteDijkstra(start, end, waypoints, roomsToAvoid);
        visibleGraphRoutes = List.of(bfsRoute, dijkstraRoute);
        visiblePixelRoute = List.of();
        redrawOverlay();
        resultArea.setText(
                "BFS: " + RouteHelper.formatRoute(bfsRoute) + System.lineSeparator()
                        + "Dijkstra: " + RouteHelper.formatRoute(dijkstraRoute) + System.lineSeparator()
                        + System.lineSeparator()
                        + RouteHelper.formatExhibitsOnRoute(dijkstraRoute)
        );
    }

    /**
     * Handles the action for finding the most interesting route.
     */
    @FXML
    public void onFindInteresting() {
        Room start = routeService.findRoomForSelection(startRoomComboBox.getValue());
        Room end = routeService.findRoomForSelection(endRoomComboBox.getValue());
        if (!RouteHelper.hasValidRoomSelection(start, end)) {
            resultArea.setText("Please choose valid start and destination rooms.");
            return;
        }

        List<String> preferredArtists = RouteHelper.parsePreferredArtists(preferredArtistsField.getText());
        List<Room> waypoints = parseWaypointRooms();
        List<Room> roomsToAvoid = parseAvoidRooms();
        List<Room> interestingRoute = routeService.getInterestingRoute(start, end, preferredArtists, waypoints, roomsToAvoid);
        visibleGraphRoutes = List.of(interestingRoute);
        visiblePixelRoute = List.of();
        redrawOverlay();
        resultArea.setText(
                "Interesting route: " + RouteHelper.formatRoute(interestingRoute) + System.lineSeparator()
                        + "Preferred artists: "
                        + (preferredArtists.isEmpty() ? "none supplied" : String.join(", ", preferredArtists))
                        + System.lineSeparator()
                        + System.lineSeparator()
                        + RouteHelper.formatExhibitsOnRoute(interestingRoute)
        );
    }

    /**
     * Enables the next map click to set the pixel-BFS start point.
     */
    @FXML
    public void onSetPixelStartMode() {
        pixelSelectionMode = PixelSelectionMode.START;
        resultArea.setText("Click on the map to choose the pixel-route start point.");
    }

    /**
     * Enables the next map click to set the pixel-BFS end point.
     */
    @FXML
    public void onSetPixelEndMode() {
        pixelSelectionMode = PixelSelectionMode.END;
        resultArea.setText("Click on the map to choose the pixel-route end point.");
    }

    /**
     * Runs pixel-by-pixel BFS across the displayed level 2 map.
     */
    @FXML
    public void onFindPixelRoute() {
        if (pixelStartPoint == null || pixelEndPoint == null) {
            resultArea.setText("Pick both a pixel start and a pixel end point on the map first.");
            return;
        }

        List<Point2D> pixelRoute = pixelBfs.findShortestPath(
                pixelTraversalImage,
                (int) Math.round(routeCanvas.getWidth()),
                (int) Math.round(routeCanvas.getHeight()),
                pixelStartPoint,
                pixelEndPoint,
                traversableMapBounds
        );

        visiblePixelRoute = pixelRoute;
        visibleGraphRoutes = List.of();
        redrawOverlay();

        if (pixelRoute.isEmpty()) {
            resultArea.setText("No pixel BFS route could be found between the selected points.");
        } else {
            resultArea.setText(
                    "Pixel BFS route found." + System.lineSeparator()
                            + "Estimated distance: " + (pixelRoute.size() - 1) + " pixels"
            );
        }
    }

    /**
     * Switches the visible map between the gallery image and the black-and-white
     * walkability map used by pixel BFS.
     */
    @FXML
    public void onToggleMapMode() {
        Image normalMapImage = displayedMapImage;
        if (mapImageView.getImage() == pixelTraversalImage) {
            mapImageView.setImage(normalMapImage);
            resultArea.setText("Showing the normal gallery map.");
        } else {
            mapImageView.setImage(pixelTraversalImage);
            resultArea.setText("Showing the black-and-white walkability map used for pixel BFS.");
        }
        resizeCanvasToImage();
        traversableMapBounds = buildTraversableBounds();
        redrawOverlay();
    }

    /**
     * Handles map clicks for selecting pixel-BFS start/end points.
     */
    @FXML
    public void onMapClicked(MouseEvent event) {
        if (pixelSelectionMode == PixelSelectionMode.NONE) {
            return;
        }

        Point2D clickedPoint = new Point2D(event.getX(), event.getY());
        if (traversableMapBounds != null && !traversableMapBounds.contains(clickedPoint)) {
            resultArea.setText("Please choose points within the main floor plan area.");
            return;
        }

        if (pixelSelectionMode == PixelSelectionMode.START) {
            pixelStartPoint = clickedPoint;
            resultArea.setText("Pixel start point selected. Choose an end point or run pixel BFS.");
        } else if (pixelSelectionMode == PixelSelectionMode.END) {
            pixelEndPoint = clickedPoint;
            resultArea.setText("Pixel end point selected. Run pixel BFS when ready.");
        }

        pixelSelectionMode = PixelSelectionMode.NONE;
        redrawOverlay();
    }

    private void populateGraphOverview() {
        List<String> adjacencyLines = new ArrayList<>();
        for (Room room : routeService.getGraph().getRooms()) {
            StringBuilder line = new StringBuilder();
            line.append(room.getId()).append(" (").append(room.getCategory()).append("):");
            for (Edge edge : room.getConnections()) {
                line.append(" ").append(edge.getDestination().getId());
            }
            adjacencyLines.add(line.toString());
        }
        graphOverviewListView.setItems(FXCollections.observableArrayList(adjacencyLines));
    }

    private void populateRoomSelectors() {
        List<String> roomOptions = routeService.getSelectableLocations();

        startRoomComboBox.setItems(FXCollections.observableArrayList(roomOptions));
        endRoomComboBox.setItems(FXCollections.observableArrayList(roomOptions));

        if (!roomOptions.isEmpty()) {
            startRoomComboBox.getSelectionModel().selectFirst();
            endRoomComboBox.getSelectionModel().selectLast();
        }
    }

    private List<Room> parseWaypointRooms() {
        return RouteHelper.parseRoomSelections(routeService.getGraph().getRooms(), waypointRoomsField.getText());
    }

    private List<Room> parseAvoidRooms() {
        return RouteHelper.parseRoomSelections(routeService.getGraph().getRooms(), avoidRoomsField.getText());
    }

    private void resizeCanvasToImage() {
        routeCanvas.setWidth(mapImageView.getFitWidth());
        Image image = mapImageView.getImage();
        double aspectRatio = image.getHeight() / image.getWidth();
        routeCanvas.setHeight(mapImageView.getFitWidth() * aspectRatio);
    }

    private void redrawOverlay() {
        GraphicsContext graphicsContext = routeCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, routeCanvas.getWidth(), routeCanvas.getHeight());

        drawGraphRoutes(graphicsContext);
        drawPixelRoute(graphicsContext);
        drawPixelMarkers(graphicsContext);
    }

    private void drawGraphRoutes(GraphicsContext graphicsContext) {
        List<Color> palette = List.of(Color.DODGERBLUE, Color.CRIMSON, Color.DARKORANGE, Color.FORESTGREEN, Color.MEDIUMPURPLE);
        for (int i = 0; i < visibleGraphRoutes.size(); i++) {
            List<Room> route = visibleGraphRoutes.get(i);
            if (route == null || route.isEmpty()) {
                continue;
            }

            Color routeColor = palette.get(i % palette.size());
            graphicsContext.setStroke(routeColor.deriveColor(0, 1, 1, 0.85));
            graphicsContext.setFill(routeColor);
            graphicsContext.setLineWidth(4);

            Point2D previousPoint = null;
            for (Room room : route) {
                Point2D roomPoint = toCanvasPoint(room);
                if (roomPoint == null) {
                    continue;
                }

                if (previousPoint != null) {
                    graphicsContext.strokeLine(previousPoint.getX(), previousPoint.getY(), roomPoint.getX(), roomPoint.getY());
                }
                graphicsContext.fillOval(roomPoint.getX() - 5, roomPoint.getY() - 5, 10, 10);
                previousPoint = roomPoint;
            }
        }
    }

    private void drawPixelRoute(GraphicsContext graphicsContext) {
        if (visiblePixelRoute == null || visiblePixelRoute.size() < 2) {
            return;
        }

        graphicsContext.setStroke(Color.GOLD);
        graphicsContext.setLineWidth(2);
        Point2D previousPoint = visiblePixelRoute.get(0);
        for (int i = 1; i < visiblePixelRoute.size(); i++) {
            Point2D currentPoint = visiblePixelRoute.get(i);
            graphicsContext.strokeLine(previousPoint.getX(), previousPoint.getY(), currentPoint.getX(), currentPoint.getY());
            previousPoint = currentPoint;
        }
    }

    private void drawPixelMarkers(GraphicsContext graphicsContext) {
        if (pixelStartPoint != null) {
            graphicsContext.setFill(Color.LIMEGREEN);
            graphicsContext.fillOval(pixelStartPoint.getX() - 6, pixelStartPoint.getY() - 6, 12, 12);
        }
        if (pixelEndPoint != null) {
            graphicsContext.setFill(Color.RED);
            graphicsContext.fillOval(pixelEndPoint.getX() - 6, pixelEndPoint.getY() - 6, 12, 12);
        }
    }

    private Point2D toCanvasPoint(Room room) {
        Point2D sourcePoint = roomCoordinates.get(room.getId());
        if (sourcePoint == null) {
            return null;
        }

        Image image = mapImageView.getImage();
        double scaledX = sourcePoint.getX() * routeCanvas.getWidth() / image.getWidth();
        double scaledY = sourcePoint.getY() * routeCanvas.getHeight() / image.getHeight();
        return new Point2D(scaledX, scaledY);
    }

    private Rectangle2D buildTraversableBounds() {
        if (roomCoordinates == null || roomCoordinates.isEmpty()) {
            return null;
        }

        Image image = mapImageView.getImage();
        double scaleX = routeCanvas.getWidth() / image.getWidth();
        double scaleY = routeCanvas.getHeight() / image.getHeight();
        double minX = roomCoordinates.values().stream().mapToDouble(point -> point.getX() * scaleX).min().orElse(0);
        double maxX = roomCoordinates.values().stream().mapToDouble(point -> point.getX() * scaleX).max().orElse(0);
        double minY = roomCoordinates.values().stream().mapToDouble(point -> point.getY() * scaleY).min().orElse(0);
        double maxY = roomCoordinates.values().stream().mapToDouble(point -> point.getY() * scaleY).max().orElse(0);
        double padding = 90;

        return new Rectangle2D(
                Math.max(0, minX - padding),
                Math.max(0, minY - padding),
                (maxX - minX) + (padding * 2),
                (maxY - minY) + (padding * 2)
        );
    }

    private enum PixelSelectionMode {
        NONE,
        START,
        END
    }
}
