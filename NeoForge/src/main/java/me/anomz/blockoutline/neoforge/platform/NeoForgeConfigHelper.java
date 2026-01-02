package me.anomz.blockoutline.neoforge.platform;

import me.anomz.blockoutline.neoforge.config.OutlineConfig;
import me.anomz.blockoutline.platform.ConfigHelper;

/**
 * NeoForge implementation of ConfigHelper
 * Service loaded by Common module
 */
public class NeoForgeConfigHelper implements ConfigHelper {

    @Override
    public int getRed() {
        return OutlineConfig.RED.get();
    }

    @Override
    public void setRed(int value) {
        OutlineConfig.RED.set(value);
    }

    @Override
    public int getGreen() {
        return OutlineConfig.GREEN.get();
    }

    @Override
    public void setGreen(int value) {
        OutlineConfig.GREEN.set(value);
    }

    @Override
    public int getBlue() {
        return OutlineConfig.BLUE.get();
    }

    @Override
    public void setBlue(int value) {
        OutlineConfig.BLUE.set(value);
    }

    @Override
    public double getOpacity() {
        return OutlineConfig.OPACITY.get();
    }

    @Override
    public void setOpacity(double value) {
        OutlineConfig.OPACITY.set(value);
    }

    @Override
    public double getWidth() {
        return OutlineConfig.WIDTH.get();
    }

    @Override
    public void setWidth(double value) {
        OutlineConfig.WIDTH.set(value);
    }

    @Override
    public boolean isRgbEnabled() {
        return OutlineConfig.RGB_ENABLED.get();
    }

    @Override
    public void setRgbEnabled(boolean value) {
        OutlineConfig.RGB_ENABLED.set(value);
    }

    @Override
    public double getRgbSpeed() {
        return OutlineConfig.RGB_SPEED.get();
    }

    @Override
    public void setRgbSpeed(double value) {
        OutlineConfig.RGB_SPEED.set(value);
    }

    @Override
    public void save() {
        OutlineConfig.SPEC.save();
    }
}