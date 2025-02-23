package com.utsusynth.utsu.view.song;

import com.google.inject.Inject;
import com.utsusynth.utsu.common.quantize.Quantizer;
import com.utsusynth.utsu.common.quantize.Scaler;
import com.utsusynth.utsu.common.utils.PitchUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class Piano {
    private static final double LEFT_KEY_WIDTH = 60;
    private static final double RIGHT_KEY_WIDTH = 40;

    private final Scaler scaler;

    private Pane pianoGrid;

    @Inject
    public Piano(Scaler scaler) {
        this.scaler = scaler;
    }

    public Pane initPiano() {
        addPianoKeys();
        return pianoGrid;
    }

    private void addPianoKeys() {
        double rowHeight = scaler.scaleY(Quantizer.ROW_HEIGHT);
        pianoGrid = new Pane();
        pianoGrid.setPrefSize(
                LEFT_KEY_WIDTH + RIGHT_KEY_WIDTH, rowHeight * PitchUtils.TOTAL_NUM_PITCHES);

        int rowNum = 0;
        for (int octave = 7; octave > 0; octave--) {
            for (String pitch : PitchUtils.REVERSE_PITCHES) {
                Pane leftHalfOfKey = new Pane();
                leftHalfOfKey.getStyleClass()
                        .add(pitch.endsWith("#") ? "piano-black-key" : "piano-white-key");
                leftHalfOfKey.setPrefSize(LEFT_KEY_WIDTH, rowHeight);
                leftHalfOfKey.getChildren().add(new Label(pitch + octave));
                leftHalfOfKey.setTranslateY(rowNum * rowHeight);

                Pane rightHalfOfKey;
                if (pitch.endsWith("#")) {
                    Pane bisectedKey = new Pane();
                    bisectedKey.getStyleClass().add("piano-no-border");
                    bisectedKey.setPrefSize(RIGHT_KEY_WIDTH, rowHeight);
                    Pane centerLine = new Pane();
                    centerLine.setPrefSize(RIGHT_KEY_WIDTH, rowHeight / 2.0);
                    centerLine.getStyleClass().add("piano-white-key");
                    bisectedKey.getChildren().add(centerLine);
                    rightHalfOfKey = bisectedKey;
                } else {
                    Pane blankKey = new Pane();
                    blankKey.setPrefSize(RIGHT_KEY_WIDTH, rowHeight);
                    if (pitch.startsWith("F") || pitch.startsWith("C")) {
                        blankKey.getStyleClass().add("piano-white-key");
                    } else {
                        blankKey.getStyleClass().add("piano-no-border");
                    }
                    rightHalfOfKey = blankKey;
                }
                // For some reason this code is necessary to make piano look normal on Windows.
                rightHalfOfKey.setTranslateX(LEFT_KEY_WIDTH);
                if (rowNum % 2 == 0 || pitch.startsWith("E")) {
                    rightHalfOfKey.setTranslateY(rowNum * rowHeight);
                } else {
                    rightHalfOfKey.setTranslateY(rowNum * rowHeight - 1);
                    rightHalfOfKey.setPrefHeight(rowHeight + 1);
                }

                pianoGrid.getChildren().addAll(leftHalfOfKey, rightHalfOfKey);
                rowNum++;
            }
        }
    }
}
