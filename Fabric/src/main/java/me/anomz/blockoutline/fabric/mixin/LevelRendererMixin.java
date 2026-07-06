package me.anomz.blockoutline.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.anomz.blockoutline.platform.ConfigHelper;
import me.anomz.blockoutline.platform.Services;
import me.anomz.blockoutline.util.RainbowColor;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "submitBlockOutline", at = @At("HEAD"), cancellable = true)
    private void onSubmitBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, CallbackInfo ci) {
        ConfigHelper config = Services.getConfigHelper();
        if (!config.isCustomOutlineEnabled()) return;

        ci.cancel();

        BlockOutlineRenderState outlineState = levelRenderState.blockOutlineRenderState;
        if (outlineState == null) return;

        BlockPos blockPos = outlineState.pos();
        VoxelShape shape = outlineState.shape();
        if (shape.isEmpty()) return;

        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;

        poseStack.pushPose();
        poseStack.translate(blockPos.getX() - cameraPos.x, blockPos.getY() - cameraPos.y, blockPos.getZ() - cameraPos.z);

        if (config.isFillEnabled()) {
            renderFill(submitNodeCollector, poseStack, shape, config);
        }
        renderOutline(submitNodeCollector, poseStack, shape, config);

        poseStack.popPose();
    }

    @Unique
    private static void renderOutline(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, VoxelShape shape, ConfigHelper config) {
        int rgb;

        if (config.isOutlineRgbEnabled()) {
            rgb = RainbowColor.rgb(config.getOutlineRgbSpeed()) & 0xFFFFFF;
        } else {
            rgb = (config.getOutlineRed() << 16) | (config.getOutlineGreen() << 8) | config.getOutlineBlue();
        }

        int alpha = (int) (config.getOutlineOpacity() * 255);
        float lineWidth = (float) config.getOutlineWidth();
        int packedColor = (alpha << 24) | rgb;

        submitNodeCollector.submitShapeOutline(poseStack, shape, RenderTypes.lines(), packedColor, lineWidth, false);
    }

    @Unique
    private static void renderFill(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, VoxelShape shape, ConfigHelper config) {
        final float red, green, blue;

        boolean useRgb = config.isFillRgbEnabled() || (config.isSyncRgb() && config.isOutlineRgbEnabled());
        if (useRgb) {
            double speed = config.isSyncRgb() && config.isOutlineRgbEnabled()
                    ? config.getOutlineRgbSpeed() : config.getFillRgbSpeed();
            int rgb = RainbowColor.rgb(speed);
            red = ((rgb >> 16) & 0xFF) / 255.0f;
            green = ((rgb >> 8) & 0xFF) / 255.0f;
            blue = (rgb & 0xFF) / 255.0f;
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
