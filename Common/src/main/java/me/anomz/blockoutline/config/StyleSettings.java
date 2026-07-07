package me.anomz.blockoutline.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    /** When RGB is on, flow the colors spatially from the block's corner instead of one uniform color. */
    public boolean outlineRgbGradient = false;
    /**
     * Draw the outline as camera-facing quads instead of GPU lines. Fixes the
     * width slider when a resource/shader pack overrides the line shader.
     */
    public boolean outlineQuadWidth = false;

    // Marching animation for the dashed style
    public boolean movingEnabled = false;
    public double movingSpeed = 1.0;

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

    /**
     * Colors the RGB effect cycles through, as "#RRGGBB" strings.
     * Empty = the full rainbow spectrum.
     */
    public List<String> rgbColors = new ArrayList<>();

    private transient int[] paletteCache;
    private transient String paletteCacheKey;

    /** Parsed {@link #rgbColors} as packed RGB ints (invalid entries skipped). */
    public int[] palette() {
        String key = String.join(",", rgbColors);
        if (paletteCache == null || !key.equals(paletteCacheKey)) {
            paletteCache = rgbColors.stream()
                    .map(StyleSettings::parseHex)
                    .filter(java.util.Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .toArray();
            paletteCacheKey = key;
        }
        return paletteCache;
    }

    /** Parses "#RRGGBB" or "RRGGBB" into a packed RGB int, or null. */
    public static Integer parseHex(String text) {
        if (text == null) {
            return null;
        }
        String hex = text.startsWith("#") ? text.substring(1) : text;
        if (hex.length() != 6) {
            return null;
        }
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

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
        c.outlineRgbGradient = outlineRgbGradient;
        c.outlineQuadWidth = outlineQuadWidth;
        c.movingEnabled = movingEnabled;
        c.movingSpeed = movingSpeed;
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
        c.rgbColors = new ArrayList<>(rgbColors);
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
        movingSpeed = clamp(movingSpeed, 0.01, 2.0);
        pulseSpeed = clamp(pulseSpeed, 0.01, 2.0);
        pulseMinOpacity = clamp(pulseMinOpacity, 0.0, 1.0);
        fillRed = clamp(fillRed);
        fillGreen = clamp(fillGreen);
        fillBlue = clamp(fillBlue);
        fillOpacity = clamp(fillOpacity, 0.0, 1.0);
        fillRgbSpeed = clamp(fillRgbSpeed, 0.1, 10.0);
        if (rgbColors == null) rgbColors = new ArrayList<>();
        List<String> cleaned = new ArrayList<>();
        for (String entry : rgbColors) {
            Integer rgb = parseHex(entry);
            if (rgb != null && cleaned.size() < 10) {
                cleaned.add(String.format(Locale.ROOT, "#%06X", rgb));
            }
        }
        rgbColors = cleaned;
        paletteCache = null;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
