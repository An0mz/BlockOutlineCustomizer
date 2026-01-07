package me.anomz.blockoutline.neoforge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class OutlineConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Outline settings
    public static final ModConfigSpec.IntValue OUTLINE_RED;
    public static final ModConfigSpec.IntValue OUTLINE_GREEN;
    public static final ModConfigSpec.IntValue OUTLINE_BLUE;
    public static final ModConfigSpec.DoubleValue OUTLINE_OPACITY;
    public static final ModConfigSpec.DoubleValue OUTLINE_WIDTH;
    public static final ModConfigSpec.BooleanValue OUTLINE_RGB_ENABLED;
    public static final ModConfigSpec.DoubleValue OUTLINE_RGB_SPEED;

    // Fill settings
    public static final ModConfigSpec.BooleanValue FILL_ENABLED;
    public static final ModConfigSpec.IntValue FILL_RED;
    public static final ModConfigSpec.IntValue FILL_GREEN;
    public static final ModConfigSpec.IntValue FILL_BLUE;
    public static final ModConfigSpec.DoubleValue FILL_OPACITY;
    public static final ModConfigSpec.BooleanValue FILL_RGB_ENABLED;
    public static final ModConfigSpec.DoubleValue FILL_RGB_SPEED;

    static {
        BUILDER.push("Outline Settings");

        OUTLINE_RED = BUILDER.comment("Outline red color value (0-255)")
                .defineInRange("outlineRed", 0, 0, 255);

        OUTLINE_GREEN = BUILDER.comment("Outline green color value (0-255)")
                .defineInRange("outlineGreen", 255, 0, 255);

        OUTLINE_BLUE = BUILDER.comment("Outline blue color value (0-255)")
                .defineInRange("outlineBlue", 255, 0, 255);

        OUTLINE_OPACITY = BUILDER.comment("Outline opacity (0.0-1.0)")
                .defineInRange("outlineOpacity", 1.0, 0.0, 1.0);

        OUTLINE_WIDTH = BUILDER.comment("Outline width (1.0-10.0)")
                .defineInRange("outlineWidth", 3.0, 1.0, 10.0);

        OUTLINE_RGB_ENABLED = BUILDER.comment("Enable RGB rainbow mode for outline")
                .define("outlineRgbEnabled", false);

        OUTLINE_RGB_SPEED = BUILDER.comment("Outline RGB animation speed (0.1-10.0)")
                .defineInRange("outlineRgbSpeed", 1.0, 0.1, 10.0);

        BUILDER.pop();

        BUILDER.push("Fill Settings");

        FILL_ENABLED = BUILDER.comment("Enable block fill")
                .define("fillEnabled", false);

        FILL_RED = BUILDER.comment("Fill red color value (0-255)")
                .defineInRange("fillRed", 255, 0, 255);

        FILL_GREEN = BUILDER.comment("Fill green color value (0-255)")
                .defineInRange("fillGreen", 255, 0, 255);

        FILL_BLUE = BUILDER.comment("Fill blue color value (0-255)")
                .defineInRange("fillBlue", 255, 0, 255);

        FILL_OPACITY = BUILDER.comment("Fill opacity (0.0-1.0)")
                .defineInRange("fillOpacity", 0.3, 0.0, 1.0);

        FILL_RGB_ENABLED = BUILDER.comment("Enable RGB rainbow mode for fill")
                .define("fillRgbEnabled", false);

        FILL_RGB_SPEED = BUILDER.comment("Fill RGB animation speed (0.1-10.0)")
                .defineInRange("fillRgbSpeed", 1.0, 0.1, 10.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}