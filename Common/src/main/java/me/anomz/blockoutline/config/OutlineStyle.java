package me.anomz.blockoutline.config;

/**
 * How the outline lines are drawn.
 */
public enum OutlineStyle {
    FULL,
    DASHED,
    CORNERS;

    public String translationKey() {
        return "blockoutlinecustomizer.style." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
