package me.anomz.blockoutline.fabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutlineConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("blockoutlinecustomizer.json");

    // Outline settings
    public int outlineRed = 0;
    public int outlineGreen = 255;
    public int outlineBlue = 255;
    public double outlineOpacity = 1.0;
    public double outlineWidth = 3.0;
    public boolean outlineRgbEnabled = false;
    public double outlineRgbSpeed = 1.0;

    // Fill settings
    public boolean fillEnabled = false;
    public int fillRed = 255;
    public int fillGreen = 255;
    public int fillBlue = 255;
    public double fillOpacity = 0.3;
    public boolean fillRgbEnabled = false;
    public double fillRgbSpeed = 1.0;

    public static OutlineConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                return GSON.fromJson(json, OutlineConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new OutlineConfig();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}