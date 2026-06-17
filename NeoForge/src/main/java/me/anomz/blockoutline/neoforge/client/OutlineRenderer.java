package me.anomz.blockoutline.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.anomz.blockoutline.platform.ConfigHelper;
import me.anomz.blockoutline.platform.Services;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import org.joml.Matrix4f;

import java.awt.Color;

public class OutlineRenderer implements CustomBlockOutlineRenderer {

    @Override
    public boolean render(BlockOutlineRenderState renderState, SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState) {
        ConfigHelper config = Services.getConfigHelper();
        if (!config.isCustomOutlineEnabled()) return false;

        BlockPos blockPos = renderState.pos();
        VoxelShape shape = renderState.shape();
        if (shape.isEmpty()) return false;

        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;

        poseStack.pushPose();
        poseStack.translate(blockPos.getX() - cameraPos.x, blockPos.getY() - cameraPos.y, blockPos.getZ() - cameraPos.z);

        if (config.isFillEnabled()) {
            renderFill(submitNodeCollector, poseStack, shape, config);
        }
        renderOutline(submitNodeCollector, poseStack, shape, config);

        poseStack.popPose();
        return true;
    }

    private static void renderOutline(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, VoxelShape shape, ConfigHelper config) {
        int red, green, blue;

        if (config.isOutlineRgbEnabled()) {
            float speed = (float) config.getOutlineRgbSpeed();
            float timeInSeconds = (System.currentTimeMillis() % 100000L) / 1000.0f;
            float hue = (timeInSeconds * speed / 10.0f) % 1.0f;
            Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
            red = color.getRed();
            green = color.getGreen();
            blue = color.getBlue();
        } else {
            red = config.getOutlineRed();
            green = config.getOutlineGreen();
            blue = config.getOutlineBlue();
        }

        int alpha = (int) (config.getOutlineOpacity() * 255);
        float lineWidth = (float) config.getOutlineWidth();
        int packedColor = (alpha << 24) | (red << 16) | (green << 8) | blue;

        submitNodeCollector.submitShapeOutline(poseStack, shape, RenderTypes.lines(), packedColor, lineWidth, false);
    }

    private static void renderFill(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, VoxelShape shape, ConfigHelper config) {
        final float red, green, blue;

        boolean useRgb = config.isFillRgbEnabled() || (config.isSyncRgb() && config.isOutlineRgbEnabled());
        if (useRgb) {
            float speed = config.isSyncRgb() && config.isOutlineRgbEnabled()
                    ? (float) config.getOutlineRgbSpeed() : (float) config.getFillRgbSpeed();
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

        final float alpha = (float) config.getFillOpacity();
        final float offset = 0.001f;

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, vc) -> {
            Matrix4f matrix = pose.pose();
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                float sMinX = (float) minX - offset;
                float sMinY = (float) minY - offset;
                float sMinZ = (float) minZ - offset;
                float sMaxX = (float) maxX + offset;
                float sMaxY = (float) maxY + offset;
                float sMaxZ = (float) maxZ + offset;

                // Bottom face (Y-)
                vc.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(red, green, blue, alpha);

                // Top face (Y+)
                vc.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(red, green, blue, alpha);

                // North face (Z-)
                vc.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(red, green, blue, alpha);

                // South face (Z+)
                vc.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);

                // West face (X-)
                vc.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(red, green, blue, alpha);

                // East face (X+)
                vc.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(red, green, blue, alpha);
                vc.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(red, green, blue, alpha);
            });
        });
    }
}
