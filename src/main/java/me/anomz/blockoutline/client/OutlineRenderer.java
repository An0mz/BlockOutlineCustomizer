package me.anomz.blockoutline.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.anomz.blockoutline.config.OutlineConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.joml.Matrix4f;

import java.awt.*;

public class OutlineRenderer {

    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
        event.setCanceled(true);

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        BlockPos blockPos = event.getTarget().getBlockPos();
        Level level = mc.level;
        BlockState blockState = level.getBlockState(blockPos);
        VoxelShape shape = blockState.getShape(level, blockPos);

        if (shape.isEmpty()) {
            return;
        }

        var camera = event.getCamera();
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

// Get color values
        final float red, green, blue, alpha;

        if (OutlineConfig.RGB_ENABLED.get()) {
            float speed = OutlineConfig.RGB_SPEED.get().floatValue();
            // Convert to seconds and keep number manageable
            float timeInSeconds = (System.currentTimeMillis() % 100000L) / 1000.0f;
            // Calculate hue that cycles from 0.0 to 1.0
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

// Get vertex consumer for lines
        VertexConsumer vertexConsumer = event.getMultiBufferSource().getBuffer(RenderType.lines());

// Translate to block position
        poseStack.pushPose();
        poseStack.translate(
                blockPos.getX() - camX,
                blockPos.getY() - camY,
                blockPos.getZ() - camZ
        );

        Matrix4f matrix = poseStack.last().pose();

// Draw outline multiple times with offsets to simulate thickness
        int passes = Math.max(1, (int)(lineWidth));
        float offsetIncrement = 0.001f;

        for (int pass = 0; pass < passes; pass++) {
            float offset = pass * offsetIncrement;

            // Draw all edges of the shape
            shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
                // Calculate normal vector for the edge
                float dx = (float)(maxX - minX);
                float dy = (float)(maxY - minY);
                float dz = (float)(maxZ - minZ);

                float length = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);

                // Normalize
                float normalX = length > 0 ? dx / length : 0;
                float normalY = length > 0 ? dy / length : 0;
                float normalZ = length > 0 ? dz / length : 0;

                // Add the two vertices for this edge with slight offset
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