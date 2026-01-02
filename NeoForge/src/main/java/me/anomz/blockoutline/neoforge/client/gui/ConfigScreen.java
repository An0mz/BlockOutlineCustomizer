package me.anomz.blockoutline.neoforge.client.gui;

import me.anomz.blockoutline.neoforge.config.OutlineConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private final Screen lastScreen;

    // Color sliders
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
        this.rgbEnabled = OutlineConfig.RGB_ENABLED.get();
    }

    @Override
    protected void init() {
        super.init();

        // Use scaled screen dimensions (respects GUI Scale setting)
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Make slider width responsive (max 200, but smaller on small screens)
        int sliderWidth = Math.min(200, this.width - 40);

        // Calculate spacing based on screen height
        int spacing = Math.max(25, this.height / 12);

        // Start Y position - centered vertically
        int totalHeight = spacing * 6 + 20; // 7 rows of controls
        int startY = Math.max(30, (this.height - totalHeight) / 2);

        // Red slider
        this.redSlider = new ColorSlider(centerX - sliderWidth/2, startY, sliderWidth, 20,
                "Red", OutlineConfig.RED.get());
        this.addRenderableWidget(redSlider);

        // Green slider
        this.greenSlider = new ColorSlider(centerX - sliderWidth/2, startY + spacing, sliderWidth, 20,
                "Green", OutlineConfig.GREEN.get());
        this.addRenderableWidget(greenSlider);

        // Blue slider
        this.blueSlider = new ColorSlider(centerX - sliderWidth/2, startY + spacing * 2, sliderWidth, 20,
                "Blue", OutlineConfig.BLUE.get());
        this.addRenderableWidget(blueSlider);

        // Opacity slider
        this.opacitySlider = new OpacitySlider(centerX - sliderWidth/2, startY + spacing * 3, sliderWidth, 20,
                OutlineConfig.OPACITY.get());
        this.addRenderableWidget(opacitySlider);

        // Width slider
        this.widthSlider = new WidthSlider(centerX - sliderWidth/2, startY + spacing * 4, sliderWidth, 20,
                OutlineConfig.WIDTH.get());
        this.addRenderableWidget(widthSlider);

        // RGB Checkbox
        this.addRenderableWidget(Checkbox.builder(Component.literal("Enable RGB"), this.font)
                .pos(centerX - 40, startY + spacing * 5)
                .selected(rgbEnabled)
                .onValueChange((checkbox, selected) -> rgbEnabled = selected)
                .build());

        // RGB Speed slider
        this.rgbSpeedSlider = new RGBSpeedSlider(centerX - sliderWidth/2, startY + spacing * 6, sliderWidth, 20,
                OutlineConfig.RGB_SPEED.get());
        this.addRenderableWidget(rgbSpeedSlider);

        // Done button
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> saveAndClose())
                .bounds(centerX - 100, this.height - 40, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        graphics.drawCenteredString(this.font, this.title, centerX, 15, 0xFFFFFFFF);
    }

    private void saveAndClose() {
        OutlineConfig.RED.set(redSlider.getIntValue());
        OutlineConfig.GREEN.set(greenSlider.getIntValue());
        OutlineConfig.BLUE.set(blueSlider.getIntValue());
        OutlineConfig.OPACITY.set(opacitySlider.getValue());
        OutlineConfig.WIDTH.set(widthSlider.getValue());
        OutlineConfig.RGB_ENABLED.set(rgbEnabled);
        OutlineConfig.RGB_SPEED.set(rgbSpeedSlider.getValue());
        OutlineConfig.SPEC.save();

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
            // Value is already stored in this.value
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
            // Value is already stored in this.value
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
            // Value is already stored in this.value
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
            // Value is already stored in this.value
        }

        public double getValue() {
            return 0.1 + (this.value * 9.9);
        }
    }
}