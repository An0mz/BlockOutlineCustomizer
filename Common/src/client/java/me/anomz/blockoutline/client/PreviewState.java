package me.anomz.blockoutline.client;

import me.anomz.blockoutline.config.StyleSettings;

/**
 * Set by the config screen while it is open so the renderer shows the
 * unsaved widget values live in the world.
 */
public final class PreviewState {
    private PreviewState() {
    }

    /** Non-null while the config screen is open. */
    public static volatile StyleSettings style;
    /** Live value of the "Custom Outline" checkbox; only valid while {@link #style} != null. */
    public static volatile boolean customEnabled = true;
    /** Live values of the General-tab toggles; only valid while {@link #style} != null. */
    public static volatile boolean forceOutline;
    public static volatile boolean seeThrough;
    public static volatile boolean connectedBlocks;
    public static volatile boolean cubeOutline;
    public static volatile boolean blockInfo;

    public static void set(StyleSettings previewStyle, boolean enabled, boolean forceValue,
                           boolean seeThroughValue, boolean connectedValue,
                           boolean cubeValue, boolean blockInfoValue) {
        customEnabled = enabled;
        forceOutline = forceValue;
        seeThrough = seeThroughValue;
        connectedBlocks = connectedValue;
        cubeOutline = cubeValue;
        blockInfo = blockInfoValue;
        style = previewStyle;
    }

    public static void clear() {
        style = null;
    }
}
