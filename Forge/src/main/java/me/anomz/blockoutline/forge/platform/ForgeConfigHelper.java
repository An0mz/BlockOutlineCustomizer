package me.anomz.blockoutline.forge.platform;

import me.anomz.blockoutline.forge.config.OutlineConfig;
import me.anomz.blockoutline.platform.ConfigHelper;

public class ForgeConfigHelper implements ConfigHelper {

    @Override
    public int getOutlineRed() {
        return OutlineConfig.OUTLINE_RED.get();
    }

    @Override
    public void setOutlineRed(int red) {
        OutlineConfig.OUTLINE_RED.set(red);
    }

    @Override
    public int getOutlineGreen() {
        return OutlineConfig.OUTLINE_GREEN.get();
    }

    @Override
    public void setOutlineGreen(int green) {
        OutlineConfig.OUTLINE_GREEN.set(green);
    }

    @Override
    public int getOutlineBlue() {
        return OutlineConfig.OUTLINE_BLUE.get();
    }

    @Override
    public void setOutlineBlue(int blue) {
        OutlineConfig.OUTLINE_BLUE.set(blue);
    }

    @Override
    public double getOutlineOpacity() {
        return OutlineConfig.OUTLINE_OPACITY.get();
    }

    @Override
    public void setOutlineOpacity(double opacity) {
        OutlineConfig.OUTLINE_OPACITY.set(opacity);
    }

    @Override
    public double getOutlineWidth() {
        return OutlineConfig.OUTLINE_WIDTH.get();
    }

    @Override
    public void setOutlineWidth(double width) {
        OutlineConfig.OUTLINE_WIDTH.set(width);
    }

    @Override
    public boolean isOutlineRgbEnabled() {
        return OutlineConfig.OUTLINE_RGB_ENABLED.get();
    }

    @Override
    public void setOutlineRgbEnabled(boolean enabled) {
        OutlineConfig.OUTLINE_RGB_ENABLED.set(enabled);
    }

    @Override
    public double getOutlineRgbSpeed() {
        return OutlineConfig.OUTLINE_RGB_SPEED.get();
    }

    @Override
    public void setOutlineRgbSpeed(double speed) {
        OutlineConfig.OUTLINE_RGB_SPEED.set(speed);
    }

    @Override
    public boolean isFillEnabled() {
        return OutlineConfig.FILL_ENABLED.get();
    }

    @Override
    public void setFillEnabled(boolean enabled) {
        OutlineConfig.FILL_ENABLED.set(enabled);
    }

    @Override
    public int getFillRed() {
        return OutlineConfig.FILL_RED.get();
    }

    @Override
    public void setFillRed(int red) {
        OutlineConfig.FILL_RED.set(red);
    }

    @Override
    public int getFillGreen() {
        return OutlineConfig.FILL_GREEN.get();
    }

    @Override
    public void setFillGreen(int green) {
        OutlineConfig.FILL_GREEN.set(green);
    }

    @Override
    public int getFillBlue() {
        return OutlineConfig.FILL_BLUE.get();
    }

    @Override
    public void setFillBlue(int blue) {
        OutlineConfig.FILL_BLUE.set(blue);
    }

    @Override
    public double getFillOpacity() {
        return OutlineConfig.FILL_OPACITY.get();
    }

    @Override
    public void setFillOpacity(double opacity) {
        OutlineConfig.FILL_OPACITY.set(opacity);
    }

    @Override
    public boolean isFillRgbEnabled() {
        return OutlineConfig.FILL_RGB_ENABLED.get();
    }

    @Override
    public void setFillRgbEnabled(boolean enabled) {
        OutlineConfig.FILL_RGB_ENABLED.set(enabled);
    }

    @Override
    public double getFillRgbSpeed() {
        return OutlineConfig.FILL_RGB_SPEED.get();
    }

    @Override
    public void setFillRgbSpeed(double speed) {
        OutlineConfig.FILL_RGB_SPEED.set(speed);
    }

    @Override
    public void save() {
        OutlineConfig.SPEC.save();
    }
}
