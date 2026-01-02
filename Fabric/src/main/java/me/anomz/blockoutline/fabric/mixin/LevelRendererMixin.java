package me.anomz.blockoutline.fabric.mixin;

import me.anomz.blockoutline.platform.Services;
import me.anomz.blockoutline.platform.ConfigHelper;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.LevelRenderer;
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
            CallbackInfo ci
    ) {
        ci.cancel();

        BlockPos blockPos = blockOutlineRenderState.pos();
        VoxelShape shape = blockOutlineRenderState.shape();

        if (shape.isEmpty()) {
            return;
        }

        ConfigHelper config = Services.getConfigHelper();

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
                (double)blockPos.getX() - camX,
                (double)blockPos.getY() - camY,
                (double)blockPos.getZ() - camZ
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