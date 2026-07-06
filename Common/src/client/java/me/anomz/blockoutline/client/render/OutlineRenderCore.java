package me.anomz.blockoutline.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.anomz.blockoutline.client.OverrideResolver;
import me.anomz.blockoutline.config.BOCConfig;
import me.anomz.blockoutline.config.StyleSettings;
import me.anomz.blockoutline.util.Animations;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

/**
 * Loader-independent outline + fill rendering. Both the Fabric mixin and the
 * NeoForge {@code CustomBlockOutlineRenderer} delegate here.
 */
public final class OutlineRenderCore {
    private static final float DASH_LENGTH = 0.125f;
    private static final float GAP_LENGTH = 0.0625f;
    private static final float CORNER_LENGTH = 0.25f;

    private OutlineRenderCore() {
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, BlockPos pos, VoxelShape shape, Vec3 cameraPos) {
        BOCConfig config = BOCConfig.get();
        StyleSettings style = OverrideResolver.resolve(config, pos);

        poseStack.pushPose();
        poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

        double pulse = style.pulseEnabled ? Animations.pulse(style.pulseSpeed, style.pulseMinOpacity) : 1.0;

        if (style.fillEnabled) {
            renderFill(collector, poseStack, shape, style, pulse);
        }
        renderOutline(collector, poseStack, shape, style, pulse);

        poseStack.popPose();
    }

    private static void renderOutline(SubmitNodeCollector collector, PoseStack poseStack, VoxelShape shape, StyleSettings style, double pulse) {
        int rgb;
        if (style.outlineRgbEnabled) {
            rgb = Animations.rainbowRgb(style.outlineRgbSpeed) & 0xFFFFFF;
        } else {
            rgb = (style.outlineRed << 16) | (style.outlineGreen << 8) | style.outlineBlue;
        }

        int alpha = (int) Math.round(style.outlineOpacity * pulse * 255.0);
        if (alpha <= 0) {
            return;
        }
        int color = (alpha << 24) | rgb;
        float width = (float) style.outlineWidth;

        switch (style.outlineStyle) {
            case FULL -> collector.submitShapeOutline(poseStack, shape, RenderTypes.lines(), color, width, false);
            case DASHED -> submitSegmentedLines(collector, poseStack, shape, color, width, false);
            case CORNERS -> submitSegmentedLines(collector, poseStack, shape, color, width, true);
        }
    }

    /**
     * Draws each shape edge as dashes or corner brackets using custom line
     * geometry (the vanilla shape-outline submit can only draw full edges).
     */
    private static void submitSegmentedLines(SubmitNodeCollector collector, PoseStack poseStack, VoxelShape shape, int color, float width, boolean corners) {
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, vc) ->
                shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    float x1 = (float) minX, y1 = (float) minY, z1 = (float) minZ;
                    float dx = (float) (maxX - minX), dy = (float) (maxY - minY), dz = (float) (maxZ - minZ);
                    float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (length < 1e-6f) {
                        return;
                    }
                    float nx = dx / length, ny = dy / length, nz = dz / length;

                    if (corners) {
                        float corner = Math.min(CORNER_LENGTH, length * 0.25f);
                        if (corner * 2.0f >= length * 0.9f) {
                            addLine(vc, pose, x1, y1, z1, nx, ny, nz, 0.0f, length, color, width);
                        } else {
                            addLine(vc, pose, x1, y1, z1, nx, ny, nz, 0.0f, corner, color, width);
                            addLine(vc, pose, x1, y1, z1, nx, ny, nz, length - corner, length, color, width);
                        }
                    } else {
                        for (float t = 0.0f; t < length; t += DASH_LENGTH + GAP_LENGTH) {
                            addLine(vc, pose, x1, y1, z1, nx, ny, nz, t, Math.min(t + DASH_LENGTH, length), color, width);
                        }
                    }
                }));
    }

    private static void addLine(VertexConsumer vc, PoseStack.Pose pose,
                                float x, float y, float z,
                                float nx, float ny, float nz,
                                float from, float to, int color, float width) {
        vc.addVertex(pose, x + nx * from, y + ny * from, z + nz * from)
                .setColor(color)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(width);
        vc.addVertex(pose, x + nx * to, y + ny * to, z + nz * to)
                .setColor(color)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(width);
    }

    private static void renderFill(SubmitNodeCollector collector, PoseStack poseStack, VoxelShape shape, StyleSettings style, double pulse) {
        final float red, green, blue;

        boolean useRgb = style.fillRgbEnabled || (style.syncRgb && style.outlineRgbEnabled);
        if (useRgb) {
            double speed = style.syncRgb && style.outlineRgbEnabled ? style.outlineRgbSpeed : style.fillRgbSpeed;
            int rgb = Animations.rainbowRgb(speed);
            red = ((rgb >> 16) & 0xFF) / 255.0f;
            green = ((rgb >> 8) & 0xFF) / 255.0f;
            blue = (rgb & 0xFF) / 255.0f;
        } else {
            red = style.fillRed / 255.0f;
            green = style.fillGreen / 255.0f;
            blue = style.fillBlue / 255.0f;
        }

        final float alpha = (float) (style.fillOpacity * pulse);
        if (alpha <= 0.0f) {
            return;
        }
        final float offset = 0.001f;

        collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, vc) -> {
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
