package me.anomz.blockoutline.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.anomz.blockoutline.client.OverrideResolver;
import me.anomz.blockoutline.client.PreviewState;
import me.anomz.blockoutline.config.BOCConfig;
import me.anomz.blockoutline.config.OutlineStyle;
import me.anomz.blockoutline.config.StyleSettings;
import me.anomz.blockoutline.util.Animations;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Loader-independent outline + fill rendering. Both the Fabric mixin and the
 * NeoForge {@code CustomBlockOutlineRenderer} delegate here.
 */
public final class OutlineRenderCore {
    private static final float DASH_LENGTH = 0.125f;
    private static final float GAP_LENGTH = 0.0625f;
    public static final float DASH_PERIOD = DASH_LENGTH + GAP_LENGTH;
    private static final float CORNER_LENGTH = 0.25f;
    /** How much of the RGB cycle is spread across one block in gradient mode. */
    private static final float GRADIENT_SPREAD = 0.75f;
    /** Subdivision step (in blocks) for smooth per-vertex gradient colors. */
    private static final float GRADIENT_STEP = 0.125f;

    private OutlineRenderCore() {
    }

    /** Emits the from/to distances of the sub-segments a styled edge is split into. */
    public interface SegmentConsumer {
        void accept(float from, float to);
    }

    /** Whether the custom outline should replace vanilla's right now (honors the live GUI preview). */
    public static boolean customOutlineActive() {
        return PreviewState.style != null ? PreviewState.customEnabled : BOCConfig.get().customOutlineEnabled;
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, BlockPos pos, VoxelShape shape, Vec3 cameraPos) {
        BOCConfig config = BOCConfig.get();
        StyleSettings preview = PreviewState.style;
        StyleSettings style = preview != null ? preview : OverrideResolver.resolve(config, pos);

        poseStack.pushPose();
        poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

        double pulse = pulseFactor(style);

        if (style.fillEnabled) {
            renderFill(collector, poseStack, shape, fillColor(style, pulse));
        }
        renderOutline(collector, poseStack, shape, style, pulse);

        poseStack.popPose();
    }

    public static double pulseFactor(StyleSettings style) {
        return style.pulseEnabled ? Animations.pulse(style.pulseSpeed, style.pulseMinOpacity) : 1.0;
    }

    /** Current marching offset for the dashed style (0 when not moving). */
    public static float dashPhase(StyleSettings style) {
        return style.movingEnabled && style.outlineStyle == OutlineStyle.DASHED
                ? Animations.marchOffset(style.movingSpeed, DASH_PERIOD)
                : 0.0f;
    }

    /** Current outline color as packed ARGB, honoring rainbow/palette and pulse. */
    public static int outlineColor(StyleSettings style, double pulse) {
        int rgb;
        if (style.outlineRgbEnabled) {
            rgb = Animations.cycleRgb(style.outlineRgbSpeed, style.palette()) & 0xFFFFFF;
        } else {
            rgb = (style.outlineRed << 16) | (style.outlineGreen << 8) | style.outlineBlue;
        }
        int alpha = (int) Math.round(style.outlineOpacity * pulse * 255.0);
        return (alpha << 24) | rgb;
    }

    /** Current fill color as packed ARGB, honoring rainbow/palette, sync and pulse. */
    public static int fillColor(StyleSettings style, double pulse) {
        int rgb;
        boolean useRgb = style.fillRgbEnabled || (style.syncRgb && style.outlineRgbEnabled);
        if (useRgb) {
            double speed = style.syncRgb && style.outlineRgbEnabled ? style.outlineRgbSpeed : style.fillRgbSpeed;
            rgb = Animations.cycleRgb(speed, style.palette()) & 0xFFFFFF;
        } else {
            rgb = (style.fillRed << 16) | (style.fillGreen << 8) | style.fillBlue;
        }
        int alpha = (int) Math.round(style.fillOpacity * pulse * 255.0);
        return (alpha << 24) | rgb;
    }

    /**
     * Gradient-mode RGB at a point in block-local coordinates: the cycle
     * position shifts with the distance from the block's (0,0,0) corner.
     */
    public static int gradientRgb(StyleSettings style, double baseT, float x, float y, float z) {
        float d = (x + y + z) / 3.0f;
        return Animations.sampleRgb(baseT + d * GRADIENT_SPREAD, style.palette()) & 0xFFFFFF;
    }

    /** Splits an edge of the given length into styled sub-segments; phase shifts the dash pattern. */
    public static void segments(OutlineStyle style, float length, float phase, SegmentConsumer out) {
        switch (style) {
            case FULL -> out.accept(0.0f, length);
            case CORNERS -> {
                float corner = Math.min(CORNER_LENGTH, length * 0.25f);
                if (corner * 2.0f >= length * 0.9f) {
                    out.accept(0.0f, length);
                } else {
                    out.accept(0.0f, corner);
                    out.accept(length - corner, length);
                }
            }
            case DASHED -> {
                for (float t = phase - DASH_PERIOD; t < length; t += DASH_PERIOD) {
                    float from = Math.max(0.0f, t);
                    float to = Math.min(t + DASH_LENGTH, length);
                    if (to > from) {
                        out.accept(from, to);
                    }
                }
            }
        }
    }

    private static void renderOutline(SubmitNodeCollector collector, PoseStack poseStack, VoxelShape shape, StyleSettings style, double pulse) {
        int alpha = (int) Math.round(style.outlineOpacity * pulse * 255.0);
        if (alpha <= 0) {
            return;
        }
        float width = (float) style.outlineWidth;

        if (style.outlineRgbEnabled && style.outlineRgbGradient) {
            submitGradientLines(collector, poseStack, shape, style, alpha << 24, width);
            return;
        }

        int color = (alpha << 24) | (outlineColor(style, pulse) & 0xFFFFFF);
        if (style.outlineStyle == OutlineStyle.FULL) {
            collector.submitShapeOutline(poseStack, shape, RenderTypes.lines(), color, width, false);
        } else {
            submitSegmentedLines(collector, poseStack, shape, style, color, width);
        }
    }

    /**
     * Draws each shape edge as dashes or corner brackets using custom line
     * geometry (the vanilla shape-outline submit can only draw full edges).
     */
    private static void submitSegmentedLines(SubmitNodeCollector collector, PoseStack poseStack, VoxelShape shape, StyleSettings style, int color, float width) {
        float phase = dashPhase(style);
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, vc) ->
                shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    float x1 = (float) minX, y1 = (float) minY, z1 = (float) minZ;
                    float dx = (float) (maxX - minX), dy = (float) (maxY - minY), dz = (float) (maxZ - minZ);
                    float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (length < 1e-6f) {
                        return;
                    }
                    float nx = dx / length, ny = dy / length, nz = dz / length;

                    segments(style.outlineStyle, length, phase, (from, to) ->
                            addLine(vc, pose, x1, y1, z1, nx, ny, nz, from, to, color, color, width));
                }));
    }

    /**
     * Gradient mode: every style is drawn as custom geometry, with edges
     * subdivided so the color can flow across the block from its corner.
     */
    private static void submitGradientLines(SubmitNodeCollector collector, PoseStack poseStack, VoxelShape shape, StyleSettings style, int alphaBits, float width) {
        float phase = dashPhase(style);
        double baseT = Animations.rainbowHue(style.outlineRgbSpeed);
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, vc) ->
                shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    float x1 = (float) minX, y1 = (float) minY, z1 = (float) minZ;
                    float dx = (float) (maxX - minX), dy = (float) (maxY - minY), dz = (float) (maxZ - minZ);
                    float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (length < 1e-6f) {
                        return;
                    }
                    float nx = dx / length, ny = dy / length, nz = dz / length;

                    segments(style.outlineStyle, length, phase, (from, to) -> {
                        int steps = Math.max(1, (int) Math.ceil((to - from) / GRADIENT_STEP));
                        for (int i = 0; i < steps; i++) {
                            float f0 = from + (to - from) * i / steps;
                            float f1 = from + (to - from) * (i + 1) / steps;
                            int c0 = alphaBits | gradientRgb(style, baseT, x1 + nx * f0, y1 + ny * f0, z1 + nz * f0);
                            int c1 = alphaBits | gradientRgb(style, baseT, x1 + nx * f1, y1 + ny * f1, z1 + nz * f1);
                            addLine(vc, pose, x1, y1, z1, nx, ny, nz, f0, f1, c0, c1, width);
                        }
                    });
                }));
    }

    private static void addLine(VertexConsumer vc, PoseStack.Pose pose,
                                float x, float y, float z,
                                float nx, float ny, float nz,
                                float from, float to, int colorFrom, int colorTo, float width) {
        vc.addVertex(pose, x + nx * from, y + ny * from, z + nz * from)
                .setColor(colorFrom)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(width);
        vc.addVertex(pose, x + nx * to, y + ny * to, z + nz * to)
                .setColor(colorTo)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(width);
    }

    private static void renderFill(SubmitNodeCollector collector, PoseStack poseStack, VoxelShape shape, int color) {
        if ((color >>> 24) == 0) {
            return;
        }
        final float offset = 0.001f;

        collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, vc) -> {
            org.joml.Matrix4f matrix = pose.pose();
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                float sMinX = (float) minX - offset;
                float sMinY = (float) minY - offset;
                float sMinZ = (float) minZ - offset;
                float sMaxX = (float) maxX + offset;
                float sMaxY = (float) maxY + offset;
                float sMaxZ = (float) maxZ + offset;

                // Bottom face (Y-)
                vc.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(color);
                vc.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(color);
                vc.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(color);
                vc.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(color);

                // Top face (Y+)
                vc.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(color);
                vc.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(color);
                vc.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(color);
                vc.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(color);

                // North face (Z-)
                vc.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(color);
                vc.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(color);
                vc.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(color);
                vc.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(color);

                // South face (Z+)
                vc.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(color);
                vc.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(color);
                vc.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(color);
                vc.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(color);

                // West face (X-)
                vc.addVertex(matrix, sMinX, sMinY, sMinZ).setColor(color);
                vc.addVertex(matrix, sMinX, sMinY, sMaxZ).setColor(color);
                vc.addVertex(matrix, sMinX, sMaxY, sMaxZ).setColor(color);
                vc.addVertex(matrix, sMinX, sMaxY, sMinZ).setColor(color);

                // East face (X+)
                vc.addVertex(matrix, sMaxX, sMinY, sMinZ).setColor(color);
                vc.addVertex(matrix, sMaxX, sMaxY, sMinZ).setColor(color);
                vc.addVertex(matrix, sMaxX, sMaxY, sMaxZ).setColor(color);
                vc.addVertex(matrix, sMaxX, sMinY, sMaxZ).setColor(color);
            });
        });
    }
}
