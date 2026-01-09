package me.anomz.blockoutline.forge.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class ColorSlider extends AbstractSliderButton {
    private final Component prefix;
    private int colorValue;

    public ColorSlider(int x, int y, int width, int height, Component prefix, int initialValue) {
        super(x, y, width, height, Component.empty(), initialValue / 255.0);
        this.prefix = prefix;
        this.colorValue = initialValue;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal(prefix.getString() + colorValue));
    }

    @Override
    protected void applyValue() {
        this.colorValue = (int)(this.value * 255);
    }

    public int getValueInt() {
        return colorValue;
    }
}