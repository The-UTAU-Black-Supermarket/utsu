package com.utsusynth.utsu.view.song;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.utsusynth.utsu.common.quantize.Quantizer;
import com.utsusynth.utsu.common.quantize.Scaler;
import com.utsusynth.utsu.common.utils.PitchUtils;
import com.utsusynth.utsu.common.utils.RoundUtils;
import com.utsusynth.utsu.view.song.note.envelope.Envelope;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.*;

/** The background track of the song editor. */
public class Track {
    private final Scaler scaler;

    private int numMeasures = 0;
    private ListView<String> noteTrack;
    private ScrollBar noteVScrollBar;
    private ListView<Set<TrackItem>> dynamicsTrack;

    @Inject
    public Track(Scaler scaler) {
        this.scaler = scaler;
    }

    public int getNumMeasures() {
        return numMeasures;
    }

    public void setNumMeasures(int numMeasures) {
        if (numMeasures < 0 || numMeasures == this.numMeasures) {
            return; // Don't refresh unless you have to.
        }
        this.numMeasures = numMeasures;
        setNumMeasures(noteTrack, numMeasures);
        setNumDynamicsMeasures(dynamicsTrack, numMeasures);
    }

    private void setNumMeasures(ListView<String> updateMe, int numMeasures) {
        if (updateMe != null) {
            int numCols = (numMeasures + 1) * 4;
            updateMe.setItems(
                    FXCollections.observableArrayList(Collections.nCopies(numCols, "")));
        }
    }

    private void setNumDynamicsMeasures(ListView<Set<TrackItem>> updateMe, int numMeasures) {
        if (updateMe != null) {
            int numCols = (numMeasures + 1) * 4;
            updateMe.setItems(FXCollections.observableArrayList());
            for (int i = 0; i < numCols; i++) {
                updateMe.getItems().add(ImmutableSet.of());
            }
        }
    }

    private Optional<ScrollBar> getScrollBar(ListView<String> source, Orientation orientation) {
        if (source == null) {
            return Optional.empty();
        }
        for (Node node : source.lookupAll(".scroll-bar")) {
            if (!(node instanceof ScrollBar)) {
                continue;
            }
            ScrollBar scrollBar = (ScrollBar) node;
            if (scrollBar.getOrientation() == orientation) {
                return Optional.of(scrollBar);
            }
        }
        return Optional.empty();
    }

    private ListView<String> createNoteTrack() {
        double colWidth = scaler.scaleX(Quantizer.COL_WIDTH).get();
        double rowHeight = scaler.scaleY(Quantizer.ROW_HEIGHT).get();

        noteTrack = new ListView<>();
        noteVScrollBar = null;
        noteTrack.setSelectionModel(new NoSelectionModel<>());
        noteTrack.setFixedCellSize(colWidth);
        noteTrack.setOrientation(Orientation.HORIZONTAL);
        noteTrack.setCellFactory(source -> new ListCell<>() {
            {
                setPrefHeight(rowHeight * PitchUtils.TOTAL_NUM_PITCHES);
                setStyle("-fx-padding: 0px;");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                VBox column = new VBox();
                for (int octave = 7; octave > 0; octave--) {
                    for (String pitch : PitchUtils.REVERSE_PITCHES) {
                        // Add row to track.
                        Pane newCell = new Pane();
                        newCell.setPrefSize(colWidth, rowHeight);
                        newCell.getStyleClass().add("track-cell");
                        if (getIndex() >= 4) {
                            newCell.getStyleClass()
                                    .add(pitch.endsWith("#") ? "black-key" : "white-key");
                        } else {
                            newCell.getStyleClass().add("gray-key");
                        }
                        if (getIndex() % 4 == 0) {
                            newCell.getStyleClass().add("measure-start");
                        } else if (getIndex() % 4 == 3) {
                            newCell.getStyleClass().add("measure-end");
                        }
                        column.getChildren().add(newCell);
                    }
                }
                setGraphic(column);
            }
        });
        // Custom scroll behavior because default behavior is stupid.
        noteTrack.addEventFilter(ScrollEvent.ANY, event -> {
            if (event.getDeltaX() != 0) {
                return; // Vertical scrolling should be left to default behavior.
            }
            Optional<ScrollBar> verticalScroll = noteVScrollBar != null
                    ? Optional.of(noteVScrollBar) : getScrollBar(noteTrack, Orientation.VERTICAL);
            if (verticalScroll.isPresent()) {
                noteVScrollBar = verticalScroll.get();
                double newValue = verticalScroll.get().getValue() - event.getDeltaY();
                double boundedValue = Math.min(
                        verticalScroll.get().getMax(),
                        Math.max(verticalScroll.get().getMin(), newValue));
                verticalScroll.get().setValue(boundedValue);
            }
            event.consume();
        });
        setNumMeasures(noteTrack, numMeasures);
        return noteTrack;
    }

    public ListView<String> getNoteTrack() {
        if (noteTrack == null) {
            return createNoteTrack();
        }
        return noteTrack;
    }

    private ListView<Set<TrackItem>> createDynamicsTrack() {
        double colWidth = scaler.scaleX(Quantizer.COL_WIDTH).get();
        double rowHeight = 50;

        dynamicsTrack = new ListView<>();
        dynamicsTrack.setSelectionModel(new NoSelectionModel<>());
        dynamicsTrack.setFixedCellSize(colWidth);
        dynamicsTrack.setOrientation(Orientation.HORIZONTAL);
        dynamicsTrack.setCellFactory(source -> new ListCell<>() {
            {
                setPrefHeight(rowHeight * 2);
                setPrefWidth(colWidth);
                setStyle("-fx-padding: 0px;");
            }
            @Override
            protected void updateItem (Set<TrackItem> item, boolean empty) {
                super.updateItem(item, empty);

                Pane graphic = new Pane();
                graphic.setPrefSize(colWidth, rowHeight * 2);

                VBox newDynamics = new VBox();
                AnchorPane topCell = new AnchorPane();
                topCell.setPrefSize(colWidth, rowHeight);
                topCell.getStyleClass().add("dynamics-top-cell");
                if (getIndex() % 4 == 0) {
                    topCell.getStyleClass().add("measure-start");
                }

                AnchorPane bottomCell = new AnchorPane();
                bottomCell.setPrefSize(colWidth, rowHeight);
                bottomCell.getStyleClass().add("dynamics-bottom-cell");
                if (getIndex() % 4 == 0) {
                    bottomCell.getStyleClass().add("measure-start");
                }
                newDynamics.getChildren().addAll(topCell, bottomCell);

                graphic.getChildren().addAll(newDynamics);
                if (item != null) {
                    for (TrackItem trackItem : item) {
                        double offset = getIndex() * colWidth;
                        Node node = trackItem.getElement();
                        node.setTranslateX(-offset);
                        graphic.getChildren().add(node);
                    }
                }
                setGraphic(graphic);
            }
        });
        // Custom scroll behavior because default behavior is stupid.
        dynamicsTrack.addEventFilter(ScrollEvent.ANY, event -> {
            if (event.getDeltaX() == 0) {
                event.consume();
            }
        });
        setNumDynamicsMeasures(dynamicsTrack, numMeasures);
        return dynamicsTrack;
    }

    public ListView<Set<TrackItem>> getDynamicsTrack() {
        if (dynamicsTrack == null) {
            return createDynamicsTrack();
        }
        return dynamicsTrack;
    }

    public void insertEnvelope(TrackItem trackItem) {
        double startX = trackItem.getStartX();
        double endX = trackItem.getStartX() + trackItem.getWidth();
        double colWidth = scaler.scaleX(Quantizer.COL_WIDTH).get();
        int startColNum = RoundUtils.round(startX / colWidth);
        int endColNum = RoundUtils.round(endX / colWidth);
        for (int colNum = startColNum; colNum <= endColNum; colNum++) {
            ImmutableSet<TrackItem> items = new ImmutableSet.Builder<TrackItem>()
                    .addAll(dynamicsTrack.getItems().get(colNum))
                    .add(trackItem)
                    .build();
            dynamicsTrack.getItems().set(colNum, items);
        }
    }

    /**
     * No-op selection model to remove unwanted selection behavior.
     */
    private static class NoSelectionModel<T> extends MultipleSelectionModel<T> {
        @Override
        public void clearAndSelect(int i) {}
        @Override
        public void select(int i) {}
        @Override
        public void select(T t) {}
        @Override
        public void clearSelection(int i) {}
        @Override
        public void clearSelection() {}
        @Override
        public boolean isSelected(int i) {
            return false;
        }
        @Override
        public boolean isEmpty() {
            return true;
        }
        @Override
        public void selectPrevious() {}
        @Override
        public void selectNext() {}
        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return FXCollections.observableArrayList();
        }
        @Override
        public ObservableList<T> getSelectedItems() {
            return FXCollections.observableArrayList();
        }
        @Override
        public void selectIndices(int i, int... ints) {}
        @Override
        public void selectAll() {}
        @Override
        public void selectFirst() {}
        @Override
        public void selectLast() {}
    }
}
