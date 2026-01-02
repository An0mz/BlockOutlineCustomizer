package me.anomz.blockoutline.neoforge.client;

import me.anomz.blockoutline.platform.Services;
import me.anomz.blockoutline.platform.ConfigHelper;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * NeoForge 1.21.1 outline renderer
 */
public class OutlineRenderer {

    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
        // Cancel vanilla outline
        event.setCanceled(true);

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        BlockPos blockPos = event.getTarget().getBlockPos();
        Level level = mc.level;
        BlockState blockState = level.getBlockState(blockPos);
        VoxelShape shape = blockState.getShape(level, blockPos);

        if (shape.isEmpty()) {
            return;
        }

        Camera camera = event.getCamera();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource multiBufferSource = event.getMultiBufferSource();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());

        ConfigHelper config = Services.getConfigHelper();

        // Get camera position
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        // Get colors
        final float red, green, blue, alpha;

        if (config.isRgbEnabled()) {
            float speed = (float)config.getRgbSpeed();
            float timeInSeconds = (System.currentTimeMillis() % 100000L) / 1000.0f;
            float hue = (timeInSeconds * speed / 10.0f) % 1.0f;
            Color color = Color.getHSBColor(hue, 1.0f, 1.0f);

            red = color.getRed() / 255.0f;
            green = color.getGreen() / 255.0f;
            blue = color.getBlue() / 255.0f;
        } else {
            red = config.getRed() / 255.0f;
            green = config.getGreen() / 255.0f;
            blue = config.getBlue() / 255.0f;
        }

        alpha = (float)config.getOpacity();
        final float lineWidth = (float)config.getWidth();

        poseStack.pushPose();
        poseStack.translate(
                blockPos.getX() - camX,
                blockPos.getY() - camY,
                blockPos.getZ() - camZ
        );

        Matrix4f matrix = poseStack.last().pose();

        int passes = Math.max(1, (int)lineWidth);
        float offsetIncrement = 0.001f;

        for (int pass = 0; pass < passes; pass++) {
            float offset = pass * offsetIncrement;

            shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
                float dx = (float)(maxX - minX);
                float dy = (float)(maxY - minY);
                float dz = (float)(maxZ - minZ);

                float length = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);

                float normalX = length > 1e-6f ? dx / length : 1.0f;
                float normalY = length > 1e-6f ? dy / length : 0.0f;
                float normalZ = length > 1e-6f ? dz / length : 0.0f;

                vertexConsumer.addVertex(matrix, (float)minX + offset, (float)minY + offset, (float)minZ + offset)
                        .setColor(red, green, blue, alpha)
                        .setNormal(normalX, normalY, normalZ);

                vertexConsumer.addVertex(matrix, (float)maxX + offset, (float)maxY + offset, (float)maxZ + offset)
                        .setColor(red, green, blue, alpha)
                        .setNormal(normalX, normalY, normalZ);
            });
        }

        poseStack.popPose();
    }
}