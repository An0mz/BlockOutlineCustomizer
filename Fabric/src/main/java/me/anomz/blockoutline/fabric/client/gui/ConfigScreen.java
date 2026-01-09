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

    // Outline controls
    private ColorSlider outlineRedSlider;
    private ColorSlider outlineGreenSlider;
    private ColorSlider outlineBlueSlider;
    private OpacitySlider outlineOpacitySlider;
    private WidthSlider outlineWidthSlider;
    private RGBSpeedSlider outlineRgbSpeedSlider;
    private Checkbox outlineRgbCheckbox;

    // Fill controls
    private Checkbox fillEnabledCheckbox;
    private ColorSlider fillRedSlider;
    private ColorSlider fillGreenSlider;
    private ColorSlider fillBlueSlider;
    private OpacitySlider fillOpacitySlider;
    private RGBSpeedSlider fillRgbSpeedSlider;
    private Checkbox fillRgbCheckbox;

    public ConfigScreen(Screen lastScreen) {
        super(Component.literal("Block Outline Customizer"));
        this.lastScreen = lastScreen;
        this.config = Services.getConfigHelper();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int columnOffset = 120;
        int leftColumnCenter = centerX - columnOffset;
        int rightColumnCenter = centerX + columnOffset;
        int sliderWidth = 150;
        int spacing = 26;

        // --- OUTLINE SECTION (LEFT) ---
        int outlineY = 60;

        Component outlineRgbText = Component.literal("Rainbow Outline");
        int outlineRgbWidth = this.font.width(outlineRgbText) + 24;

        this.outlineRgbCheckbox = this.addRenderableWidget(
                new Checkbox(
                        leftColumnCenter - outlineRgbWidth / 2,
                        outlineY,
                        outlineRgbWidth,
                        20,
                        outlineRgbText,
                        config.isOutlineRgbEnabled(),
                        true
                )
        );

        outlineY += spacing;

        this.outlineRgbSpeedSlider = new RGBSpeedSlider(
                leftColumnCenter - sliderWidth / 2,
                outlineY,
                sliderWidth,
                20,
                config.getOutlineRgbSpeed()
        );
        this.addRenderableWidget(outlineRgbSpeedSlider);

        outlineY += spacing;

        this.outlineRedSlider = new ColorSlider(
                leftColumnCenter - sliderWidth / 2,
                outlineY,
                sliderWidth,
                20,
                "Red",
                config.getOutlineRed()
        );
        this.addRenderableWidget(outlineRedSlider);

        outlineY += spacing;

        this.outlineGreenSlider = new ColorSlider(
                leftColumnCenter - sliderWidth / 2,
                outlineY,
                sliderWidth,
                20,
                "Green",
                config.getOutlineGreen()
        );
        this.addRenderableWidget(outlineGreenSlider);

        outlineY += spacing;

        this.outlineBlueSlider = new ColorSlider(
                leftColumnCenter - sliderWidth / 2,
                outlineY,
                sliderWidth,
                20,
                "Blue",
                config.getOutlineBlue()
        );
        this.addRenderableWidget(outlineBlueSlider);

        outlineY += spacing;

        this.outlineOpacitySlider = new OpacitySlider(
                leftColumnCenter - sliderWidth / 2,
                outlineY,
                sliderWidth,
                20,
                config.getOutlineOpacity()
        );
        this.addRenderableWidget(outlineOpacitySlider);

        outlineY += spacing;

        this.outlineWidthSlider = new WidthSlider(
                leftColumnCenter - sliderWidth / 2,
                outlineY,
                sliderWidth,
                20,
                config.getOutlineWidth()
        );
        this.addRenderableWidget(outlineWidthSlider);

        // --- FILL SECTION (RIGHT) ---
        int fillY = 60;

        Component fillEnabledText = Component.literal("Enable Fill");
        int fillEnabledWidth = this.font.width(fillEnabledText) + 24;

        this.fillEnabledCheckbox = this.addRenderableWidget(
                new Checkbox(
                        rightColumnCenter - fillEnabledWidth / 2,
                        fillY,
                        fillEnabledWidth,
                        20,
                        fillEnabledText,
                        config.isFillEnabled(),
                        true
                )
        );

        fillY += spacing;

        Component fillRgbText = Component.literal("Rainbow Fill");
        int fillRgbWidth = this.font.width(fillRgbText) + 24;

        this.fillRgbCheckbox = this.addRenderableWidget(
                new Checkbox(
                        rightColumnCenter - fillRgbWidth / 2,
                        fillY,
                        fillRgbWidth,
                        20,
                        fillRgbText,
                        config.isFillRgbEnabled(),
                        true
                )
        );

        fillY += spacing;

        this.fillRgbSpeedSlider = new RGBSpeedSlider(
                rightColumnCenter - sliderWidth / 2,
                fillY,
                sliderWidth,
                20,
                config.getFillRgbSpeed()
        );
        this.addRenderableWidget(fillRgbSpeedSlider);

        fillY += spacing;

        this.fillRedSlider = new ColorSlider(
                rightColumnCenter - sliderWidth / 2,
                fillY,
                sliderWidth,
                20,
                "Red",
                config.getFillRed()
        );
        this.addRenderableWidget(fillRedSlider);

        fillY += spacing;

        this.fillGreenSlider = new ColorSlider(
                rightColumnCenter - sliderWidth / 2,
                fillY,
                sliderWidth,
                20,
                "Green",
                config.getFillGreen()
        );
        this.addRenderableWidget(fillGreenSlider);

        fillY += spacing;

        this.fillBlueSlider = new ColorSlider(
                rightColumnCenter - sliderWidth / 2,
                fillY,
                sliderWidth,
                20,
                "Blue",
                config.getFillBlue()
        );
        this.addRenderableWidget(fillBlueSlider);

        fillY += spacing;

        this.fillOpacitySlider = new OpacitySlider(
                rightColumnCenter - sliderWidth / 2,
                fillY,
                sliderWidth,
                20,
                config.getFillOpacity()
        );
        this.addRenderableWidget(fillOpacitySlider);

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> saveAndClose())
                .bounds(centerX - 100, this.height - 30, 200, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);  // Different in 1.20.1

        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int columnOffset = 120;
        int leftColumnCenter = centerX - columnOffset;
        int rightColumnCenter = centerX + columnOffset;

        graphics.drawCenteredString(this.font, this.title, centerX, 15, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, "Outline Settings", leftColumnCenter, 42, 0xFF00FFFF);
        graphics.drawCenteredString(this.font, "Fill Settings", rightColumnCenter, 42, 0xFF00FFFF);
    }

    private void saveAndClose() {
        // Save outline settings - read checkbox state when saving
        config.setOutlineRed(outlineRedSlider.getIntValue());
        config.setOutlineGreen(outlineGreenSlider.getIntValue());
        config.setOutlineBlue(outlineBlueSlider.getIntValue());
        config.setOutlineOpacity(outlineOpacitySlider.getValue());
        config.setOutlineWidth(outlineWidthSlider.getValue());
        config.setOutlineRgbEnabled(outlineRgbCheckbox.selected());
        config.setOutlineRgbSpeed(outlineRgbSpeedSlider.getValue());

        // Save fill settings
        config.setFillEnabled(fillEnabledCheckbox.selected());
        config.setFillRed(fillRedSlider.getIntValue());
        config.setFillGreen(fillGreenSlider.getIntValue());
        config.setFillBlue(fillBlueSlider.getIntValue());
        config.setFillOpacity(fillOpacitySlider.getValue());
        config.setFillRgbEnabled(fillRgbCheckbox.selected());
        config.setFillRgbSpeed(fillRgbSpeedSlider.getValue());

        config.save();

        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    // Slider classes remain the same...
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