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

    public int red = 0;
    public int green = 255;
    public int blue = 255;
    public double opacity = 1.0;
    public double width = 3.0;
    public boolean rgbEnabled = false;
    public double rgbSpeed = 1.0;

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