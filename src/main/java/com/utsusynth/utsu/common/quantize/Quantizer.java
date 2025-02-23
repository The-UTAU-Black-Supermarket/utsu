package com.utsusynth.utsu.common.quantize;

public class Quantizer {
    public static final int DEFAULT_NOTE_DURATION = 480;
    public static final int COL_WIDTH = DEFAULT_NOTE_DURATION;
    public static final int ROW_HEIGHT = 20;
    public static final int SCROLL_BAR_WIDTH = 16; // If changing here, change css as well.
    public static final int TEXT_FIELD_WIDTH = 60; // Enforce a constant text field size.
    public static final int TRACK_COL_WIDTH = COL_WIDTH; // Not guaranteed to be equal.

    /* The number of ms in one quant. */
    private int quantization;

    public Quantizer(int defaultQuantization) {
        this.quantization = defaultQuantization;
    }

    public int getQuant() {
        return quantization;
    }

    public void changeQuant(int oldQuant, int newQuant) {
        if (oldQuant != quantization) {
            // TODO: Handle this better.
            System.out.println("ERROR: Data race when changing quantization!");
        }
        quantization = newQuant;
    }
}
