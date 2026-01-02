package me.anomz.blockoutline.platform;

/**
 * Platform-agnostic config interface
 * Implemented differently by Fabric and NeoForge
 */
public interface ConfigHelper {
    int getRed();
    void setRed(int value);

    int getGreen();
    void setGreen(int value);

    int getBlue();
    void setBlue(int value);

    double getOpacity();
    void setOpacity(double value);

    double getWidth();
    void setWidth(double value);

    boolean isRgbEnabled();
    void setRgbEnabled(boolean value);

    double getRgbSpeed();
    void setRgbSpeed(double value);

    void save();
}