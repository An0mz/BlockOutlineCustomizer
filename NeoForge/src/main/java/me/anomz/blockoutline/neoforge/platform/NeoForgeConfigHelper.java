package me.anomz.blockoutline.neoforge.platform;

import me.anomz.blockoutline.neoforge.config.OutlineConfig;
import me.anomz.blockoutline.platform.ConfigHelper;

/**
 * NeoForge implementation of ConfigHelper
 * Service loaded by Common module
 */
public class NeoForgeConfigHelper implements ConfigHelper {

    @Override
    public boolean isCustomOutlineEnabled() {
        return OutlineConfig.CUSTOM_OUTLINE_ENABLED.get();
    }

    @Override
    public void setCustomOutlineEnabled(boolean enabled) {
        OutlineConfig.CUSTOM_OUTLINE_ENABLED.set(enabled);
    }

    @Override
    public boolean isSyncRgb() {
        return OutlineConfig.SYNC_RGB.get();
    }

    @Override
    public void setSyncRgb(boolean sync) {
        OutlineConfig.SYNC_RGB.set(sync);
    }

    // Outline getters
    @Override
    public int getOutlineRed() {
        return OutlineConfig.OUTLINE_RED.get();
    }

    @Override
    public int getOutlineGreen() {
        return OutlineConfig.OUTLINE_GREEN.get();
    }

    @Override
    public int getOutlineBlue() {
        return OutlineConfig.OUTLINE_BLUE.get();
    }

    @Override
    public double getOutlineOpacity() {
        return OutlineConfig.OUTLINE_OPACITY.get();
    }

    @Override
    public double getOutlineWidth() {
        return OutlineConfig.OUTLINE_WIDTH.get();
    }

    @Override
    public boolean isOutlineRgbEnabled() {
        return OutlineConfig.OUTLINE_RGB_ENABLED.get();
    }

    @Override
    public double getOutlineRgbSpeed() {
        return OutlineConfig.OUTLINE_RGB_SPEED.get();
    }

    // Outline setters
    @Override
    public void setOutlineRed(int red) {
        OutlineConfig.OUTLINE_RED.set(red);
    }

    @Override
    public void setOutlineGreen(int green) {
        OutlineConfig.OUTLINE_GREEN.set(green);
    }

    @Override
    public void setOutlineBlue(int blue) {
        OutlineConfig.OUTLINE_BLUE.set(blue);
    }

    @Override
    public void setOutlineOpacity(double opacity) {
        OutlineConfig.OUTLINE_OPACITY.set(opacity);
    }

    @Override
    public void setOutlineWidth(double width) {
        OutlineConfig.OUTLINE_WIDTH.set(width);
    }

    @Override
    public void setOutlineRgbEnabled(boolean enabled) {
        OutlineConfig.OUTLINE_RGB_ENABLED.set(enabled);
    }

    @Override
    public void setOutlineRgbSpeed(double speed) {
        OutlineConfig.OUTLINE_RGB_SPEED.set(speed);
    }

    // Fill getters
    @Override
    public boolean isFillEnabled() {
        return OutlineConfig.FILL_ENABLED.get();
    }

    @Override
    public int getFillRed() {
        return OutlineConfig.FILL_RED.get();
    }

    @Override
    public int getFillGreen() {
        return OutlineConfig.FILL_GREEN.get();
    }

    @Override
    public int getFillBlue() {
        return OutlineConfig.FILL_BLUE.get();
    }

    @Override
    public double getFillOpacity() {
        return OutlineConfig.FILL_OPACITY.get();
    }

    @Override
    public boolean isFillRgbEnabled() {
        return OutlineConfig.FILL_RGB_ENABLED.get();
    }

    @Override
    public double getFillRgbSpeed() {
        return OutlineConfig.FILL_RGB_SPEED.get();
    }

    // Fill setters
    @Override
    public void setFillEnabled(boolean enabled) {
        OutlineConfig.FILL_ENABLED.set(enabled);
    }

    @Override
    public void setFillRed(int red) {
        OutlineConfig.FILL_RED.set(red);
    }

    @Override
    public void setFillGreen(int green) {
        OutlineConfig.FILL_GREEN.set(green);
    }

    @Override
    public void setFillBlue(int blue) {
        OutlineConfig.FILL_BLUE.set(blue);
    }

    @Override
    public void setFillOpacity(double opacity) {
        OutlineConfig.FILL_OPACITY.set(opacity);
    }

    @Override
    public void setFillRgbEnabled(boolean enabled) {
        OutlineConfig.FILL_RGB_ENABLED.set(enabled);
    }

    @Override
    public void setFillRgbSpeed(double speed) {
        OutlineConfig.FILL_RGB_SPEED.set(speed);
    }

    @Override
    public void save() {
        OutlineConfig.SPEC.save();
    }

    @Override
    public void resetToDefaults() {
        OutlineConfig.CUSTOM_OUTLINE_ENABLED.set(true);
        OutlineConfig.SYNC_RGB.set(false);
        OutlineConfig.OUTLINE_RED.set(0);
        OutlineConfig.OUTLINE_GREEN.set(255);
        OutlineConfig.OUTLINE_BLUE.set(255);
        OutlineConfig.OUTLINE_OPACITY.set(1.0);
        OutlineConfig.OUTLINE_WIDTH.set(3.0);
        OutlineConfig.OUTLINE_RGB_ENABLED.set(false);
        OutlineConfig.OUTLINE_RGB_SPEED.set(1.0);
        OutlineConfig.FILL_ENABLED.set(false);
        OutlineConfig.FILL_RED.set(255);
        OutlineConfig.FILL_GREEN.set(255);
        OutlineConfig.FILL_BLUE.set(255);
        OutlineConfig.FILL_OPACITY.set(0.3);
        OutlineConfig.FILL_RGB_ENABLED.set(false);
        OutlineConfig.FILL_RGB_SPEED.set(1.0);
        OutlineConfig.SPEC.save();
    }
}