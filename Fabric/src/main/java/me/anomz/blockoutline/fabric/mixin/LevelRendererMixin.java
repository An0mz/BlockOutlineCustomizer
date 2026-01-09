package me.anomz.blockoutline.fabric.mixin;

import me.anomz.blockoutline.platform.Services;
import me.anomz.blockoutline.platform.ConfigHelper;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderHitOutline", at = @At("HEAD"), cancellable = true)
    private void onRenderHitOutline(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            double camX,
            double camY,
            double camZ,
            BlockOutlineRenderState blockOutlineRenderState,
            int packedColor,
            float partialTick,
            CallbackInfo ci
    ) {
        ci.cancel();

        BlockPos blockPos = blockOutlineRenderState.pos();
        VoxelShape shape = blockOutlineRenderState.shape();

        if (shape.isEmpty()) {
            return;
        }

        ConfigHelper config = Services.getConfigHelper();

        poseStack.pushPose();
        poseStack.translate(
                (double)blockPos.getX() - camX,
                (double)blockPos.getY() - camY,
                (double)blockPos.getZ() - camZ
        );

        Matrix4f matrix = poseStack.last().pose();

        // Get buffer source - we need to get both consumers from here
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // Render fill first (if enabled)
        if (config.isFillEnabled()) {
            renderFill(bufferSource, shape, matrix, config);
        }

        // Render outline - get our own line consumer since we cancelled vanilla setup
        renderOutline(bufferSource, shape, matrix, config);

        poseStack.popPose();
    }

    private void renderOutline(MultiBufferSource.BufferSource bufferSource, VoxelShape shape, Matrix4f matrix, ConfigHelper config) {
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

        // Get our own vertex consumer for lines
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderTypes.lines());

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

                lineConsumer.addVertex(matrix, (float)minX + offset, (float)minY + offset, (float)minZ + offset)
                        .setColor(red, green, blue, alpha)
                        .setNormal(normalX, normalY, normalZ)
                        .setLineWidth(lineWidth);

                lineConsumer.addVertex(matrix, (float)maxX + offset, (float)maxY + offset, (float)maxZ + offset)
                        .setColor(red, green, blue, alpha)
                        .setNormal(normalX, normalY, normalZ)
                        .setLineWidth(lineWidth);
            });
        }
    }

    private void renderFill(MultiBufferSource.BufferSource bufferSource, VoxelShape shape, Matrix4f matrix, ConfigHelper config) {
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

        VertexConsumer fillConsumer = bufferSource.getBuffer(RenderTypes.debugQuads());

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float sMinX = (float)minX - offset;
            float sMinY = (float)minY - offset;
            float sMinZ = (float)minZ - offset;
            float sMaxX = (float)maxX + offset;
            float sMaxY = (float)maxY + offset;
            float sMaxZ = (float)maxZ + offset;

            // Bottom face (Y-)
            fillConsumer.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(red, green, blue, alpha);

            // Top face (Y+)
            fillConsumer.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(red, green, blue, alpha);

            // North face (Z-)
            fillConsumer.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(red, green, blue, alpha);

            // South face (Z+)
            fillConsumer.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);

            // West face (X-)
            fillConsumer.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(red, green, blue, alpha);

            // East face (X+)
            fillConsumer.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
            fillConsumer.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
        });
    }
}