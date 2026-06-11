package com.example.chess.util;

/**
 * Interface to isolate resource (strings) retrieval from the logic classes.
 */
public interface IResourceProvider {
    String getString(int resId);
    String getString(int resId, Object... formatArgs);
}
