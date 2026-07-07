package me.anomz.blockoutline.client.gui;

import me.anomz.blockoutline.client.PreviewState;
import me.anomz.blockoutline.client.render.OutlineRenderCore;
import me.anomz.blockoutline.config.BOCConfig;
import me.anomz.blockoutline.config.OutlineStyle;
import me.anomz.blockoutline.config.StyleSettings;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Card-styled, tabbed config screen with a live preview: in-world the real
 * outline updates as you drag; in menus a stylized grass block is drawn.
 */
public class ConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 214;
    private static final int PANEL_BG = 0xEA0D1015;
    private static final int PANEL_BORDER = 0xFF262B36;
    private static final int ACCENT = 0xFF00C8C8;
    private static final int WIDGET_BG = 0xFF14161C;
    private static final int WIDGET_BORDER = 0xFF3A3F4D;
    private static final int WIDGET_BORDER_HOVER = 0xFF9BE8E8;
    private static final int MAX_PALETTE = 10;

    private enum Tab {OUTLINE, FILL, EFFECTS, GENERAL}

    private final Screen lastScreen;
    private final BOCConfig config;

    private StyleSettings working;
    private boolean customOutlineEnabled;
    private Tab tab = Tab.OUTLINE;
    private String selectedPreset = BOCConfig.PRESET_NEON;

    private boolean inWorld;
    private int panelX, panelY, panelH;
    private int rowStep, widgetH;

    private GradientSlider redSlider, greenSlider, blueSlider;
    private EditBox hexBox;
    private boolean updatingHex;
    private int swatchX, swatchY;
    private String paletteHexText = "#FF0000";
    private int paletteRowX, paletteRowY, paletteRowW;

    public ConfigScreen(Screen lastScreen) {
        super(Component.translatable("blockoutlinecustomizer.screen.title"));
        this.lastScreen = lastScreen;
        this.config = BOCConfig.get();
        this.working = config.style.copy();
        this.customOutlineEnabled = config.customOutlineEnabled;
    }

    @Override
    protected void init() {
        super.init();

        this.inWorld = this.minecraft != null && this.minecraft.level != null;
        this.panelH = Math.min(this.height - 8, 300);
        this.panelY = (this.height - panelH) / 2;
        int rightDocked = this.width - PANEL_WIDTH - 6;
        this.panelX = inWorld ? rightDocked : Math.min(rightDocked, this.width / 2 + 6);

        this.rowStep = Math.max(20, Math.min(26, (panelH - 66) / 8));
        this.widgetH = Math.min(20, rowStep - 3);

        this.redSlider = null;
        this.greenSlider = null;
        this.blueSlider = null;
        this.hexBox = null;

        addTabBar();

        int x = panelX + 10;
        int w = PANEL_WIDTH - 20;
        int y = panelY + 34;

        switch (tab) {
            case OUTLINE -> initOutlineTab(x, y, w);
            case FILL -> initFillTab(x, y, w);
            case EFFECTS -> initEffectsTab(x, y, w);
            case GENERAL -> initGeneralTab(x, y, w);
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> saveAndClose())
                .bounds(x, panelY + panelH - widgetH - 8, w, widgetH)
                .build());

        PreviewState.set(working, customOutlineEnabled);
    }

    private void addTabBar() {
        int tabW = (PANEL_WIDTH - 20 - 6) / 4;
        int x = panelX + 10;
        int y = panelY + 8;
        addTabButton(x, y, tabW, Tab.OUTLINE, "blockoutlinecustomizer.section.outline");
        addTabButton(x + (tabW + 2), y, tabW, Tab.FILL, "blockoutlinecustomizer.section.fill");
        addTabButton(x + (tabW + 2) * 2, y, tabW, Tab.EFFECTS, "blockoutlinecustomizer.section.effects");
        addTabButton(x + (tabW + 2) * 3, y, tabW, Tab.GENERAL, "blockoutlinecustomizer.section.general");
    }

    private void addTabButton(int x, int y, int w, Tab target, String key) {
        Button button = Button.builder(Component.translatable(key), b -> {
            this.tab = target;
            this.rebuildWidgets();
        }).bounds(x, y, w, 18).build();
        button.active = this.tab != target;
        this.addRenderableWidget(button);
    }

    private void initOutlineTab(int x, int y, int w) {
        CycleButton<OutlineStyle> styleButton = CycleButton
                .builder((OutlineStyle s) -> Component.translatable(s.translationKey()))
                .withValues(OutlineStyle.values())
                .withInitialValue(working.outlineStyle)
                .create(x, y, w, widgetH, Component.translatable("blockoutlinecustomizer.option.style"),
                        (btn, value) -> working.outlineStyle = value);
        this.addRenderableWidget(styleButton);
        y += rowStep;

        y = addColorRows(x, y, w,
                () -> working.outlineRed, v -> working.outlineRed = v,
                () -> working.outlineGreen, v -> working.outlineGreen = v,
                () -> working.outlineBlue, v -> working.outlineBlue = v);

        this.addRenderableWidget(new StyledSlider(this.font, x, y, w, widgetH,
                "blockoutlinecustomizer.option.opacity", 0.0, 1.0, false,
                () -> working.outlineOpacity, v -> working.outlineOpacity = v));
        y += rowStep;

        this.addRenderableWidget(new StyledSlider(this.font, x, y, w, widgetH,
                "blockoutlinecustomizer.option.width", 1.0, 10.0, false,
                () -> working.outlineWidth, v -> working.outlineWidth = v));
        y += rowStep;

        Checkbox shaderFix = new CallbackCheckbox(x, y, Component.translatable("blockoutlinecustomizer.option.quad_width"), this.font,
                working.outlineQuadWidth, v -> working.outlineQuadWidth = v);
        shaderFix.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.translatable("blockoutlinecustomizer.option.quad_width.tooltip")));
        this.addRenderableWidget(shaderFix);
    }

    private void initFillTab(int x, int y, int w) {
        this.addRenderableWidget(new CallbackCheckbox(x, y, Component.translatable("blockoutlinecustomizer.option.fill_enable"), this.font,
                working.fillEnabled, v -> working.fillEnabled = v));
        y += rowStep;

        y = addColorRows(x, y, w,
                () -> working.fillRed, v -> working.fillRed = v,
                () -> working.fillGreen, v -> working.fillGreen = v,
                () -> working.fillBlue, v -> working.fillBlue = v);

        this.addRenderableWidget(new StyledSlider(this.font, x, y, w, widgetH,
                "blockoutlinecustomizer.option.opacity", 0.0, 1.0, false,
                () -> working.fillOpacity, v -> working.fillOpacity = v));
    }

    private void initEffectsTab(int x, int y, int w) {
        y = addToggleWithSpeed(x, y, w,
                Component.translatable("blockoutlinecustomizer.option.rgb"),
                working.outlineRgbEnabled, v -> working.outlineRgbEnabled = v,
                0.1, 10.0,
                () -> working.outlineRgbSpeed, v -> working.outlineRgbSpeed = v);

        this.addRenderableWidget(new CallbackCheckbox(x, y, Component.translatable("blockoutlinecustomizer.option.gradient"), this.font,
                working.outlineRgbGradient, v -> working.outlineRgbGradient = v));
        y += rowStep;

        y = addToggleWithSpeed(x, y, w,
                Component.translatable("blockoutlinecustomizer.option.fill_rgb"),
                working.fillRgbEnabled, v -> working.fillRgbEnabled = v,
                0.1, 10.0,
                () -> working.fillRgbSpeed, v -> working.fillRgbSpeed = v);

        this.addRenderableWidget(new CallbackCheckbox(x, y, Component.translatable("blockoutlinecustomizer.option.sync_rgb"), this.font,
                working.syncRgb, v -> working.syncRgb = v));
        y += rowStep;

        y = addToggleWithSpeed(x, y, w,
                Component.translatable("blockoutlinecustomizer.option.pulse"),
                working.pulseEnabled, v -> working.pulseEnabled = v,
                0.01, 2.0,
                () -> working.pulseSpeed, v -> working.pulseSpeed = v);

        y = addToggleWithSpeed(x, y, w,
                Component.translatable("blockoutlinecustomizer.option.moving"),
                working.movingEnabled, v -> working.movingEnabled = v,
                0.01, 2.0,
                () -> working.movingSpeed, v -> working.movingSpeed = v);

        // Palette editor: hex input + add, then removable swatches
        int addW = 50;
        EditBox paletteBox = new HexBox(this.font, x, y, w - addW - 4, widgetH,
                Component.translatable("blockoutlinecustomizer.palette"), null);
        paletteBox.setValue(paletteHexText);
        paletteBox.setResponder(text -> paletteHexText = text);
        this.addRenderableWidget(paletteBox);

        this.addRenderableWidget(Button.builder(Component.translatable("blockoutlinecustomizer.palette.add"), b -> {
            Integer rgb = StyleSettings.parseHex(paletteHexText);
            if (rgb != null && working.rgbColors.size() < MAX_PALETTE) {
                working.rgbColors.add(String.format(Locale.ROOT, "#%06X", rgb));
                this.rebuildWidgets();
            }
        }).bounds(x + w - addW, y, addW, widgetH).build());
        y += rowStep;

        this.paletteRowX = x;
        this.paletteRowY = y;
        this.paletteRowW = w;
        int swatchSize = Math.min(widgetH, 18);
        for (int i = 0; i < working.rgbColors.size(); i++) {
            Integer rgb = StyleSettings.parseHex(working.rgbColors.get(i));
            if (rgb == null) {
                continue;
            }
            final int index = i;
            this.addRenderableWidget(new SwatchButton(x + i * (swatchSize + 2), y, swatchSize,
                    0xFF000000 | rgb, () -> {
                working.rgbColors.remove(index);
                this.rebuildWidgets();
            }));
        }
    }

    private void initGeneralTab(int x, int y, int w) {
        this.addRenderableWidget(new CallbackCheckbox(x, y, Component.translatable("blockoutlinecustomizer.option.enable_custom"), this.font,
                customOutlineEnabled, v -> customOutlineEnabled = v));
        y += rowStep;

        if (!config.presets.containsKey(selectedPreset)) {
            selectedPreset = config.presets.keySet().stream().findFirst().orElse(BOCConfig.PRESET_NEON);
        }
        CycleButton<String> presetButton = CycleButton
                .builder(ConfigScreen::presetName)
                .withValues(new ArrayList<>(config.presets.keySet()))
                .withInitialValue(selectedPreset)
                .create(x, y, w, widgetH, Component.translatable("blockoutlinecustomizer.preset"),
                        (btn, value) -> selectedPreset = value);
        this.addRenderableWidget(presetButton);
        y += rowStep;

        this.addRenderableWidget(Button.builder(Component.translatable("blockoutlinecustomizer.preset.apply"), b -> applyPreset())
                .bounds(x, y, w, widgetH).build());
        y += rowStep;

        this.addRenderableWidget(Button.builder(Component.translatable("blockoutlinecustomizer.preset.save_custom"), b -> saveCustomPreset())
                .bounds(x, y, w, widgetH).build());
        y += rowStep;

        this.addRenderableWidget(Button.builder(Component.translatable("blockoutlinecustomizer.reset"), b -> resetToDefaults())
                .bounds(x, y, w, widgetH).build());
    }

    private int addToggleWithSpeed(int x, int y, int w, Component label,
                                   boolean selected, java.util.function.Consumer<Boolean> onToggle,
                                   double speedMin, double speedMax,
                                   DoubleSupplier speedGet, DoubleConsumer speedSet) {
        int checkboxWidth = this.font.width(label) + 28;
        this.addRenderableWidget(new CallbackCheckbox(x, y, label, this.font, selected, onToggle::accept));

        int sliderX = x + checkboxWidth;
        int sliderW = Math.max(40, w - checkboxWidth);
        this.addRenderableWidget(new StyledSlider(this.font, sliderX, y, sliderW, widgetH,
                "blockoutlinecustomizer.option.speed", speedMin, speedMax, false, speedGet, speedSet));
        return y + rowStep;
    }

    /** Adds R/G/B gradient sliders plus the hex row; returns the next y. */
    private int addColorRows(int x, int y, int w,
                             IntSupplier rGet, IntConsumer rSet,
                             IntSupplier gGet, IntConsumer gSet,
                             IntSupplier bGet, IntConsumer bSet) {
        IntSupplier rgb = () -> (rGet.getAsInt() << 16) | (gGet.getAsInt() << 8) | bGet.getAsInt();

        this.redSlider = this.addRenderableWidget(new GradientSlider(this.font, x, y, w, widgetH,
                "blockoutlinecustomizer.option.red", 0, rgb, rGet, v -> {
            rSet.accept(v);
            syncHexFromSliders(rgb);
        }));
        y += rowStep;
        this.greenSlider = this.addRenderableWidget(new GradientSlider(this.font, x, y, w, widgetH,
                "blockoutlinecustomizer.option.green", 1, rgb, gGet, v -> {
            gSet.accept(v);
            syncHexFromSliders(rgb);
        }));
        y += rowStep;
        this.blueSlider = this.addRenderableWidget(new GradientSlider(this.font, x, y, w, widgetH,
                "blockoutlinecustomizer.option.blue", 2, rgb, bGet, v -> {
            bSet.accept(v);
            syncHexFromSliders(rgb);
        }));
        y += rowStep;

        int swatchSize = widgetH;
        this.swatchX = x + w - swatchSize;
        this.swatchY = y;
        this.hexBox = new HexBox(this.font, x, y, w - swatchSize - 6, widgetH,
                Component.translatable("blockoutlinecustomizer.hex"), () -> {
            updatingHex = true;
            hexBox.setValue(formatHex(rgb.getAsInt()));
            updatingHex = false;
        });
        this.hexBox.setValue(formatHex(rgb.getAsInt()));
        this.hexBox.setResponder(text -> {
            if (updatingHex) {
                return;
            }
            Integer value = StyleSettings.parseHex(text);
            if (value != null) {
                rSet.accept((value >> 16) & 0xFF);
                gSet.accept((value >> 8) & 0xFF);
                bSet.accept(value & 0xFF);
                if (redSlider != null) redSlider.setDisplay(rGet.getAsInt());
                if (greenSlider != null) greenSlider.setDisplay(gGet.getAsInt());
                if (blueSlider != null) blueSlider.setDisplay(bGet.getAsInt());
            }
        });
        this.addRenderableWidget(hexBox);
        return y + rowStep;
    }

    private void syncHexFromSliders(IntSupplier rgb) {
        if (hexBox != null && !hexBox.isFocused()) {
            updatingHex = true;
            hexBox.setValue(formatHex(rgb.getAsInt()));
            updatingHex = false;
        }
    }

    private static String formatHex(int rgb) {
        return String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
    }

    private static Component presetName(String id) {
        if (BOCConfig.BUILTIN_PRESETS.contains(id) || BOCConfig.PRESET_CUSTOM.equals(id)) {
            return Component.translatable("blockoutlinecustomizer.preset." + id);
        }
        return Component.literal(id);
    }

    @Override
    public void tick() {
        super.tick();
        PreviewState.set(working, customOutlineEnabled);
    }

    @Override
    public void removed() {
        super.removed();
        PreviewState.clear();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        if (!inWorld) {
            super.renderBackground(graphics);
        }
        // Settings panel card
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelH, PANEL_BG);
        graphics.renderOutline(panelX, panelY, PANEL_WIDTH, panelH, PANEL_BORDER);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 2, ACCENT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (hexBox != null) {
            int rgb = tab == Tab.FILL
                    ? (working.fillRed << 16) | (working.fillGreen << 8) | working.fillBlue
                    : (working.outlineRed << 16) | (working.outlineGreen << 8) | working.outlineBlue;
            graphics.fill(swatchX, swatchY, swatchX + widgetH, swatchY + widgetH, 0xFF000000 | rgb);
            graphics.renderOutline(swatchX, swatchY, widgetH, widgetH, WIDGET_BORDER);
        }

        if (tab == Tab.EFFECTS && working.rgbColors.isEmpty()) {
            // Full-rainbow hint strip where the palette swatches would be
            int strips = 24;
            int h = Math.min(widgetH, 18);
            for (int i = 0; i < strips; i++) {
                int sx1 = paletteRowX + paletteRowW * i / strips;
                int sx2 = paletteRowX + paletteRowW * (i + 1) / strips;
                graphics.fill(sx1, paletteRowY, sx2, paletteRowY + h,
                        Color.HSBtoRGB(i / (float) strips, 1.0f, 1.0f));
            }
            graphics.renderOutline(paletteRowX, paletteRowY, paletteRowW, h, WIDGET_BORDER);
        }

        if (!inWorld) {
            drawPreviewCube(graphics);
        }
    }

    // --- Preview cube (menus only; in-world the real outline is the preview) ---

    private void drawPreviewCube(GuiGraphics graphics) {
        int cx = panelX / 2;
        int cy = this.height / 2;
        int a = Math.min(40, Math.max(24, panelX / 5));

        // Card behind the cube
        int card = a * 3 + 20;
        graphics.fill(cx - card / 2, cy - card / 2, cx + card / 2, cy + card / 2, PANEL_BG);
        graphics.renderOutline(cx - card / 2, cy - card / 2, card, card, PANEL_BORDER);
        graphics.drawCenteredString(this.font, this.title, cx, cy - card / 2 - 14, 0xFFFFFFFF);

        float topY = cy - (a + a) / 2.0f;
        // Cube corners (2:1 dimetric projection)
        float tX = cx, tY = topY;                       // top back corner
        float rX = cx + a, rY = topY + a / 2.0f;        // top right
        float fX = cx, fY = topY + a;                   // top front
        float lX = cx - a, lY = topY + a / 2.0f;        // top left
        float h = a;                                    // vertical edge length

        // Faces: top (grass), left + right (dirt) with a grass strip
        fillQuad(graphics, tX, tY, a, a / 2.0f, -a, a / 2.0f, 0xFF6DA344);
        fillQuad(graphics, lX, lY, a, a / 2.0f, 0, h, 0xFF6F4E31);
        fillQuad(graphics, fX, fY, a, -a / 2.0f, 0, h, 0xFF5B3F26);
        fillQuad(graphics, lX, lY, a, a / 2.0f, 0, h * 0.2f, 0xFF578438);
        fillQuad(graphics, fX, fY, a, -a / 2.0f, 0, h * 0.2f, 0xFF48702E);

        double pulse = OutlineRenderCore.pulseFactor(working);

        // Fill overlay
        if (working.fillEnabled) {
            int fill = OutlineRenderCore.fillColor(working, pulse);
            fillQuad(graphics, tX, tY, a, a / 2.0f, -a, a / 2.0f, fill);
            fillQuad(graphics, lX, lY, a, a / 2.0f, 0, h, fill);
            fillQuad(graphics, fX, fY, a, -a / 2.0f, 0, h, fill);
        }

        int alpha = (int) Math.round(working.outlineOpacity * pulse * 255.0);
        if (alpha <= 0 || !customOutlineEnabled) {
            return;
        }

        int alphaBits = alpha << 24;
        int uniformColor = alphaBits | (OutlineRenderCore.outlineColor(working, pulse) & 0xFFFFFF);
        boolean gradient = working.outlineRgbEnabled && working.outlineRgbGradient;
        double baseT = me.anomz.blockoutline.util.Animations.rainbowHue(working.outlineRgbSpeed);
        float phase = OutlineRenderCore.dashPhase(working);
        float px = (float) working.outlineWidth;

        // The 9 visible cube edges with their block-local 3D coordinates
        // (for gradient sampling): T=(0,1,0) R=(1,1,0) F=(1,1,1) L=(0,1,1)
        styledEdge(graphics, tX, tY, rX, rY, 0, 1, 0, 1, 1, 0, gradient, baseT, phase, alphaBits, uniformColor, px);
        styledEdge(graphics, rX, rY, fX, fY, 1, 1, 0, 1, 1, 1, gradient, baseT, phase, alphaBits, uniformColor, px);
        styledEdge(graphics, fX, fY, lX, lY, 1, 1, 1, 0, 1, 1, gradient, baseT, phase, alphaBits, uniformColor, px);
        styledEdge(graphics, lX, lY, tX, tY, 0, 1, 1, 0, 1, 0, gradient, baseT, phase, alphaBits, uniformColor, px);
        styledEdge(graphics, rX, rY, rX, rY + h, 1, 1, 0, 1, 0, 0, gradient, baseT, phase, alphaBits, uniformColor, px);
        styledEdge(graphics, fX, fY, fX, fY + h, 1, 1, 1, 1, 0, 1, gradient, baseT, phase, alphaBits, uniformColor, px);
        styledEdge(graphics, lX, lY, lX, lY + h, 0, 1, 1, 0, 0, 1, gradient, baseT, phase, alphaBits, uniformColor, px);
        styledEdge(graphics, lX, lY + h, fX, fY + h, 0, 0, 1, 1, 0, 1, gradient, baseT, phase, alphaBits, uniformColor, px);
        styledEdge(graphics, fX, fY + h, rX, rY + h, 1, 0, 1, 1, 0, 0, gradient, baseT, phase, alphaBits, uniformColor, px);
    }

    /** Draws a parallelogram: origin + u*[0..1] + v*[0..1]. */
    private static void fillQuad(GuiGraphics graphics, float ox, float oy, float ux, float uy, float vx, float vy, int color) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.mulPoseMatrix(new Matrix4f(ux, uy, 0, 0, vx, vy, 0, 0, 0, 0, 1, 0, ox, oy, 0, 1));
        graphics.fill(0, 0, 1, 1, color);
        pose.popPose();
    }

    /** Draws one cube edge split into styled segments (each edge is one block-length). */
    private void styledEdge(GuiGraphics graphics, float x1, float y1, float x2, float y2,
                            float ax, float ay, float az, float bx, float by, float bz,
                            boolean gradient, double baseT, float phase, int alphaBits, int uniformColor, float px) {
        OutlineRenderCore.segments(working.outlineStyle, 1.0f, phase, (from, to) -> {
            if (gradient) {
                int steps = Math.max(1, (int) Math.ceil((to - from) / 0.2f));
                for (int i = 0; i < steps; i++) {
                    float f0 = from + (to - from) * i / steps;
                    float f1 = from + (to - from) * (i + 1) / steps;
                    float mid = (f0 + f1) / 2.0f;
                    int color = alphaBits | OutlineRenderCore.gradientRgb(working, baseT,
                            ax + (bx - ax) * mid, ay + (by - ay) * mid, az + (bz - az) * mid);
                    lineSegment(graphics,
                            x1 + (x2 - x1) * f0, y1 + (y2 - y1) * f0,
                            x1 + (x2 - x1) * f1, y1 + (y2 - y1) * f1, px, color);
                }
            } else {
                lineSegment(graphics,
                        x1 + (x2 - x1) * from, y1 + (y2 - y1) * from,
                        x1 + (x2 - x1) * to, y1 + (y2 - y1) * to, px, uniformColor);
            }
        });
    }

    private static void lineSegment(GuiGraphics graphics, float x1, float y1, float x2, float y2, float px, int color) {
        float dx = x2 - x1, dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.01f) {
            return;
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x1, y1, 0);
        pose.mulPose(Axis.ZP.rotation((float) Math.atan2(dy, dx)));
        int half = (int) Math.floor(px / 2.0f);
        int other = (int) Math.ceil(px / 2.0f);
        graphics.fill(0, -Math.max(1, half), Math.round(len), Math.max(1, other), color);
        pose.popPose();
    }

    // --- Actions ---

    private void applyPreset() {
        StyleSettings preset = config.presets.get(selectedPreset);
        if (preset != null) {
            working = preset.copy();
            this.rebuildWidgets();
        }
    }

    private void saveCustomPreset() {
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
        config.customOutlineEnabled = customOutlineEnabled;
        config.style = working.copy();
        config.save();
        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    // --- Custom-styled widgets ---

    /** 1.20.1 Checkbox has no builder or change callback; add one. */
    private static class CallbackCheckbox extends Checkbox {
        private final java.util.function.Consumer<Boolean> onChange;

        CallbackCheckbox(int x, int y, Component label, Font font, boolean selected,
                         java.util.function.Consumer<Boolean> onChange) {
            super(x, y, font.width(label) + 24, 20, label, selected);
            this.onChange = onChange;
        }

        @Override
        public void onPress() {
            super.onPress();
            onChange.accept(this.selected());
        }
    }

    /**
     * Hex color field. Selects its whole text when focused so typing replaces
     * the old value (the field is otherwise always at max length).
     */
    private static class HexBox extends EditBox {
        private final Runnable onFocusLost;

        HexBox(Font font, int x, int y, int w, int h, Component message, Runnable onFocusLost) {
            super(font, x, y, w, h, message);
            this.onFocusLost = onFocusLost;
            this.setMaxLength(7);
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (focused) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
            } else if (onFocusLost != null) {
                onFocusLost.run();
            }
        }
    }

    /** A clickable palette color square; clicking removes it. */
    private static class SwatchButton extends AbstractButton {
        private final int color;
        private final Runnable onRemove;

        SwatchButton(int x, int y, int size, int color, Runnable onRemove) {
            super(x, y, size, size, Component.literal(String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF)));
            this.color = color;
            this.onRemove = onRemove;
        }

        @Override
        public void onPress() {
            onRemove.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
            graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), isHovered() ? WIDGET_BORDER_HOVER : WIDGET_BORDER);
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static class StyledSlider extends AbstractSliderButton {
        private final Font font;
        private final String key;
        private final double min;
        private final double max;
        private final boolean integer;
        private final DoubleConsumer setter;

        StyledSlider(Font font, int x, int y, int w, int h, String key,
                     double min, double max, boolean integer,
                     DoubleSupplier getter, DoubleConsumer setter) {
            super(x, y, w, h, Component.empty(),
                    (Math.max(min, Math.min(max, getter.getAsDouble())) - min) / (max - min));
            this.font = font;
            this.key = key;
            this.min = min;
            this.max = max;
            this.integer = integer;
            this.setter = setter;
            this.updateMessage();
        }

        double get() {
            return min + this.value * (max - min);
        }

        @Override
        protected void updateMessage() {
            String formatted = integer
                    ? String.valueOf((int) Math.round(get()))
                    : String.format(Locale.ROOT, "%.2f", get());
            this.setMessage(Component.translatable(key, formatted));
        }

        @Override
        protected void applyValue() {
            setter.accept(get());
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            graphics.fill(x, y, x + w, y + h, WIDGET_BG);
            int progress = (int) ((w - 2) * this.value);
            graphics.fill(x + 1, y + 1, x + 1 + progress, y + h - 1, 0xFF0E4A4D);
            int handleX = x + 1 + (int) ((w - 5) * this.value);
            graphics.fill(handleX, y + 1, handleX + 3, y + h - 1, 0xFFDDE7E7);
            graphics.renderOutline(x, y, w, h, isHovered() ? WIDGET_BORDER_HOVER : WIDGET_BORDER);
            graphics.drawCenteredString(font, getMessage(), x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
        }
    }

    private static class GradientSlider extends AbstractSliderButton {
        private final Font font;
        private final String key;
        private final int channel;
        private final IntSupplier rgbSupplier;
        private final IntConsumer setter;

        GradientSlider(Font font, int x, int y, int w, int h, String key, int channel,
                       IntSupplier rgbSupplier, IntSupplier getter, IntConsumer setter) {
            super(x, y, w, h, Component.empty(), getter.getAsInt() / 255.0);
            this.font = font;
            this.key = key;
            this.channel = channel;
            this.rgbSupplier = rgbSupplier;
            this.setter = setter;
            this.updateMessage();
        }

        int getInt() {
            return (int) Math.round(this.value * 255.0);
        }

        void setDisplay(int channelValue) {
            this.value = channelValue / 255.0;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable(key, String.valueOf(getInt())));
        }

        @Override
        protected void applyValue() {
            setter.accept(getInt());
            this.updateMessage();
        }

        private int withChannel(int rgb, int channelValue) {
            int shift = (2 - channel) * 8;
            return (rgb & ~(0xFF << shift)) | (channelValue << shift);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            graphics.fill(x, y, x + w, y + h, WIDGET_BG);

            int rgb = rgbSupplier.getAsInt();
            int strips = 24;
            int trackX = x + 1, trackW = w - 2;
            for (int i = 0; i < strips; i++) {
                int channelValue = i * 255 / (strips - 1);
                int sx1 = trackX + trackW * i / strips;
                int sx2 = trackX + trackW * (i + 1) / strips;
                graphics.fill(sx1, y + 1, sx2, y + h - 1, 0xFF000000 | withChannel(rgb, channelValue));
            }

            int handleX = x + 1 + (int) ((w - 5) * this.value);
            graphics.fill(handleX, y, handleX + 3, y + h, 0xFFFFFFFF);
            graphics.renderOutline(x, y, w, h, isHovered() ? WIDGET_BORDER_HOVER : WIDGET_BORDER);
            graphics.drawCenteredString(font, getMessage(), x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
        }
    }
}
