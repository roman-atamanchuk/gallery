package algorithm;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Breadth-first search over walkable map pixels for shortest visual path finding.
 */
public class PixelBFS {

    /**
     * Finds the shortest walkable pixel path between two selected points on the map.
     * The map is sampled at the rendered image size so the UI route matches the display.
     */
    public List<Point2D> findShortestPath(Image image,
                                          int renderedWidth,
                                          int renderedHeight,
                                          Point2D startPoint,
                                          Point2D endPoint) {
        return findShortestPath(image, renderedWidth, renderedHeight, startPoint, endPoint, null);
    }

    /**
     * Finds the shortest walkable pixel path while restricting traversal to the
     * main floor-plan bounds inside the rendered map.
     */
    public List<Point2D> findShortestPath(Image image,
                                          int renderedWidth,
                                          int renderedHeight,
                                          Point2D startPoint,
                                          Point2D endPoint,
                                          Rectangle2D traversableBounds) {
        if (image == null || startPoint == null || endPoint == null || renderedWidth <= 0 || renderedHeight <= 0) {
            return List.of();
        }

        boolean[][] walkable = buildWalkableGrid(image, renderedWidth, renderedHeight, traversableBounds);
        int startX = clamp((int) Math.round(startPoint.getX()), 0, renderedWidth - 1);
        int startY = clamp((int) Math.round(startPoint.getY()), 0, renderedHeight - 1);
        int endX = clamp((int) Math.round(endPoint.getX()), 0, renderedWidth - 1);
        int endY = clamp((int) Math.round(endPoint.getY()), 0, renderedHeight - 1);

        Point2D snappedStart = snapToWalkable(walkable, startX, startY);
        Point2D snappedEnd = snapToWalkable(walkable, endX, endY);
        if (snappedStart == null || snappedEnd == null) {
            return List.of();
        }

        int snappedStartX = (int) snappedStart.getX();
        int snappedStartY = (int) snappedStart.getY();
        int snappedEndX = (int) snappedEnd.getX();
        int snappedEndY = (int) snappedEnd.getY();

        boolean[][] visited = new boolean[renderedHeight][renderedWidth];
        int[][] previousX = new int[renderedHeight][renderedWidth];
        int[][] previousY = new int[renderedHeight][renderedWidth];
        for (int row = 0; row < renderedHeight; row++) {
            Arrays.fill(previousX[row], -1);
            Arrays.fill(previousY[row], -1);
        }

        Deque<int[]> agenda = new ArrayDeque<>();
        agenda.addLast(new int[]{snappedStartX, snappedStartY});
        visited[snappedStartY][snappedStartX] = true;

        int[] deltaX = {1, -1, 0, 0};
        int[] deltaY = {0, 0, 1, -1};

        while (!agenda.isEmpty()) {
            int[] current = agenda.removeFirst();
            int currentX = current[0];
            int currentY = current[1];

            if (currentX == snappedEndX && currentY == snappedEndY) {
                return rebuildPath(previousX, previousY, snappedStartX, snappedStartY, snappedEndX, snappedEndY);
            }

            for (int i = 0; i < deltaX.length; i++) {
                int nextX = currentX + deltaX[i];
                int nextY = currentY + deltaY[i];
                if (nextX < 0 || nextY < 0 || nextX >= renderedWidth || nextY >= renderedHeight) {
                    continue;
                }
                if (!walkable[nextY][nextX] || visited[nextY][nextX]) {
                    continue;
                }

                visited[nextY][nextX] = true;
                previousX[nextY][nextX] = currentX;
                previousY[nextY][nextX] = currentY;
                agenda.addLast(new int[]{nextX, nextY});
            }
        }

        return List.of();
    }

    private boolean[][] buildWalkableGrid(Image image,
                                          int renderedWidth,
                                          int renderedHeight,
                                          Rectangle2D traversableBounds) {
        boolean[][] walkable = new boolean[renderedHeight][renderedWidth];
        PixelReader pixelReader = image.getPixelReader();
        double scaleX = image.getWidth() / renderedWidth;
        double scaleY = image.getHeight() / renderedHeight;
        boolean monochromeMap = isMostlyMonochrome(image, pixelReader, traversableBounds, renderedWidth, renderedHeight);

        for (int y = 0; y < renderedHeight; y++) {
            for (int x = 0; x < renderedWidth; x++) {
                if (!isInsideBounds(x, y, traversableBounds)) {
                    continue;
                }

                int sourceX = clamp((int) Math.floor(x * scaleX), 0, (int) image.getWidth() - 1);
                int sourceY = clamp((int) Math.floor(y * scaleY), 0, (int) image.getHeight() - 1);
                Color color = pixelReader.getColor(sourceX, sourceY);
                walkable[y][x] = monochromeMap ? isWalkableOnMonochromeMap(color) : isWalkableOnColourMap(color);
            }
        }

        return walkable;
    }

    private boolean isWalkableOnMonochromeMap(Color color) {
        if (color.getOpacity() < 0.95) {
            return false;
        }

        return color.getBrightness() >= 0.80;
    }

    private boolean isWalkableOnColourMap(Color color) {
        if (color.getOpacity() < 0.95) {
            return false;
        }

        double brightness = color.getBrightness();
        double saturation = color.getSaturation();

        if (brightness < 0.15) {
            return false;
        }

        if (saturation >= 0.08) {
            return true;
        }

        return brightness >= 0.55 && brightness <= 0.92;
    }

    private Point2D snapToWalkable(boolean[][] walkable, int startX, int startY) {
        if (walkable[startY][startX]) {
            return new Point2D(startX, startY);
        }

        int height = walkable.length;
        int width = walkable[0].length;
        int maxRadius = Math.max(width, height);
        for (int radius = 1; radius < maxRadius; radius++) {
            for (int y = Math.max(0, startY - radius); y <= Math.min(height - 1, startY + radius); y++) {
                for (int x = Math.max(0, startX - radius); x <= Math.min(width - 1, startX + radius); x++) {
                    if (walkable[y][x]) {
                        return new Point2D(x, y);
                    }
                }
            }
        }

        return null;
    }

    private List<Point2D> rebuildPath(int[][] previousX,
                                      int[][] previousY,
                                      int startX,
                                      int startY,
                                      int endX,
                                      int endY) {
        List<Point2D> path = new ArrayList<>();
        int currentX = endX;
        int currentY = endY;
        path.add(new Point2D(currentX, currentY));

        while (currentX != startX || currentY != startY) {
            int nextX = previousX[currentY][currentX];
            int nextY = previousY[currentY][currentX];
            if (nextX < 0 || nextY < 0) {
                return List.of();
            }
            currentX = nextX;
            currentY = nextY;
            path.add(0, new Point2D(currentX, currentY));
        }

        return path;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean isMostlyMonochrome(Image image,
                                       PixelReader pixelReader,
                                       Rectangle2D traversableBounds,
                                       int renderedWidth,
                                       int renderedHeight) {
        long sampleCount = 0;
        double saturationTotal = 0;
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int stepX = Math.max(1, width / 60);
        int stepY = Math.max(1, height / 60);

        for (int y = 0; y < height; y += stepY) {
            for (int x = 0; x < width; x += stepX) {
                int renderedX = clamp((int) Math.round((double) x * renderedWidth / width), 0, renderedWidth - 1);
                int renderedY = clamp((int) Math.round((double) y * renderedHeight / height), 0, renderedHeight - 1);
                if (!isInsideBounds(renderedX, renderedY, traversableBounds)) {
                    continue;
                }
                saturationTotal += pixelReader.getColor(x, y).getSaturation();
                sampleCount++;
            }
        }

        if (sampleCount == 0) {
            return false;
        }

        double averageSaturation = saturationTotal / sampleCount;
        return averageSaturation < 0.06;
    }

    private boolean isInsideBounds(int x, int y, Rectangle2D traversableBounds) {
        return traversableBounds == null || traversableBounds.contains(x, y);
    }
}
