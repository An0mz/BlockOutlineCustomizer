package me.anomz.blockoutline.client.gui;

import me.anomz.blockoutline.config.BOCConfig;
import me.anomz.blockoutline.config.OutlineStyle;
import me.anomz.blockoutline.config.StyleSettings;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class ConfigScreen extends Screen {
    private static final int SPACING = 24;
    private static final int WIDGET_HEIGHT = 20;

    private final Screen lastScreen;
    private final BOCConfig config;

    /** Working copy edited by the widgets; written back to the config on Done. */
    private StyleSettings working;
    private boolean customOutlineEnabled;
    private String selectedPreset;

    // Outline widgets
    private CycleButton<OutlineStyle> styleButton;
    private ValueSlider outlineRgbSpeedSlider;
    private ValueSlider outlineRedSlider;
    private ValueSlider outlineGreenSlider;
    private ValueSlider outlineBlueSlider;
    private ValueSlider outlineOpacitySlider;
    private ValueSlider outlineWidthSlider;
    private ValueSlider pulseSpeedSlider;

    // Fill widgets
    private ValueSlider fillRgbSpeedSlider;
    private ValueSlider fillRedSlider;
    private ValueSlider fillGreenSlider;
    private ValueSlider fillBlueSlider;
    private ValueSlider fillOpacitySlider;

    private CycleButton<String> presetButton;

    // Column centers, computed in init()
    private int outlineCenter;
    private int fillCenter;
    private int generalCenter;

    public ConfigScreen(Screen lastScreen) {
        super(Component.translatable("blockoutlinecustomizer.screen.title"));
        this.lastScreen = lastScreen;
        this.config = BOCConfig.get();
        this.working = config.style.copy();
        this.customOutlineEnabled = config.customOutlineEnabled;
        this.selectedPreset = BOCConfig.PRESET_NEON;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int colW = Math.min(140, (this.width - 40) / 3);
        int colGap = colW + 10;
        outlineCenter = centerX - colGap;
        fillCenter = centerX;
        generalCenter = centerX + colGap;

        int startY = 34;

        initOutlineColumn(outlineCenter - colW / 2, startY, colW);
        initFillColumn(fillCenter - colW / 2, startY, colW);
        initGeneralColumn(generalCenter - colW / 2, startY, colW);
    }

    private void initOutlineColumn(int x, int y, int colW) {
        this.styleButton = this.addRenderableWidget(
                CycleButton.builder(style -> Component.translatable(style.translationKey()), working.outlineStyle)
                        .withValues(OutlineStyle.values())
                        .create(x, y, colW, WIDGET_HEIGHT, Component.translatable("blockoutlinecustomizer.option.style")));
        y += SPACING;

        y = addToggleWithSpeed(x, y, colW,
                Component.translatable("blockoutlinecustomizer.option.rgb"),
                working.outlineRgbEnabled, selected -> working.outlineRgbEnabled = selected,
                working.outlineRgbSpeed, slider -> this.outlineRgbSpeedSlider = slider);

        this.outlineRedSlider = addSlider(x, y, colW, "blockoutlinecustomizer.option.red", 0, 255, working.outlineRed, true);
        y += SPACING;
        this.outlineGreenSlider = addSlider(x, y, colW, "blockoutlinecustomizer.option.green", 0, 255, working.outlineGreen, true);
        y += SPACING;
        this.outlineBlueSlider = addSlider(x, y, colW, "blockoutlinecustomizer.option.blue", 0, 255, working.outlineBlue, true);
        y += SPACING;
        this.outlineOpacitySlider = addSlider(x, y, colW, "blockoutlinecustomizer.option.opacity", 0.0, 1.0, working.outlineOpacity, false);
        y += SPACING;
        this.outlineWidthSlider = addSlider(x, y, colW, "blockoutlinecustomizer.option.width", 1.0, 10.0, working.outlineWidth, false);
        y += SPACING;

        addToggleWithSpeed(x, y, colW,
                Component.translatable("blockoutlinecustomizer.option.pulse"),
                working.pulseEnabled, selected -> working.pulseEnabled = selected,
                working.pulseSpeed, slider -> this.pulseSpeedSlider = slider);
    }

    private void initFillColumn(int x, int y, int colW) {
        Component fillText = Component.translatable("blockoutlinecustomizer.option.fill_enable");
        this.addRenderableWidget(Checkbox.builder(fillText, this.font)
                .pos(x, y)
                .selected(working.fillEnabled)
                .onValueChange((checkbox, selected) -> working.fillEnabled = selected)
                .build());
        y += SPACING;

        y = addToggleWithSpeed(x, y, colW,
                Component.translatable("blockoutlinecustomizer.option.rgb"),
                working.fillRgbEnabled, selected -> working.fillRgbEnabled = selected,
                working.fillRgbSpeed, slider -> this.fillRgbSpeedSlider = slider);

        this.fillRedSlider = addSlider(x, y, colW, "blockoutlinecustomizer.option.red", 0, 255, working.fillRed, true);
        y += SPACING;
        this.fillGreenSlider = addSlider(x, y, colW, "blockoutlinecustomizer.option.green", 0, 255, working.fillGreen, true);
        y += SPACING;
        this.fillBlueSlider = addSlider(x, y, colW, "blockoutlinecustomizer.option.blue", 0, 255, working.fillBlue, true);
        y += SPACING;
        this.fillOpacitySlider = addSlider(x, y, colW, "blockoutlinecustomizer.option.opacity", 0.0, 1.0, working.fillOpacity, false);
    }

    private void initGeneralColumn(int x, int y, int colW) {
        this.addRenderableWidget(Checkbox.builder(Component.translatable("blockoutlinecustomizer.option.enable_custom"), this.font)
                .pos(x, y)
                .selected(customOutlineEnabled)
                .onValueChange((checkbox, selected) -> customOutlineEnabled = selected)
                .build());
        y += SPACING;

        this.addRenderableWidget(Checkbox.builder(Component.translatable("blockoutlinecustomizer.option.sync_rgb"), this.font)
                .pos(x, y)
                .selected(working.syncRgb)
                .onValueChange((checkbox, selected) -> working.syncRgb = selected)
                .build());
        y += SPACING;

        if (!config.presets.containsKey(selectedPreset)) {
            selectedPreset = config.presets.keySet().stream().findFirst().orElse(BOCConfig.PRESET_NEON);
        }
        this.presetButton = this.addRenderableWidget(
                CycleButton.builder(ConfigScreen::presetName, selectedPreset)
                        .withValues(new ArrayList<>(config.presets.keySet()))
                        .create(x, y, colW, WIDGET_HEIGHT, Component.translatable("blockoutlinecustomizer.preset")));
        y += SPACING;

        this.addRenderableWidget(Button.builder(Component.translatable("blockoutlinecustomizer.preset.apply"), b -> applyPreset())
                .bounds(x, y, colW, WIDGET_HEIGHT)
                .build());
        y += SPACING;

        this.addRenderableWidget(Button.builder(Component.translatable("blockoutlinecustomizer.preset.save_custom"), b -> saveCustomPreset())
                .bounds(x, y, colW, WIDGET_HEIGHT)
                .build());
        y += SPACING;

        this.addRenderableWidget(Button.builder(Component.translatable("blockoutlinecustomizer.reset"), b -> resetToDefaults())
                .bounds(x, y, colW, WIDGET_HEIGHT)
                .build());
        y += SPACING;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> saveAndClose())
                .bounds(x, y, colW, WIDGET_HEIGHT)
                .build());
    }

    /** Adds a checkbox and a speed slider sharing one row; returns the next row's y. */
    private int addToggleWithSpeed(int x, int y, int colW, Component label,
                                   boolean selected, java.util.function.Consumer<Boolean> onToggle,
                                   double speed, java.util.function.Consumer<ValueSlider> sliderOut) {
        int checkboxWidth = this.font.width(label) + 28;
        this.addRenderableWidget(Checkbox.builder(label, this.font)
                .pos(x, y)
                .selected(selected)
                .onValueChange((checkbox, value) -> onToggle.accept(value))
                .build());

        int sliderX = x + checkboxWidth;
        int sliderWidth = Math.max(40, colW - checkboxWidth);
        ValueSlider slider = new ValueSlider(sliderX, y, sliderWidth, WIDGET_HEIGHT,
                "blockoutlinecustomizer.option.speed", 0.1, 10.0, speed, false);
        sliderOut.accept(this.addRenderableWidget(slider));
        return y + SPACING;
    }

    private ValueSlider addSlider(int x, int y, int colW, String key, double min, double max, double initial, boolean integer) {
        return this.addRenderableWidget(new ValueSlider(x, y, colW, WIDGET_HEIGHT, key, min, max, initial, integer));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable("blockoutlinecustomizer.section.outline"), outlineCenter, 22, 0xFF00FFFF);
        graphics.centeredText(this.font, Component.translatable("blockoutlinecustomizer.section.fill"), fillCenter, 22, 0xFF00FFFF);
        graphics.centeredText(this.font, Component.translatable("blockoutlinecustomizer.section.general"), generalCenter, 22, 0xFF00FFFF);
    }

    private static Component presetName(String id) {
        if (BOCConfig.BUILTIN_PRESETS.contains(id) || BOCConfig.PRESET_CUSTOM.equals(id)) {
            return Component.translatable("blockoutlinecustomizer.preset." + id);
        }
        return Component.literal(id);
    }

    /** Writes the current slider/cycle values into {@link #working}. */
    private void flushWidgets() {
        working.outlineStyle = styleButton.getValue();
        working.outlineRgbSpeed = outlineRgbSpeedSlider.get();
        working.outlineRed = outlineRedSlider.getInt();
        working.outlineGreen = outlineGreenSlider.getInt();
        working.outlineBlue = outlineBlueSlider.getInt();
        working.outlineOpacity = outlineOpacitySlider.get();
        working.outlineWidth = outlineWidthSlider.get();
        working.pulseSpeed = pulseSpeedSlider.get();
        working.fillRgbSpeed = fillRgbSpeedSlider.get();
        working.fillRed = fillRedSlider.getInt();
        working.fillGreen = fillGreenSlider.getInt();
        working.fillBlue = fillBlueSlider.getInt();
        working.fillOpacity = fillOpacitySlider.get();
    }

    private void applyPreset() {
        selectedPreset = presetButton.getValue();
        StyleSettings preset = config.presets.get(selectedPreset);
        if (preset != null) {
            working = preset.copy();
            this.rebuildWidgets();
        }
    }

    private void saveCustomPreset() {
        flushWidgets();
        config.presets.put(BOCConfig.PRESET_CUSTOM, working.copy());
        config.save();
        selectedPreset = BOCConfig.PRESET_CUSTOM;
        this.rebuildWidgets();
    }

    private void resetToDefaults() {
        working = new StyleSettings();
        customOutlineEnabled = true;
        this.rebuildWidgets();
    }

    private void saveAndClose() {
        flushWidgets();
        config.customOutlineEnabled = customOutlineEnabled;
        config.style = working.copy();
        config.save();
        this.minecraft.gui.setScreen(lastScreen);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(lastScreen);
    }

    private static class ValueSlider extends AbstractSliderButton {
        private final String key;
        private final double min;
        private final double max;
        private final boolean integer;

        ValueSlider(int x, int y, int width, int height, String key, double min, double max, double initial, boolean integer) {
            super(x, y, width, height, Component.empty(), (clamp(initial, min, max) - min) / (max - min));
            this.key = key;
            this.min = min;
            this.max = max;
            this.integer = integer;
            this.updateMessage();
        }

        private static double clamp(double v, double min, double max) {
            return Math.max(min, Math.min(max, v));
        }

        @Override
        protected void updateMessage() {
            String formatted = integer ? String.valueOf(getInt()) : String.format("%.2f", get());
            this.setMessage(Component.translatable(key, formatted));
        }

        @Override
        protected void applyValue() {
        }

        double get() {
            return min + this.value * (max - min);
        }

        int getInt() {
            return (int) Math.round(get());
        }
    }
}
