package algorithm;

import javafx.geometry.Point2D;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PixelBfsTest {

    @Test
    void pixelBfs_findsPathAcrossMonochromeWalkableCorridor() {
        WritableImage image = new WritableImage(5, 5);
        PixelWriter writer = image.getPixelWriter();

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                writer.setColor(x, y, Color.BLACK);
            }
        }

        for (int x = 0; x < 5; x++) {
            writer.setColor(x, 2, Color.WHITE);
        }

        PixelBFS pixelBfs = new PixelBFS();
        List<Point2D> path = pixelBfs.findShortestPath(image, 5, 5, new Point2D(0, 2), new Point2D(4, 2));

        assertFalse(path.isEmpty());
        assertTrue(path.size() >= 5);
    }
}
