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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * NeoForge outline renderer with fill support
 */
public class OutlineRenderer {

    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
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

        ConfigHelper config = Services.getConfigHelper();

        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        poseStack.pushPose();
        poseStack.translate(
                blockPos.getX() - camX,
                blockPos.getY() - camY,
                blockPos.getZ() - camZ
        );

        Matrix4f matrix = poseStack.last().pose();

        // Render fill first
        if (config.isFillEnabled()) {
            renderFill(multiBufferSource, shape, matrix, config);
        }

        // Render outline
        renderOutline(multiBufferSource, shape, matrix, config);

        poseStack.popPose();
    }

    private static void renderOutline(MultiBufferSource multiBufferSource, VoxelShape shape, Matrix4f matrix, ConfigHelper config) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());

        float red, green, blue, alpha;

        if (config.isOutlineRgbEnabled()) {
            float speed = (float)config.getOutlineRgbSpeed();
            float timeInSeconds = (System.currentTimeMillis() % 100000L) / 1000.0f;
            float hue = (timeInSeconds * speed / 10.0f) % 1.0f;
            Color color = Color.getHSBColor(hue, 1.0f, 1.0f);

            red = color.getRed() / 255.0f;
            green = color.getGreen() / 255.0f;
            blue = color.getBlue() / 255.0f;
        } else {
            red = config.getOutlineRed() / 255.0f;
            green = config.getOutlineGreen() / 255.0f;
            blue = config.getOutlineBlue() / 255.0f;
        }

        alpha = (float)config.getOutlineOpacity();
        float lineWidth = (float)config.getOutlineWidth();

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

                vertexConsumer.vertex(matrix, (float)minX + offset, (float)minY + offset, (float)minZ + offset)
                        .color(red, green, blue, alpha)
                        .normal(normalX, normalY, normalZ)
                        .endVertex();

                vertexConsumer.vertex(matrix, (float)maxX + offset, (float)maxY + offset, (float)maxZ + offset)
                        .color(red, green, blue, alpha)
                        .normal(normalX, normalY, normalZ)
                        .endVertex();
            });
        }
    }

    private static void renderFill(MultiBufferSource multiBufferSource, VoxelShape shape, Matrix4f matrix, ConfigHelper config) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugQuads());

        float red, green, blue, alpha;

        if (config.isFillRgbEnabled()) {
            float speed = (float)config.getFillRgbSpeed();
            float timeInSeconds = (System.currentTimeMillis() % 100000L) / 1000.0f;
            float hue = (timeInSeconds * speed / 10.0f) % 1.0f;
            Color color = Color.getHSBColor(hue, 1.0f, 1.0f);

            red = color.getRed() / 255.0f;
            green = color.getGreen() / 255.0f;
            blue = color.getBlue() / 255.0f;
        } else {
            red = config.getFillRed() / 255.0f;
            green = config.getFillGreen() / 255.0f;
            blue = config.getFillBlue() / 255.0f;
        }

        alpha = (float)config.getFillOpacity();

        float offset = 0.001f;

        // Render all faces of the shape
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            // Making sure the fill is not clipping with texture
            float sMinX = (float)minX - offset;
            float sMinY = (float)minY - offset;
            float sMinZ = (float)minZ - offset;
            float sMaxX = (float)maxX + offset;
            float sMaxY = (float)maxY + offset;
            float sMaxZ = (float)maxZ + offset;

            // Bottom face (Y-)
            vertexConsumer.vertex(matrix, sMinX, sMinY, sMinZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMaxX, sMinY, sMinZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMaxX, sMinY, sMaxZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMinX, sMinY, sMaxZ).color(red, green, blue, alpha).endVertex();

            // Top face (Y+)
            vertexConsumer.vertex(matrix, sMinX, sMaxY, sMinZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMinX, sMaxY, sMaxZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMaxX, sMaxY, sMaxZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMaxX, sMaxY, sMinZ).color(red, green, blue, alpha).endVertex();

            // North face (Z-)
            vertexConsumer.vertex(matrix, sMinX, sMinY, sMinZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMinX, sMaxY, sMinZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMaxX, sMaxY, sMinZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMaxX, sMinY, sMinZ).color(red, green, blue, alpha).endVertex();

            // South face (Z+)
            vertexConsumer.vertex(matrix, sMinX, sMinY, sMaxZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMaxX, sMinY, sMaxZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMaxX, sMaxY, sMaxZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMinX, sMaxY, sMaxZ).color(red, green, blue, alpha).endVertex();

            // West face (X-)
            vertexConsumer.vertex(matrix, sMinX, sMinY, sMinZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMinX, sMinY, sMaxZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMinX, sMaxY, sMaxZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMinX, sMaxY, sMinZ).color(red, green, blue, alpha).endVertex();

            // East face (X+)
            vertexConsumer.vertex(matrix, sMaxX, sMinY, sMinZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMaxX, sMaxY, sMinZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMaxX, sMaxY, sMaxZ).color(red, green, blue, alpha).endVertex();
            vertexConsumer.vertex(matrix, sMaxX, sMinY, sMaxZ).color(red, green, blue, alpha).endVertex();
        });
    }
}