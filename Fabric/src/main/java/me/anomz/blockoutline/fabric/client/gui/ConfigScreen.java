package me.anomz.blockoutline.fabric.client.gui;

import me.anomz.blockoutline.platform.ConfigHelper;
import me.anomz.blockoutline.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private final Screen lastScreen;
    private final ConfigHelper config;

    // Sliders
    private ColorSlider redSlider;
    private ColorSlider greenSlider;
    private ColorSlider blueSlider;
    private OpacitySlider opacitySlider;
    private WidthSlider widthSlider;
    private RGBSpeedSlider rgbSpeedSlider;
    private boolean rgbEnabled;

    public ConfigScreen(Screen lastScreen) {
        super(Component.literal("Block Outline Settings"));
        this.lastScreen = lastScreen;
        this.config = Services.getConfigHelper();
        this.rgbEnabled = config.isRgbEnabled();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;

        int sliderWidth = Math.min(300, this.width - 60);

        int availableHeight = this.height - 100;
        int itemCount = 7;
        int spacing = Math.min(30, availableHeight / itemCount);

        int startY = Math.max(50, (this.height - (itemCount * spacing)) / 2);

        // Red slider
        this.redSlider = new ColorSlider(
                centerX - sliderWidth / 2,
                startY,
                sliderWidth,
                20,
                "Red",
                config.getRed()
        );
        this.addRenderableWidget(redSlider);

        // Green slider
        this.greenSlider = new ColorSlider(
                centerX - sliderWidth / 2,
                startY + spacing,
                sliderWidth,
                20,
                "Green",
                config.getGreen()
        );
        this.addRenderableWidget(greenSlider);

        // Blue slider
        this.blueSlider = new ColorSlider(
                centerX - sliderWidth / 2,
                startY + spacing * 2,
                sliderWidth,
                20,
                "Blue",
                config.getBlue()
        );
        this.addRenderableWidget(blueSlider);

        // Opacity slider
        this.opacitySlider = new OpacitySlider(
                centerX - sliderWidth / 2,
                startY + spacing * 3,
                sliderWidth,
                20,
                config.getOpacity()
        );
        this.addRenderableWidget(opacitySlider);

        // Width slider
        this.widthSlider = new WidthSlider(
                centerX - sliderWidth / 2,
                startY + spacing * 4,
                sliderWidth,
                20,
                config.getWidth()
        );
        this.addRenderableWidget(widthSlider);

        // RGB Checkbox - centered
        Component checkboxText = Component.literal("Enable RGB");
        int checkboxWidth = this.font.width(checkboxText) + 24; // 24 for checkbox itself
        this.addRenderableWidget(Checkbox.builder(checkboxText, this.font)
                .pos(centerX - checkboxWidth / 2, startY + spacing * 5)
                .selected(rgbEnabled)
                .onValueChange((checkbox, selected) -> rgbEnabled = selected)
                .build());

        // RGB Speed slider
        this.rgbSpeedSlider = new RGBSpeedSlider(
                centerX - sliderWidth / 2,
                startY + spacing * 6,
                sliderWidth,
                20,
                config.getRgbSpeed()
        );
        this.addRenderableWidget(rgbSpeedSlider);

        // Done button - responsive width and position
        int buttonWidth = Math.min(200, this.width - 40);
        int buttonY = Math.min(this.height - 30, startY + spacing * 7 + 10);

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> saveAndClose())
                .bounds(centerX - buttonWidth / 2, buttonY, buttonWidth, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Solid background
        graphics.fill(0, 0, this.width, this.height, 0xC0101010);

        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw title - responsive position
        int centerX = this.width / 2;
        String text = "Block Outline Settings";
        int textWidth = this.font.width(text);
        int textX = centerX - (textWidth / 2);
        int titleY = Math.min(30, this.height / 8); // Adaptive title position

        graphics.drawString(this.font, text, textX, titleY, 0xFFFFFFFF, true);
    }

    private void saveAndClose() {
        config.setRed(redSlider.getIntValue());
        config.setGreen(greenSlider.getIntValue());
        config.setBlue(blueSlider.getIntValue());
        config.setOpacity(opacitySlider.getValue());
        config.setWidth(widthSlider.getValue());
        config.setRgbEnabled(rgbEnabled);
        config.setRgbSpeed(rgbSpeedSlider.getValue());
        config.save();

        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    // Custom slider classes
    private static class ColorSlider extends AbstractSliderButton {
        private final String label;

        public ColorSlider(int x, int y, int width, int height, String label, int initialValue) {
            super(x, y, width, height, Component.literal(label + ": " + initialValue), initialValue / 255.0);
            this.label = label;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(label + ": " + getIntValue()));
        }

        @Override
        protected void applyValue() {
        }

        public int getIntValue() {
            return (int)(this.value * 255);
        }
    }

    private static class OpacitySlider extends AbstractSliderButton {
        public OpacitySlider(int x, int y, int width, int height, double initialValue) {
            super(x, y, width, height, Component.literal(String.format("Opacity: %.2f", initialValue)), initialValue);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(String.format("Opacity: %.2f", getValue())));
        }

        @Override
        protected void applyValue() {
        }

        public double getValue() {
            return this.value;
        }
    }

    private static class WidthSlider extends AbstractSliderButton {
        public WidthSlider(int x, int y, int width, int height, double initialValue) {
            super(x, y, width, height, Component.literal(String.format("Width: %.1f", initialValue)),
                    (initialValue - 1.0) / 9.0);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(String.format("Width: %.1f", getValue())));
        }

        @Override
        protected void applyValue() {
        }

        public double getValue() {
            return 1.0 + (this.value * 9.0);
        }
    }

    private static class RGBSpeedSlider extends AbstractSliderButton {
        public RGBSpeedSlider(int x, int y, int width, int height, double initialValue) {
            super(x, y, width, height, Component.literal(String.format("RGB Speed: %.1f", initialValue)),
                    (initialValue - 0.1) / 9.9);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(String.format("RGB Speed: %.1f", getValue())));
        }

        @Override
        protected void applyValue() {
        }

        public double getValue() {
            return 0.1 + (this.value * 9.9);
        }
    }
}