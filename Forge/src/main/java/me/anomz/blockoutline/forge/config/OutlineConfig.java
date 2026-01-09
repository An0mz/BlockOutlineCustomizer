package me.anomz.blockoutline.forge.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class OutlineConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Outline settings
    public static final ForgeConfigSpec.IntValue OUTLINE_RED;
    public static final ForgeConfigSpec.IntValue OUTLINE_GREEN;
    public static final ForgeConfigSpec.IntValue OUTLINE_BLUE;
    public static final ForgeConfigSpec.DoubleValue OUTLINE_OPACITY;
    public static final ForgeConfigSpec.DoubleValue OUTLINE_WIDTH;
    public static final ForgeConfigSpec.BooleanValue OUTLINE_RGB_ENABLED;
    public static final ForgeConfigSpec.DoubleValue OUTLINE_RGB_SPEED;

    // Fill settings
    public static final ForgeConfigSpec.BooleanValue FILL_ENABLED;
    public static final ForgeConfigSpec.IntValue FILL_RED;
    public static final ForgeConfigSpec.IntValue FILL_GREEN;
    public static final ForgeConfigSpec.IntValue FILL_BLUE;
    public static final ForgeConfigSpec.DoubleValue FILL_OPACITY;
    public static final ForgeConfigSpec.BooleanValue FILL_RGB_ENABLED;
    public static final ForgeConfigSpec.DoubleValue FILL_RGB_SPEED;

    static {
        BUILDER.comment("Block Outline Customizer Configuration").push("outline");

        // Outline settings
        OUTLINE_RED = BUILDER
                .comment("Red component (0-255)")
                .defineInRange("red", 0, 0, 255);

        OUTLINE_GREEN = BUILDER
                .comment("Green component (0-255)")
                .defineInRange("green", 255, 0, 255);

        OUTLINE_BLUE = BUILDER
                .comment("Blue component (0-255)")
                .defineInRange("blue", 255, 0, 255);

        OUTLINE_OPACITY = BUILDER
                .comment("Opacity (0.0-1.0)")
                .defineInRange("opacity", 1.0, 0.0, 1.0);

        OUTLINE_WIDTH = BUILDER
                .comment("Line width (1.0-10.0)")
                .defineInRange("width", 3.0, 1.0, 10.0);

        OUTLINE_RGB_ENABLED = BUILDER
                .comment("Enable rainbow effect")
                .define("rgbEnabled", false);

        OUTLINE_RGB_SPEED = BUILDER
                .comment("Rainbow animation speed (0.1-10.0)")
                .defineInRange("rgbSpeed", 1.0, 0.1, 10.0);

        BUILDER.pop();
        BUILDER.push("fill");

        // Fill settings
        FILL_ENABLED = BUILDER
                .comment("Enable fill rendering")
                .define("enabled", false);

        FILL_RED = BUILDER
                .comment("Red component (0-255)")
                .defineInRange("red", 255, 0, 255);

        FILL_GREEN = BUILDER
                .comment("Green component (0-255)")
                .defineInRange("green", 255, 0, 255);

        FILL_BLUE = BUILDER
                .comment("Blue component (0-255)")
                .defineInRange("blue", 255, 0, 255);

        FILL_OPACITY = BUILDER
                .comment("Opacity (0.0-1.0)")
                .defineInRange("opacity", 0.3, 0.0, 1.0);

        FILL_RGB_ENABLED = BUILDER
                .comment("Enable rainbow effect")
                .define("rgbEnabled", false);

        FILL_RGB_SPEED = BUILDER
                .comment("Rainbow animation speed (0.1-10.0)")
                .defineInRange("rgbSpeed", 1.0, 0.1, 10.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}