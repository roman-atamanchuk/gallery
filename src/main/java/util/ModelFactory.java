package util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Generic utility factory for creating model objects and model collections.
 * This keeps common creation patterns in the util layer while allowing the
 * concrete model classes to stay assignment-specific.
 */
public final class ModelFactory {

    private ModelFactory() {
        // Prevent instantiation of utility classes.
    }

    /**
     * Generic helper for creating any model object through a supplier.
     */
    public static <T> T createModel(Supplier<T> supplier) {
        // TODO: Add shared creation steps here later if required.
        return supplier.get();
    }

    /**
     * Generic helper for creating typed lists used by model classes.
     */
    public static <T> List<T> createList() {
        // TODO: Replace or extend the list implementation later if needed.
        return new ArrayList<>();
    }
}
