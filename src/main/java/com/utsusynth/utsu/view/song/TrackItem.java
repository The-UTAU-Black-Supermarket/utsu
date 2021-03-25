package com.utsusynth.utsu.view.song;

import javafx.scene.Node;

import java.util.Set;

/** Represents a single item that can be drawn in the track. */
public interface TrackItem {
    /** Start position of this item. */
    double getStartX();

    /** Width, in pixels, of this item. */
    double getWidth();

    /** Element to render for this item. */
    Node getElement();

    /** Redraw this item with a certain offset. */
    Node redraw(int colNum, double offsetX);

    /** Return every column where this item is currently drawn. */
    Set<Integer> getColumns();

    /** Clear list of columns after erasing item from them. */
    void clearColumns();
}
