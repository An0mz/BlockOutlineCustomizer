package me.anomz.blockoutline.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class OutlineConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue RED;
    public static final ModConfigSpec.IntValue GREEN;
    public static final ModConfigSpec.IntValue BLUE;
    public static final ModConfigSpec.DoubleValue OPACITY;
    public static final ModConfigSpec.DoubleValue WIDTH;
    public static final ModConfigSpec.BooleanValue RGB_ENABLED;
    public static final ModConfigSpec.DoubleValue RGB_SPEED;

    static {
        BUILDER.push("Block Outline Customizer Config");

        RED = BUILDER
                .comment("Red color component (0-255)")
                .defineInRange("red", 0, 0, 255);

        GREEN = BUILDER
                .comment("Green color component (0-255)")
                .defineInRange("green", 0, 0, 255);

        BLUE = BUILDER
                .comment("Blue color component (0-255)")
                .defineInRange("blue", 0, 0, 255);

        OPACITY = BUILDER
                .comment("Opacity of the outline (0.0-1.0)")
                .defineInRange("opacity", 0.4, 0.0, 1.0);

        WIDTH = BUILDER
                .comment("Width of the outline (1.0-10.0)")
                .defineInRange("width", 2.0, 1.0, 10.0);

        RGB_ENABLED = BUILDER
                .comment("Enable RGB rainbow effect")
                .define("rgbEnabled", false);

        RGB_SPEED = BUILDER
                .comment("Speed of RGB effect (0.1-10.0)")
                .defineInRange("rgbSpeed", 1.0, 0.1, 10.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
