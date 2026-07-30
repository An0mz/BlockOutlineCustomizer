package me.anomz.blockoutline.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Floating name-tag style info above the targeted block: name, position and
 * a few gameplay-relevant state properties (power, facing, ...).
 */
public final class BlockInfoOverlay {
    /** Packed fullbright lightmap coords (block 15 / sky 15). */
    private static final int FULL_BRIGHT = 0xF000F0;
    /** 25% black, like the vanilla name-tag background. */
    private static final int BACKGROUND = 0x40000000;
    /** State properties worth showing, in display order. */
    private static final List<String> NOTABLE_PROPERTIES = List.of(
            "power", "note", "instrument", "facing", "axis", "open", "lit", "level", "age");
    private static final int MAX_PROPERTIES = 3;

    private BlockInfoOverlay() {
    }

    /**
     * Draws the info lines as camera-facing name tags. The pose stack must
     * already be translated to the block's min corner (as in
     * OutlineRenderCore.submit).
     */
    public static void render(PoseStack poseStack, MultiBufferSource bufferSource,
                              BlockPos pos, BlockState state) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        Quaternionf cameraRotation = mc.gameRenderer.getMainCamera().rotation();

        List<Component> lines = new ArrayList<>(3);
        lines.add(state.getBlock().getName());
        lines.add(Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
        String properties = notableProperties(state);
        if (!properties.isEmpty()) {
            lines.add(Component.literal(properties));
        }

        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            poseStack.pushPose();
            // Highest line first; each name tag line is ~0.25 world units tall
            poseStack.translate(0.5, 1.35 + (lines.size() - 1 - i) * 0.25, 0.5);
            poseStack.mulPose(cameraRotation);
            poseStack.scale(0.025f, -0.025f, 0.025f);
            Matrix4f matrix = poseStack.last().pose();
            float x = -font.width(line) / 2.0f;
            font.drawInBatch(line, x, 0, 0xFFFFFFFF, false, matrix, bufferSource,
                    Font.DisplayMode.SEE_THROUGH, BACKGROUND, FULL_BRIGHT);
            poseStack.popPose();
        }
    }

    private static String notableProperties(BlockState state) {
        StringBuilder text = new StringBuilder();
        int shown = 0;
        for (String name : NOTABLE_PROPERTIES) {
            for (Property<?> property : state.getProperties()) {
                if (property.getName().equals(name)) {
                    if (shown++ > 0) {
                        text.append("  ");
                    }
                    text.append(name).append('=').append(valueName(state, property));
                    break;
                }
            }
            if (shown >= MAX_PROPERTIES) {
                break;
            }
        }
        return text.toString();
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }
}
