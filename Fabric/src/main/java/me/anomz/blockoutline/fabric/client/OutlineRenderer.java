package me.anomz.blockoutline.fabric.client;

import me.anomz.blockoutline.client.render.OutlineRenderCore;
import me.anomz.blockoutline.config.BOCConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OutlineRenderer {

    /** Set when force mode already drew the outline this frame. */
    private static boolean renderedEarly;

    /**
     * Force mode: BEFORE_BLOCK_OUTLINE runs before other mods (AE2 etc.) get a
     * chance to cancel the outline stage, so drawing here means ours always
     * shows. Always returns true so we never suppress other listeners.
     */
    public static boolean beforeBlockOutline(WorldRenderContext context, HitResult hitResult) {
        renderedEarly = false;
        if (!OutlineRenderCore.forceOutlineActive() || !OutlineRenderCore.customOutlineActive()) {
            return true;
        }
        if (!(hitResult instanceof BlockHitResult blockHit) || hitResult.getType() != HitResult.Type.BLOCK
                || context.world() == null) {
            return true;
        }

        BlockPos blockPos = blockHit.getBlockPos();
        VoxelShape shape = context.world().getBlockState(blockPos).getShape(context.world(), blockPos);
        renderedEarly = true;
        if (shape.isEmpty()) {
            return true;
        }

        MultiBufferSource.BufferSource bufferSource = context.consumers() instanceof MultiBufferSource.BufferSource bs
                ? bs
                : Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPos = context.camera().getPosition();
        OutlineRenderCore.submit(context.matrixStack(), bufferSource, blockPos, shape, cameraPos);
        return true;
    }

    /** BLOCK_OUTLINE hook; returning false cancels the vanilla outline. */
    public static boolean onRenderBlockOutline(WorldRenderContext context, WorldRenderContext.BlockOutlineContext outlineContext) {
        if (renderedEarly) {
            return false; // Force mode already drew it; just suppress vanilla
        }
        if (!OutlineRenderCore.customOutlineActive()) {
            return true; // Let vanilla render
        }

        BlockPos blockPos = outlineContext.blockPos();
        if (blockPos == null || context.world() == null) {
            return true;
        }
        VoxelShape shape = context.world().getBlockState(blockPos).getShape(context.world(), blockPos);
        if (shape.isEmpty()) {
            return false;
        }

        MultiBufferSource.BufferSource bufferSource = context.consumers() instanceof MultiBufferSource.BufferSource bs
                ? bs
                : Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPos = context.camera().getPosition();
        OutlineRenderCore.submit(context.matrixStack(), bufferSource, blockPos, shape, cameraPos);
        return false;
    }
}
