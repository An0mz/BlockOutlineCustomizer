package me.anomz.blockoutline.fabric.platform;

import me.anomz.blockoutline.fabric.config.OutlineConfig;
import me.anomz.blockoutline.platform.ConfigHelper;

public class FabricConfigHelper implements ConfigHelper {
    private static final OutlineConfig CONFIG = OutlineConfig.load();

    @Override
    public int getRed() {
        return CONFIG.red;
    }

    @Override
    public int getGreen() {
        return CONFIG.green;
    }

    @Override
    public int getBlue() {
        return CONFIG.blue;
    }

    @Override
    public double getOpacity() {
        return CONFIG.opacity;
    }

    @Override
    public double getWidth() {
        return CONFIG.width;
    }

    @Override
    public boolean isRgbEnabled() {
        return CONFIG.rgbEnabled;
    }

    @Override
    public double getRgbSpeed() {
        return CONFIG.rgbSpeed;
    }

    @Override
    public void setRed(int red) {
        CONFIG.red = red;
    }

    @Override
    public void setGreen(int green) {
        CONFIG.green = green;
    }

    @Override
    public void setBlue(int blue) {
        CONFIG.blue = blue;
    }

    @Override
    public void setOpacity(double opacity) {
        CONFIG.opacity = opacity;
    }

    @Override
    public void setWidth(double width) {
        CONFIG.width = width;
    }

    @Override
    public void setRgbEnabled(boolean enabled) {
        CONFIG.rgbEnabled = enabled;
    }

    @Override
    public void setRgbSpeed(double speed) {
        CONFIG.rgbSpeed = speed;
    }

    @Override
    public void save() {
        CONFIG.save();
    }
}