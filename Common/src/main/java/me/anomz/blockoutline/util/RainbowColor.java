package me.anomz.blockoutline.util;

import java.awt.Color;

/**
 * Time-based rainbow color shared by both loaders.
 */
public final class RainbowColor {
    private RainbowColor() {
    }

    /**
     * Current rainbow hue in [0, 1). Derived from continuous real time so the
     * color never snaps. A speed of 1.0 completes a full cycle every 10 seconds.
     */
    public static float hue(double speed) {
        double cycles = System.currentTimeMillis() / 1000.0 * (speed / 10.0);
        return (float) (cycles - Math.floor(cycles));
    }

    /**
     * Current rainbow color packed as 0xFFRRGGBB.
     */
    public static int rgb(double speed) {
        return Color.HSBtoRGB(hue(speed), 1.0f, 1.0f);
    }
}
