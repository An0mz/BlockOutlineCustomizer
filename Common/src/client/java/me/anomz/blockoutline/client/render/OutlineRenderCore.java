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
import net.minecraft.client.renderer.rendertype.RenderType;
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
    public static final float DASH_PERIOD = DASH_LENGTH + GAP_LENGTH;
    private static final float CORNER_LENGTH = 0.25f;
    /** How much of the RGB cycle is spread across one block in gradient mode. */
    private static final float GRADIENT_SPREAD = 0.75f;
    /** Subdivision step (in blocks) for smooth per-vertex gradient colors. */
    private static final float GRADIENT_STEP = 0.125f;

    private static final net.minecraft.resources.Identifier WHITE_TEXTURE =
            net.minecraft.resources.Identifier.fromNamespaceAndPath(me.anomz.blockoutline.Constants.MOD_ID, "textures/white.png");
    /** Packed fullbright lightmap coords (block 15 / sky 15). */
    private static final int FULL_BRIGHT = 0xF000F0;

    private OutlineRenderCore() {
    }

    /**
     * Render type for our solid-color quads (fill + shader-fix outline).
     * Entity-translucent-emissive geometry is processed by shader packs,
     * unlike the debug render types which Iris drops when a pack is active.
     * Known limitation: some packs render entity translucency without alpha
     * blending, making the opacity sliders act as on/off under those packs.
     */
    private static RenderType quadRenderType() {
        return RenderTypes.entityTranslucentEmissive(WHITE_TEXTURE);
    }

    private static void quadVertex(VertexConsumer vc, Matrix4f matrix,
                                   float x, float y, float z, int color, float u, float v,
                                   float nx, float ny, float nz) {
        vc.addVertex(matrix, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(nx, ny, nz);
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
        renderOutline(collector, poseStack, shape, style, pulse,
                (float) (pos.getX() - cameraPos.x), (float) (pos.getY() - cameraPos.y), (float) (pos.getZ() - cameraPos.z));

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

    private static void renderOutline(SubmitNodeCollector collector, PoseStack poseStack, VoxelShape shape, StyleSettings style, double pulse,
                                      float relX, float relY, float relZ) {
        int alpha = (int) Math.round(style.outlineOpacity * pulse * 255.0);
        if (alpha <= 0) {
            return;
        }
        float width = (float) style.outlineWidth;
        boolean gradient = style.outlineRgbEnabled && style.outlineRgbGradient;
        int uniformColor = (alpha << 24) | (outlineColor(style, pulse) & 0xFFFFFF);

        if (style.outlineQuadWidth) {
            renderQuadOutline(collector, poseStack, shape, style, gradient, alpha << 24, uniformColor, width, relX, relY, relZ);
            return;
        }

        if (gradient) {
            submitGradientLines(collector, poseStack, shape, style, alpha << 24, width);
            return;
        }

        if (style.outlineStyle == OutlineStyle.FULL) {
            collector.submitShapeOutline(poseStack, shape, RenderTypes.lines(), uniformColor, width, false);
        } else {
            submitSegmentedLines(collector, poseStack, shape, style, uniformColor, width);
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

    /**
     * Shader-fix width mode: draws each outline segment as a camera-facing quad
     * ribbon through the entity pipeline, so the width works even when a
     * resource/shader pack overrides the vanilla line shader. The ribbon width
     * scales with distance to stay roughly constant on screen.
     */
    private static void renderQuadOutline(SubmitNodeCollector collector, PoseStack poseStack, VoxelShape shape, StyleSettings style,
                                          boolean gradient, int alphaBits, int uniformColor, float width,
                                          float relX, float relY, float relZ) {
        float phase = dashPhase(style);
        double baseT = Animations.rainbowHue(style.outlineRgbSpeed);
        int screenHeight = Math.max(1, net.minecraft.client.Minecraft.getInstance().getWindow().getHeight());
        // World units per requested pixel per block of distance, assuming ~70 degree FOV.
        float widthFactor = width * 1.4f / screenHeight;

        collector.submitCustomGeometry(poseStack, quadRenderType(), (pose, vc) -> {
            Matrix4f matrix = pose.pose();
            shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
                float x1 = (float) minX, y1 = (float) minY, z1 = (float) minZ;
                float dx = (float) (maxX - minX), dy = (float) (maxY - minY), dz = (float) (maxZ - minZ);
                float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (length < 1e-6f) {
                    return;
                }
                float nx = dx / length, ny = dy / length, nz = dz / length;

                segments(style.outlineStyle, length, phase, (from, to) -> {
                    if (gradient) {
                        int steps = Math.max(1, (int) Math.ceil((to - from) / GRADIENT_STEP));
                        for (int i = 0; i < steps; i++) {
                            float f0 = from + (to - from) * i / steps;
                            float f1 = from + (to - from) * (i + 1) / steps;
                            int c0 = alphaBits | gradientRgb(style, baseT, x1 + nx * f0, y1 + ny * f0, z1 + nz * f0);
                            int c1 = alphaBits | gradientRgb(style, baseT, x1 + nx * f1, y1 + ny * f1, z1 + nz * f1);
                            emitRibbon(vc, matrix, x1, y1, z1, nx, ny, nz, f0, f1, c0, c1, relX, relY, relZ, widthFactor);
                        }
                    } else {
                        emitRibbon(vc, matrix, x1, y1, z1, nx, ny, nz, from, to, uniformColor, uniformColor, relX, relY, relZ, widthFactor);
                    }
                });
            });
        });
    }

    private static void emitRibbon(VertexConsumer vc, Matrix4f matrix,
                                   float x, float y, float z,
                                   float nx, float ny, float nz,
                                   float from, float to, int colorFrom, int colorTo,
                                   float relX, float relY, float relZ, float widthFactor) {
        float p0x = x + nx * from, p0y = y + ny * from, p0z = z + nz * from;
        float p1x = x + nx * to, p1y = y + ny * to, p1z = z + nz * to;

        // Camera-relative midpoint (camera sits at -rel in local space)
        float mx = (p0x + p1x) / 2.0f + relX;
        float my = (p0y + p1y) / 2.0f + relY;
        float mz = (p0z + p1z) / 2.0f + relZ;
        float dist = (float) Math.sqrt(mx * mx + my * my + mz * mz);
        if (dist < 1e-4f) {
            return;
        }

        // Perpendicular to the edge and the view direction -> screen-facing ribbon
        float sx = ny * mz - nz * my;
        float sy = nz * mx - nx * mz;
        float sz = nx * my - ny * mx;
        float sl = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (sl < 1e-5f) {
            // Edge points straight at the camera; any perpendicular works
            sx = -ny;
            sy = nx;
            sz = 0;
            sl = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
            if (sl < 1e-5f) {
                sx = 0;
                sy = 0;
                sz = 1;
                sl = 1;
            }
        }
        float half = Math.max(dist * widthFactor, 0.004f) / 2.0f;
        sx = sx / sl * half;
        sy = sy / sl * half;
        sz = sz / sl * half;

        // Pull slightly toward the camera to avoid z-fighting with block faces
        // (same idea as the vanilla line shader's VIEW_SHRINK).
        final float shrink = 0.994f;
        float a0x = (p0x + relX) * shrink - relX, a0y = (p0y + relY) * shrink - relY, a0z = (p0z + relZ) * shrink - relZ;
        float a1x = (p1x + relX) * shrink - relX, a1y = (p1y + relY) * shrink - relY, a1z = (p1z + relZ) * shrink - relZ;

        // Normal pointing back at the camera
        float vnx = -mx / dist, vny = -my / dist, vnz = -mz / dist;

        // Both windings so the ribbon is visible regardless of cull direction
        quadVertex(vc, matrix, a0x + sx, a0y + sy, a0z + sz, colorFrom, 0, 0, vnx, vny, vnz);
        quadVertex(vc, matrix, a0x - sx, a0y - sy, a0z - sz, colorFrom, 0, 1, vnx, vny, vnz);
        quadVertex(vc, matrix, a1x - sx, a1y - sy, a1z - sz, colorTo, 1, 1, vnx, vny, vnz);
        quadVertex(vc, matrix, a1x + sx, a1y + sy, a1z + sz, colorTo, 1, 0, vnx, vny, vnz);

        quadVertex(vc, matrix, a1x + sx, a1y + sy, a1z + sz, colorTo, 1, 0, vnx, vny, vnz);
        quadVertex(vc, matrix, a1x - sx, a1y - sy, a1z - sz, colorTo, 1, 1, vnx, vny, vnz);
        quadVertex(vc, matrix, a0x - sx, a0y - sy, a0z - sz, colorFrom, 0, 1, vnx, vny, vnz);
        quadVertex(vc, matrix, a0x + sx, a0y + sy, a0z + sz, colorFrom, 0, 0, vnx, vny, vnz);
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

        collector.submitCustomGeometry(poseStack, quadRenderType(), (pose, vc) -> {
            Matrix4f matrix = pose.pose();
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                float x0 = (float) minX - offset;
                float y0 = (float) minY - offset;
                float z0 = (float) minZ - offset;
                float x1 = (float) maxX + offset;
                float y1 = (float) maxY + offset;
                float z1 = (float) maxZ + offset;

                // Bottom (Y-), Top (Y+)
                fillFace(vc, matrix, color, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
                fillFace(vc, matrix, color, 0, 1, 0, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
                // North (Z-), South (Z+)
                fillFace(vc, matrix, color, 0, 0, -1, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
                fillFace(vc, matrix, color, 0, 0, 1, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
                // West (X-), East (X+)
                fillFace(vc, matrix, color, -1, 0, 0, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
                fillFace(vc, matrix, color, 1, 0, 0, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
            });
        });
    }

    /** One solid face, emitted with both windings so culling can't hide it. */
    private static void fillFace(VertexConsumer vc, Matrix4f matrix, int color,
                                 float nx, float ny, float nz,
                                 float ax, float ay, float az, float bx, float by, float bz,
                                 float cx, float cy, float cz, float dx, float dy, float dz) {
        quadVertex(vc, matrix, ax, ay, az, color, 0, 0, nx, ny, nz);
        quadVertex(vc, matrix, bx, by, bz, color, 0, 1, nx, ny, nz);
        quadVertex(vc, matrix, cx, cy, cz, color, 1, 1, nx, ny, nz);
        quadVertex(vc, matrix, dx, dy, dz, color, 1, 0, nx, ny, nz);

        quadVertex(vc, matrix, dx, dy, dz, color, 1, 0, -nx, -ny, -nz);
        quadVertex(vc, matrix, cx, cy, cz, color, 1, 1, -nx, -ny, -nz);
        quadVertex(vc, matrix, bx, by, bz, color, 0, 1, -nx, -ny, -nz);
        quadVertex(vc, matrix, ax, ay, az, color, 0, 0, -nx, -ny, -nz);
    }
}
