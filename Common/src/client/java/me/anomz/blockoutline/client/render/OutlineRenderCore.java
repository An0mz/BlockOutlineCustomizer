package me.anomz.blockoutline.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.anomz.blockoutline.client.OverrideResolver;
import me.anomz.blockoutline.client.PreviewState;
import me.anomz.blockoutline.config.BOCConfig;
import me.anomz.blockoutline.config.OutlineStyle;
import me.anomz.blockoutline.config.StyleSettings;
import me.anomz.blockoutline.util.Animations;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loader-independent outline + fill rendering for Minecraft 1.21.1 (buffer
 * based). The Fabric WorldRenderEvents hook and the NeoForge
 * RenderHighlightEvent handler both delegate here.
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

    private static final net.minecraft.resources.ResourceLocation WHITE_TEXTURE =
            new net.minecraft.resources.ResourceLocation(me.anomz.blockoutline.Constants.MOD_ID, "textures/white.png");
    /** Packed fullbright lightmap coords (block 15 / sky 15). */
    private static final int FULL_BRIGHT = 0xF000F0;

    /**
     * 1.21.1 has no per-vertex line width, and RenderType.lines() forces a
     * window-scaled default width in its setup. Wrap it in render types that
     * set our width after the vanilla setup runs (one cached instance per
     * quantized width step so batching still works).
     */
    private static final Map<Float, RenderType> LINE_TYPES = new ConcurrentHashMap<>();

    private OutlineRenderCore() {
    }

    private static RenderType linesWidth(float width, boolean seeThrough) {
        float quantized = Math.round(width * 4.0f) / 4.0f;
        float key = seeThrough ? -quantized : quantized;
        return LINE_TYPES.computeIfAbsent(key, k -> new RenderType(
                "boc_lines_" + quantized + (seeThrough ? "_seethrough" : ""),
                DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES,
                1536, false, false,
                () -> {
                    RenderType.lines().setupRenderState();
                    RenderSystem.lineWidth(quantized);
                    if (seeThrough) {
                        RenderSystem.disableDepthTest();
                    }
                },
                () -> {
                    if (seeThrough) {
                        RenderSystem.enableDepthTest();
                    }
                    RenderType.lines().clearRenderState();
                }) {
        });
    }

    /** Entity-translucent-emissive quads, but with the depth test disabled. */
    private static final RenderType QUADS_SEE_THROUGH = new RenderType(
            "boc_quads_seethrough", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
            1536, false, true,
            () -> {
                RenderType.entityTranslucentEmissive(WHITE_TEXTURE).setupRenderState();
                RenderSystem.disableDepthTest();
            },
            () -> {
                RenderSystem.enableDepthTest();
                RenderType.entityTranslucentEmissive(WHITE_TEXTURE).clearRenderState();
            }) {
    };

    /**
     * Render type for our solid-color quads (fill + shader-fix outline).
     * Entity-translucent-emissive geometry is processed by shader packs,
     * unlike the debug render types which Iris drops when a pack is active.
     * Known limitation: some packs render entity translucency without alpha
     * blending, making the opacity sliders act as on/off under those packs.
     */
    private static RenderType quadRenderType(boolean seeThrough) {
        return seeThrough ? QUADS_SEE_THROUGH : RenderType.entityTranslucentEmissive(WHITE_TEXTURE);
    }

    private static void quadVertex(VertexConsumer vc, Matrix4f matrix,
                                   float x, float y, float z, int color, float u, float v,
                                   float nx, float ny, float nz) {
        vc.vertex(matrix, x, y, z)
                .color(color)
                .uv(u, v)
                .overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .uv2(FULL_BRIGHT)
                .normal(nx, ny, nz)
                .endVertex();
    }

    /** Emits the from/to distances of the sub-segments a styled edge is split into. */
    public interface SegmentConsumer {
        void accept(float from, float to);
    }

    /** Whether the custom outline should replace vanilla's right now (honors the live GUI preview). */
    public static boolean customOutlineActive() {
        return PreviewState.style != null ? PreviewState.customEnabled : BOCConfig.get().customOutlineEnabled;
    }

    /** Whether force mode is on right now (honors the live GUI preview). */
    public static boolean forceOutlineActive() {
        return PreviewState.style != null ? PreviewState.forceOutline : BOCConfig.get().forceOutline;
    }

    public static void submit(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockPos pos, VoxelShape shape, Vec3 cameraPos) {
        BOCConfig config = BOCConfig.get();
        StyleSettings preview = PreviewState.style;
        StyleSettings style = preview != null ? preview : OverrideResolver.resolve(config, pos);

        // While the config screen is open, its unsaved toggles win
        boolean seeThrough = preview != null ? PreviewState.seeThrough : config.seeThrough;
        boolean connected = preview != null ? PreviewState.connectedBlocks : config.connectedBlocks;
        boolean cube = preview != null ? PreviewState.cubeOutline : config.cubeOutline;
        boolean blockInfo = preview != null ? PreviewState.blockInfo : config.blockInfo;

        net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
        net.minecraft.world.level.block.state.BlockState state = level != null ? level.getBlockState(pos) : null;
        if (state != null && (cube || connected)) {
            shape = ConnectedBlocks.shapeFor(level, pos, state, shape, connected, cube);
        }

        poseStack.pushPose();
        poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
        PoseStack.Pose pose = poseStack.last();

        double pulse = pulseFactor(style);

        if (style.fillEnabled) {
            renderFill(bufferSource, shape, pose.pose(), fillColor(style, pulse), seeThrough);
        }
        renderOutline(bufferSource, shape, pose, style, pulse, seeThrough,
                (float) (pos.getX() - cameraPos.x), (float) (pos.getY() - cameraPos.y), (float) (pos.getZ() - cameraPos.z));

        if (blockInfo && state != null) {
            BlockInfoOverlay.render(poseStack, bufferSource, pos, state);
        }

        poseStack.popPose();
    }

    public static double pulseFactor(StyleSettings style) {
        if (!style.pulseEnabled) {
            return 1.0;
        }
        return style.pulseBlink
                ? Animations.blink(style.pulseSpeed, style.pulseMinOpacity)
                : Animations.pulse(style.pulseSpeed, style.pulseMinOpacity);
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

    private static void renderOutline(MultiBufferSource.BufferSource bufferSource, VoxelShape shape, PoseStack.Pose pose, StyleSettings style, double pulse,
                                      boolean seeThrough, float relX, float relY, float relZ) {
        int alpha = (int) Math.round(style.outlineOpacity * pulse * 255.0);
        if (alpha <= 0) {
            return;
        }
        float width = (float) style.outlineWidth;
        float phase = dashPhase(style);
        boolean gradient = style.outlineRgbEnabled && style.outlineRgbGradient;
        double baseT = Animations.rainbowHue(style.outlineRgbSpeed);
        int alphaBits = alpha << 24;
        int uniformColor = alphaBits | (outlineColor(style, pulse) & 0xFFFFFF);

        if (style.outlineQuadWidth) {
            renderQuadOutline(bufferSource, shape, pose.pose(), style, gradient, baseT, phase, alphaBits, uniformColor, width, seeThrough, relX, relY, relZ);
            return;
        }

        RenderType lineType = linesWidth(width, seeThrough);
        VertexConsumer vc = bufferSource.getBuffer(lineType);

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
                        addLine(vc, pose, x1, y1, z1, nx, ny, nz, f0, f1, c0, c1);
                    }
                } else {
                    addLine(vc, pose, x1, y1, z1, nx, ny, nz, from, to, uniformColor, uniformColor);
                }
            });
        });

        bufferSource.endBatch(lineType);
    }

    /**
     * Shader-fix width mode: draws each outline segment as a camera-facing quad
     * ribbon through the entity pipeline, so the width works even when a
     * resource/shader pack overrides the vanilla line shader. The ribbon width
     * scales with distance to stay roughly constant on screen.
     */
    private static void renderQuadOutline(MultiBufferSource.BufferSource bufferSource, VoxelShape shape, Matrix4f matrix, StyleSettings style,
                                          boolean gradient, double baseT, float phase, int alphaBits, int uniformColor, float width,
                                          boolean seeThrough, float relX, float relY, float relZ) {
        VertexConsumer vc = bufferSource.getBuffer(quadRenderType(seeThrough));
        int screenHeight = Math.max(1, net.minecraft.client.Minecraft.getInstance().getWindow().getHeight());
        // World units per requested pixel per block of distance, assuming ~70 degree FOV.
        float widthFactor = width * 1.4f / screenHeight;

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

        bufferSource.endBatch(quadRenderType(seeThrough));
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
                                float from, float to, int colorFrom, int colorTo) {
        // The lines shader derives the screen-space width from Position + Normal,
        // both in view space: the direction must go through the pose's normal
        // matrix (the pose carries the camera rotation here), or the line
        // collapses at certain view angles.
        vc.vertex(pose.pose(), x + nx * from, y + ny * from, z + nz * from)
                .color(colorFrom)
                .normal(pose.normal(), nx, ny, nz)
                .endVertex();
        vc.vertex(pose.pose(), x + nx * to, y + ny * to, z + nz * to)
                .color(colorTo)
                .normal(pose.normal(), nx, ny, nz)
                .endVertex();
    }

    private static void renderFill(MultiBufferSource.BufferSource bufferSource, VoxelShape shape, Matrix4f matrix, int color, boolean seeThrough) {
        if ((color >>> 24) == 0) {
            return;
        }
        final float offset = 0.001f;

        VertexConsumer vc = bufferSource.getBuffer(quadRenderType(seeThrough));
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

        bufferSource.endBatch(quadRenderType(seeThrough));
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
