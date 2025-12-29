package me.anomz.blockoutline.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.anomz.blockoutline.config.OutlineConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
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

        Camera camera = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();

        double camX = camera.position().x;
        double camY = camera.position().y;
        double camZ = camera.position().z;

        final float red, green, blue, alpha;

        if (OutlineConfig.RGB_ENABLED.get()) {
            float speed = OutlineConfig.RGB_SPEED.get().floatValue();
            float timeInSeconds = (System.currentTimeMillis() % 100000L) / 1000.0f;
            float hue = (timeInSeconds * speed / 10.0f) % 1.0f;
            Color color = Color.getHSBColor(hue, 1.0f, 1.0f);

            red = color.getRed() / 255.0f;
            green = color.getGreen() / 255.0f;
            blue = color.getBlue() / 255.0f;
        } else {
            red = OutlineConfig.RED.get() / 255.0f;
            green = OutlineConfig.GREEN.get() / 255.0f;
            blue = OutlineConfig.BLUE.get() / 255.0f;
        }

        alpha = OutlineConfig.OPACITY.get().floatValue();
        final float lineWidth = OutlineConfig.WIDTH.get().floatValue();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderTypes.LINES);


        poseStack.pushPose();
        poseStack.translate(
                blockPos.getX() - camX,
                blockPos.getY() - camY,
                blockPos.getZ() - camZ
        );

        Matrix4f matrix = poseStack.last().pose();

        shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float dx = (float)(maxX - minX);
            float dy = (float)(maxY - minY);
            float dz = (float)(maxZ - minZ);

            float length = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);

            float normalX = length > 1e-6f ? dx / length : 1.0f;
            float normalY = length > 1e-6f ? dy / length : 0.0f;
            float normalZ = length > 1e-6f ? dz / length : 0.0f;

            vertexConsumer.addVertex(matrix, (float)minX, (float)minY, (float)minZ)
                    .setColor(red, green, blue, alpha)
                    .setNormal(normalX, normalY, normalZ)
                    .setLineWidth(lineWidth);

            vertexConsumer.addVertex(matrix, (float)maxX, (float)maxY, (float)maxZ)
                    .setColor(red, green, blue, alpha)
                    .setNormal(normalX, normalY, normalZ)
                    .setLineWidth(lineWidth);
        });

        poseStack.popPose();
        bufferSource.endBatch(RenderTypes.LINES);
    }
}