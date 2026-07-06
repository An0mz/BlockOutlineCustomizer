package me.anomz.blockoutline.util;

import java.awt.Color;

/**
 * Time-based animation helpers shared by both loaders.
 */
public final class Animations {
    private Animations() {
    }

    /**
     * Current rainbow hue in [0, 1). Derived from continuous real time so the
     * color never snaps. A speed of 1.0 completes a full cycle every 10 seconds.
     */
    public static float rainbowHue(double speed) {
        double cycles = System.currentTimeMillis() / 1000.0 * (speed / 10.0);
        return (float) (cycles - Math.floor(cycles));
    }

    /**
     * Current rainbow color packed as 0xFFRRGGBB.
     */
    public static int rainbowRgb(double speed) {
        return Color.HSBtoRGB(rainbowHue(speed), 1.0f, 1.0f);
    }

    /**
     * Breathing opacity multiplier in [minFactor, 1]. A speed of 1.0 completes
     * a full pulse every 2 seconds.
     */
    public static double pulse(double speed, double minFactor) {
        double wave = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 1000.0 * speed * Math.PI);
        return minFactor + (1.0 - minFactor) * wave;
    }
}
