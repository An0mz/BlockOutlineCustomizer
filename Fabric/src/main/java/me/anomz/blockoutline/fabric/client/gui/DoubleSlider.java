package me.anomz.blockoutline.fabric.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class DoubleSlider extends AbstractSliderButton {
    private final Component prefix;
    private final double minValue;
    private final double maxValue;
    private final double step;
    private double currentValue;

    public DoubleSlider(int x, int y, int width, int height, Component prefix,
                        double minValue, double maxValue, double initialValue, double step) {
        super(x, y, width, height, Component.empty(),
                (initialValue - minValue) / (maxValue - minValue));
        this.prefix = prefix;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        this.currentValue = initialValue;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal(
                prefix.getString() + String.format("%.2f", currentValue)
        ));
    }

    @Override
    protected void applyValue() {
        double rawValue = minValue + (value * (maxValue - minValue));
        this.currentValue = Math.round(rawValue / step) * step;
        this.currentValue = Math.max(minValue, Math.min(maxValue, currentValue));
    }

    public double getValue() {
        return currentValue;
    }
}