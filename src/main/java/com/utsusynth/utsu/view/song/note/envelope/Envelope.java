package com.utsusynth.utsu.view.song.note.envelope;

import com.utsusynth.utsu.common.data.EnvelopeData;
import com.utsusynth.utsu.common.quantize.Scaler;
import com.utsusynth.utsu.common.utils.RoundUtils;
import com.utsusynth.utsu.view.song.TrackItem;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class Envelope implements TrackItem {
    private final MoveTo start;
    private final LineTo[] lines;
    private final LineTo end;
    private final Group group;
    private final double maxHeight;
    private final Scaler scaler;

    // Temporary cache values.
    private boolean changed = false;
    private EnvelopeData startData;

    Envelope(
            MoveTo start,
            LineTo l1,
            LineTo l2,
            LineTo l3,
            LineTo l4,
            LineTo l5,
            LineTo end,
            EnvelopeCallback callback,
            double maxHeight,
            Scaler scaler) {
        this.maxHeight = maxHeight;
        this.scaler = scaler;
        this.start = start;
        this.lines = new LineTo[] {l1, l2, l3, l4, l5};
        Circle[] circles = new Circle[5]; // Control points.
        for (int i = 0; i < 5; i++) {
            Circle circle = new Circle(lines[i].getX(), lines[i].getY(), 3);
            circle.getStyleClass().add("envelope-circle");
            lines[i].xProperty().bind(circle.centerXProperty());
            lines[i].yProperty().bind(circle.centerYProperty());
            final int index = i;
            circle.setOnMouseEntered(event -> {
                circle.getScene().setCursor(Cursor.HAND);
            });
            circle.setOnMouseExited(event -> {
                circle.getScene().setCursor(Cursor.DEFAULT);
            });
            circle.setOnMousePressed(event -> {
                changed = false;
                startData = getData();
            });
            circle.setOnMouseDragged(event -> {
                // Set reasonable limits for where envelope can be dragged.
                if (index > 0 && index < 4) {
                    double newX = event.getX();
                    if (newX > lines[index - 1].getX() && newX < lines[index + 1].getX()) {
                        changed = true;
                        circle.setCenterX(newX);
                    }
                }
                double newY = event.getY();
                if (newY >= 0 && newY <= maxHeight) {
                    changed = true;
                    circle.setCenterY(newY);
                }
            });
            circle.setOnMouseReleased(event -> {
                if (changed) {
                    callback.modifySongEnvelope(startData, getData());
                }
            });
            circles[i] = circle;
        }
        this.end = end;
        Path path = new Path(start, lines[0], lines[1], lines[2], lines[3], lines[4], end);
        path.getStyleClass().add("envelope-line");
        this.group = new Group(path, circles[0], circles[1], circles[2], circles[4], circles[3]);
    }

    @Override
    public double getStartX() {
        return start.getX();
    }

    @Override
    public double getWidth() {
        return end.getX() - start.getX();
    }

    @Override
    public Group getElement() {
        return group;
    }

    public int getStartMs() {
        return RoundUtils.round(scaler.unscalePos(start.getX()));
    }

    public EnvelopeData getData() {
        double[] widths = new double[5];
        widths[0] = scaler.unscaleX(lines[0].getX() - start.getX());
        widths[1] = scaler.unscaleX(lines[1].getX() - lines[0].getX());
        widths[2] = scaler.unscaleX(lines[4].getX() - lines[3].getX());
        widths[3] = scaler.unscaleX(end.getX() - lines[4].getX());
        widths[4] = scaler.unscaleX(lines[2].getX() - lines[1].getX());

        double multiplier = 200 / maxHeight; // Final value should have range 0-200.
        double[] heights = new double[5];
        heights[0] = (maxHeight - lines[0].getY()) * multiplier;
        heights[1] = (maxHeight - lines[1].getY()) * multiplier;
        heights[2] = (maxHeight - lines[3].getY()) * multiplier;
        heights[3] = (maxHeight - lines[4].getY()) * multiplier;
        heights[4] = (maxHeight - lines[2].getY()) * multiplier;
        return new EnvelopeData(widths, heights);
    }
}
