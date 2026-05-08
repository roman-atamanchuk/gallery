package util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Builds a black-and-white walkability map from the visible gallery map.
 * White pixels represent accessible rooms, corridors, and doorways.
 * Black pixels represent walls, background, labels, and blocked areas.
 */
public final class MapMaskBuilder {

    private MapMaskBuilder() {
        // Utility class.
    }

    /**
     * Converts the supplied map image into a binary traversal image.
     */
    public static Image createBlackAndWhiteWalkabilityMap(Image sourceImage) {
        if (sourceImage == null) {
            throw new IllegalArgumentException("sourceImage must not be null");
        }

        int width = (int) sourceImage.getWidth();
        int height = (int) sourceImage.getHeight();
        WritableImage maskImage = new WritableImage(width, height);
        PixelReader pixelReader = sourceImage.getPixelReader();
        PixelWriter pixelWriter = maskImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color sourceColor = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, isWalkableOnGalleryMap(sourceColor) ? Color.WHITE : Color.BLACK);
            }
        }

        return maskImage;
    }

    private static boolean isWalkableOnGalleryMap(Color color) {
        if (color.getOpacity() < 0.95) {
            return false;
        }

        double brightness = color.getBrightness();
        double saturation = color.getSaturation();

        if (brightness > 0.96 && saturation < 0.05) {
            return false;
        }

        if (brightness < 0.18) {
            return false;
        }

        return saturation >= 0.06 || brightness >= 0.55;
    }
}
