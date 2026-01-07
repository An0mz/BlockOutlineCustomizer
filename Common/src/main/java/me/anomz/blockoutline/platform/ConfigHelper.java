package me.anomz.blockoutline.platform;

public interface ConfigHelper {
    // Outline settings
    int getOutlineRed();
    int getOutlineGreen();
    int getOutlineBlue();
    double getOutlineOpacity();
    double getOutlineWidth();
    boolean isOutlineRgbEnabled();
    double getOutlineRgbSpeed();

    void setOutlineRed(int red);
    void setOutlineGreen(int green);
    void setOutlineBlue(int blue);
    void setOutlineOpacity(double opacity);
    void setOutlineWidth(double width);
    void setOutlineRgbEnabled(boolean enabled);
    void setOutlineRgbSpeed(double speed);

    // Fill settings
    boolean isFillEnabled();
    int getFillRed();
    int getFillGreen();
    int getFillBlue();
    double getFillOpacity();
    boolean isFillRgbEnabled();
    double getFillRgbSpeed();

    void setFillEnabled(boolean enabled);
    void setFillRed(int red);
    void setFillGreen(int green);
    void setFillBlue(int blue);
    void setFillOpacity(double opacity);
    void setFillRgbEnabled(boolean enabled);
    void setFillRgbSpeed(double speed);

    void save();
}