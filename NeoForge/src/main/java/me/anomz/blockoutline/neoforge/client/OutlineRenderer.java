package me.anomz.blockoutline.neoforge.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.anomz.blockoutline.platform.ConfigHelper;
import me.anomz.blockoutline.platform.Services;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.awt.*;

public class OutlineRenderer {

    // Use the specific subclass!
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.hitResult == null) {
            return;
        }

        HitResult hitResult = mc.hitResult;
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockPos blockPos = blockHitResult.getBlockPos();
        Level level = mc.level;
        BlockState blockState = level.getBlockState(blockPos);
        VoxelShape shape = blockState.getShape(level, blockPos);

        if (shape.isEmpty()) {
            return;
        }

        ConfigHelper config = Services.getConfigHelper();
        Camera camera = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

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

        // Render fill first (so outline renders on top)
        if (config.isFillEnabled()) {
            renderFill(bufferSource, shape, matrix, config);
        }

        // Render outline
        renderOutline(bufferSource, shape, matrix, config);

        poseStack.popPose();
    }

    private static void renderOutline(MultiBufferSource.BufferSource bufferSource, VoxelShape shape, Matrix4f matrix, ConfigHelper config) {
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

        RenderSystem.lineWidth(lineWidth);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

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

        bufferSource.endBatch(RenderType.lines());
    }

    private static void renderFill(MultiBufferSource.BufferSource bufferSource, VoxelShape shape, Matrix4f matrix, ConfigHelper config) {
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

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.debugQuads());

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float sMinX = (float)minX - offset;
            float sMinY = (float)minY - offset;
            float sMinZ = (float)minZ - offset;
            float sMaxX = (float)maxX + offset;
            float sMaxY = (float)maxY + offset;
            float sMaxZ = (float)maxZ + offset;

            // Bottom face (Y-)
            vertexConsumer.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(red, green, blue, alpha);

            // Top face (Y+)
            vertexConsumer.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(red, green, blue, alpha);

            // North face (Z-)
            vertexConsumer.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(red, green, blue, alpha);

            // South face (Z+)
            vertexConsumer.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);

            // West face (X-)
            vertexConsumer.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(red, green, blue, alpha);

            // East face (X+)
            vertexConsumer.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
            vertexConsumer.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
        });

        bufferSource.endBatch(RenderType.debugQuads());
    }
}