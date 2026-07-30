package me.anomz.blockoutline.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.anomz.blockoutline.Constants;
import me.anomz.blockoutline.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The mod's single JSON config, shared by Fabric and NeoForge
 * (config/blockoutlinecustomizer.json).
 */
public class BOCConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final String PRESET_CLASSIC = "classic";
    public static final String PRESET_NEON = "neon";
    public static final String PRESET_SUBTLE = "subtle";
    public static final String PRESET_RAINBOW = "rainbow";
    public static final String PRESET_CUSTOM = "custom";
    public static final List<String> BUILTIN_PRESETS =
            List.of(PRESET_CLASSIC, PRESET_NEON, PRESET_SUBTLE, PRESET_RAINBOW);

    public boolean customOutlineEnabled = true;
    /** Also replace outlines drawn by other mods' custom highlight renderers (e.g. AE2 cables). */
    public boolean forceOutline = false;
    /** Render the outline (and fill) through walls. */
    public boolean seeThrough = false;
    /** Outline both halves of doors, beds, chests, tall plants and pistons as one shape. */
    public boolean connectedBlocks = false;
    /** Always outline the full block cube instead of the block's actual shape. */
    public boolean cubeOutline = false;
    /** Show a floating info tag (name, position, state) above the targeted block. */
    public boolean blockInfo = false;
    public StyleSettings style = new StyleSettings();
    public LinkedHashMap<String, StyleSettings> presets = new LinkedHashMap<>();
    public List<BlockOverride> blockOverrides = new ArrayList<>();

    /** Bumped on every save so caches (override matchers) can invalidate. */
    public transient int generation = 0;

    private static BOCConfig instance;

    public static BOCConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static Path configPath() {
        return Services.PLATFORM.getConfigDir().resolve(Constants.MOD_ID + ".json");
    }

    private static BOCConfig load() {
        BOCConfig config = null;
        Path path = configPath();
        if (Files.exists(path)) {
            try {
                JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if (json.has("outlineRed") && !json.has("style")) {
                    config = migrateLegacy(json);
                } else {
                    config = GSON.fromJson(json, BOCConfig.class);
                }
            } catch (IOException | RuntimeException e) {
                Constants.LOGGER.error("Failed to read config, using defaults", e);
            }
        }
        if (config == null) {
            config = new BOCConfig();
        }
        config.sanitize();
        config.save();
        return config;
    }

    /** Converts the pre-1.4 flat config format. */
    private static BOCConfig migrateLegacy(JsonObject json) {
        BOCConfig config = new BOCConfig();
        StyleSettings s = config.style;
        config.customOutlineEnabled = getBool(json, "customOutlineEnabled", true);
        s.syncRgb = getBool(json, "syncRgb", s.syncRgb);
        s.outlineRed = getInt(json, "outlineRed", s.outlineRed);
        s.outlineGreen = getInt(json, "outlineGreen", s.outlineGreen);
        s.outlineBlue = getInt(json, "outlineBlue", s.outlineBlue);
        s.outlineOpacity = getDouble(json, "outlineOpacity", s.outlineOpacity);
        s.outlineWidth = getDouble(json, "outlineWidth", s.outlineWidth);
        s.outlineRgbEnabled = getBool(json, "outlineRgbEnabled", s.outlineRgbEnabled);
        s.outlineRgbSpeed = getDouble(json, "outlineRgbSpeed", s.outlineRgbSpeed);
        s.fillEnabled = getBool(json, "fillEnabled", s.fillEnabled);
        s.fillRed = getInt(json, "fillRed", s.fillRed);
        s.fillGreen = getInt(json, "fillGreen", s.fillGreen);
        s.fillBlue = getInt(json, "fillBlue", s.fillBlue);
        s.fillOpacity = getDouble(json, "fillOpacity", s.fillOpacity);
        s.fillRgbEnabled = getBool(json, "fillRgbEnabled", s.fillRgbEnabled);
        s.fillRgbSpeed = getDouble(json, "fillRgbSpeed", s.fillRgbSpeed);
        Constants.LOGGER.info("Migrated legacy config to the new format");
        return config;
    }

    public void save() {
        generation++;
        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(this));
        } catch (IOException e) {
            Constants.LOGGER.error("Failed to save config", e);
        }
    }

    public void sanitize() {
        if (style == null) style = new StyleSettings();
        style.sanitize();
        if (presets == null) presets = new LinkedHashMap<>();
        presets.values().removeIf(java.util.Objects::isNull);
        presets.values().forEach(StyleSettings::sanitize);
        addBuiltinPresets(presets);
        if (blockOverrides == null) blockOverrides = new ArrayList<>();
        blockOverrides.removeIf(java.util.Objects::isNull);
        blockOverrides.forEach(BlockOverride::sanitize);
        if (blockOverrides.isEmpty()) {
            blockOverrides.add(exampleOverride());
        }
    }

    private static void addBuiltinPresets(LinkedHashMap<String, StyleSettings> presets) {
        presets.computeIfAbsent(PRESET_CLASSIC, k -> {
            StyleSettings s = new StyleSettings();
            s.outlineRed = 0;
            s.outlineGreen = 0;
            s.outlineBlue = 0;
            s.outlineOpacity = 0.4;
            s.outlineWidth = 1.0;
            return s;
        });
        presets.computeIfAbsent(PRESET_NEON, k -> {
            StyleSettings s = new StyleSettings();
            s.outlineRed = 0;
            s.outlineGreen = 255;
            s.outlineBlue = 255;
            s.outlineOpacity = 1.0;
            s.outlineWidth = 3.0;
            return s;
        });
        presets.computeIfAbsent(PRESET_SUBTLE, k -> {
            StyleSettings s = new StyleSettings();
            s.outlineRed = 255;
            s.outlineGreen = 255;
            s.outlineBlue = 255;
            s.outlineOpacity = 0.35;
            s.outlineWidth = 2.0;
            return s;
        });
        presets.computeIfAbsent(PRESET_RAINBOW, k -> {
            StyleSettings s = new StyleSettings();
            s.outlineRgbEnabled = true;
            s.outlineWidth = 3.0;
            s.syncRgb = true;
            return s;
        });
    }

    private static BlockOverride exampleOverride() {
        BlockOverride override = new BlockOverride();
        override.name = "Ores (example - set enabled to true)";
        override.enabled = false;
        override.blocks.add("#c:ores");
        override.style.outlineRed = 255;
        override.style.outlineGreen = 170;
        override.style.outlineBlue = 0;
        override.style.outlineWidth = 3.0;
        return override;
    }

    public void resetStyleToDefaults() {
        customOutlineEnabled = true;
        style = new StyleSettings();
    }

    private static boolean getBool(JsonObject json, String key, boolean def) {
        return json.has(key) ? json.get(key).getAsBoolean() : def;
    }

    private static int getInt(JsonObject json, String key, int def) {
        return json.has(key) ? json.get(key).getAsInt() : def;
    }

    private static double getDouble(JsonObject json, String key, double def) {
        return json.has(key) ? json.get(key).getAsDouble() : def;
    }
}
