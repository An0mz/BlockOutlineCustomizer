package me.anomz.blockoutline.config;

/**
 * A complete look for the outline + fill. Used for the base config,
 * presets, and per-block overrides.
 */
public class StyleSettings {
    public boolean syncRgb = false;

    // Outline
    public OutlineStyle outlineStyle = OutlineStyle.FULL;
    public int outlineRed = 0;
    public int outlineGreen = 255;
    public int outlineBlue = 255;
    public double outlineOpacity = 1.0;
    public double outlineWidth = 3.0;
    public boolean outlineRgbEnabled = false;
    public double outlineRgbSpeed = 1.0;

    // Pulse (breathing opacity, applies to outline and fill)
    public boolean pulseEnabled = false;
    public double pulseSpeed = 1.0;
    public double pulseMinOpacity = 0.25;

    // Fill
    public boolean fillEnabled = false;
    public int fillRed = 255;
    public int fillGreen = 255;
    public int fillBlue = 255;
    public double fillOpacity = 0.3;
    public boolean fillRgbEnabled = false;
    public double fillRgbSpeed = 1.0;

    public StyleSettings copy() {
        StyleSettings c = new StyleSettings();
        c.syncRgb = syncRgb;
        c.outlineStyle = outlineStyle;
        c.outlineRed = outlineRed;
        c.outlineGreen = outlineGreen;
        c.outlineBlue = outlineBlue;
        c.outlineOpacity = outlineOpacity;
        c.outlineWidth = outlineWidth;
        c.outlineRgbEnabled = outlineRgbEnabled;
        c.outlineRgbSpeed = outlineRgbSpeed;
        c.pulseEnabled = pulseEnabled;
        c.pulseSpeed = pulseSpeed;
        c.pulseMinOpacity = pulseMinOpacity;
        c.fillEnabled = fillEnabled;
        c.fillRed = fillRed;
        c.fillGreen = fillGreen;
        c.fillBlue = fillBlue;
        c.fillOpacity = fillOpacity;
        c.fillRgbEnabled = fillRgbEnabled;
        c.fillRgbSpeed = fillRgbSpeed;
        return c;
    }

    /**
     * Clamps all values into their valid ranges and fixes nulls after
     * deserializing user-edited JSON.
     */
    public void sanitize() {
        if (outlineStyle == null) outlineStyle = OutlineStyle.FULL;
        outlineRed = clamp(outlineRed);
        outlineGreen = clamp(outlineGreen);
        outlineBlue = clamp(outlineBlue);
        outlineOpacity = clamp(outlineOpacity, 0.0, 1.0);
        outlineWidth = clamp(outlineWidth, 1.0, 10.0);
        outlineRgbSpeed = clamp(outlineRgbSpeed, 0.1, 10.0);
        pulseSpeed = clamp(pulseSpeed, 0.1, 10.0);
        pulseMinOpacity = clamp(pulseMinOpacity, 0.0, 1.0);
        fillRed = clamp(fillRed);
        fillGreen = clamp(fillGreen);
        fillBlue = clamp(fillBlue);
        fillOpacity = clamp(fillOpacity, 0.0, 1.0);
        fillRgbSpeed = clamp(fillRgbSpeed, 0.1, 10.0);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
