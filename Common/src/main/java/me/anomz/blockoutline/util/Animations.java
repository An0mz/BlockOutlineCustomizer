package me.anomz.blockoutline.util;

import java.awt.Color;

/**
 * Time-based animation helpers shared by both loaders.
 */
public final class Animations {
    private Animations() {
    }

    /**
     * Current rainbow position in [0, 1). Derived from continuous real time so
     * the color never snaps. A speed of 1.0 completes a full cycle every 10 seconds.
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
     * RGB cycle color: the full spectrum, or looping through a custom palette.
     */
    public static int cycleRgb(double speed, int[] palette) {
        return sampleRgb(rainbowHue(speed), palette);
    }

    /**
     * Color at cycle position t (wrapped into [0,1)): full spectrum when the
     * palette is empty, otherwise a smooth loop through the palette colors.
     */
    public static int sampleRgb(double t, int[] palette) {
        float wrapped = (float) (t - Math.floor(t));
        if (palette == null || palette.length == 0) {
            return Color.HSBtoRGB(wrapped, 1.0f, 1.0f);
        }
        if (palette.length == 1) {
            return 0xFF000000 | palette[0];
        }
        float scaled = wrapped * palette.length;
        int index = Math.min((int) scaled, palette.length - 1);
        float frac = scaled - index;
        return 0xFF000000 | lerpRgb(palette[index], palette[(index + 1) % palette.length], frac);
    }

    private static int lerpRgb(int c1, int c2, float t) {
        int r = (int) (((c1 >> 16) & 0xFF) + t * (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)));
        int g = (int) (((c1 >> 8) & 0xFF) + t * (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)));
        int b = (int) ((c1 & 0xFF) + t * ((c2 & 0xFF) - (c1 & 0xFF)));
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Marching-dash offset in [0, period). A speed of 1.0 moves the pattern
     * 0.375 blocks per second (two dash periods).
     */
    public static float marchOffset(double speed, float period) {
        double distance = System.currentTimeMillis() / 1000.0 * (speed * 0.375);
        return (float) (distance % period);
    }

    /**
     * Breathing opacity multiplier in [minFactor, 1]. A speed of 1.0 completes
     * a full pulse every 2 seconds.
     */
    public static double pulse(double speed, double minFactor) {
        double wave = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 1000.0 * speed * Math.PI);
        return minFactor + (1.0 - minFactor) * wave;
    }

    /**
     * Hard on/off flashing: full opacity for half the cycle, minFactor for the
     * other half. Same period as {@link #pulse} so the speed slider matches.
     */
    public static double blink(double speed, double minFactor) {
        double phase = (System.currentTimeMillis() / 1000.0 * speed) % 2.0;
        return phase < 1.0 ? 1.0 : minFactor;
    }
}
