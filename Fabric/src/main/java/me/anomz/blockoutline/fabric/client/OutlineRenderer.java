package me.anomz.blockoutline.fabric.client;

import me.anomz.blockoutline.client.render.OutlineRenderCore;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OutlineRenderer {

    /** BLOCK_OUTLINE hook; returning false cancels the vanilla outline. */
    public static boolean onRenderBlockOutline(WorldRenderContext context, WorldRenderContext.BlockOutlineContext outlineContext) {
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
