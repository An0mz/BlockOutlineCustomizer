package me.anomz.blockoutline.fabric.platform;

import me.anomz.blockoutline.fabric.config.OutlineConfig;
import me.anomz.blockoutline.platform.ConfigHelper;

public class FabricConfigHelper implements ConfigHelper {
    private static final OutlineConfig CONFIG = OutlineConfig.load();

    @Override
    public int getOutlineRed() {
        return CONFIG.outlineRed;
    }

    @Override
    public int getOutlineGreen() {
        return CONFIG.outlineGreen;
    }

    @Override
    public int getOutlineBlue() {
        return CONFIG.outlineBlue;
    }

    @Override
    public double getOutlineOpacity() {
        return CONFIG.outlineOpacity;
    }

    @Override
    public double getOutlineWidth() {
        return CONFIG.outlineWidth;
    }

    @Override
    public boolean isOutlineRgbEnabled() {
        return CONFIG.outlineRgbEnabled;
    }

    @Override
    public double getOutlineRgbSpeed() {
        return CONFIG.outlineRgbSpeed;
    }

    // Outline setters
    @Override
    public void setOutlineRed(int red) {
        CONFIG.outlineRed = red;
    }

    @Override
    public void setOutlineGreen(int green) {
        CONFIG.outlineGreen = green;
    }

    @Override
    public void setOutlineBlue(int blue) {
        CONFIG.outlineBlue = blue;
    }

    @Override
    public void setOutlineOpacity(double opacity) {
        CONFIG.outlineOpacity = opacity;
    }

    @Override
    public void setOutlineWidth(double width) {
        CONFIG.outlineWidth = width;
    }

    @Override
    public void setOutlineRgbEnabled(boolean enabled) {
        CONFIG.outlineRgbEnabled = enabled;
    }

    @Override
    public void setOutlineRgbSpeed(double speed) {
        CONFIG.outlineRgbSpeed = speed;
    }

    // Fill getters
    @Override
    public boolean isFillEnabled() {
        return CONFIG.fillEnabled;
    }

    @Override
    public int getFillRed() {
        return CONFIG.fillRed;
    }

    @Override
    public int getFillGreen() {
        return CONFIG.fillGreen;
    }

    @Override
    public int getFillBlue() {
        return CONFIG.fillBlue;
    }

    @Override
    public double getFillOpacity() {
        return CONFIG.fillOpacity;
    }

    @Override
    public boolean isFillRgbEnabled() {
        return CONFIG.fillRgbEnabled;
    }

    @Override
    public double getFillRgbSpeed() {
        return CONFIG.fillRgbSpeed;
    }

    // Fill setters
    @Override
    public void setFillEnabled(boolean enabled) {
        CONFIG.fillEnabled = enabled;
    }

    @Override
    public void setFillRed(int red) {
        CONFIG.fillRed = red;
    }

    @Override
    public void setFillGreen(int green) {
        CONFIG.fillGreen = green;
    }

    @Override
    public void setFillBlue(int blue) {
        CONFIG.fillBlue = blue;
    }

    @Override
    public void setFillOpacity(double opacity) {
        CONFIG.fillOpacity = opacity;
    }

    @Override
    public void setFillRgbEnabled(boolean enabled) {
        CONFIG.fillRgbEnabled = enabled;
    }

    @Override
    public void setFillRgbSpeed(double speed) {
        CONFIG.fillRgbSpeed = speed;
    }

    @Override
    public void save() {
        CONFIG.save();
    }
}