package me.anomz.blockoutline.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.anomz.blockoutline.client.render.OutlineRenderCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;

public class OutlineRenderer implements CustomBlockOutlineRenderer {

    @Override
    public boolean render(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource buffer, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState) {
        if (!OutlineRenderCore.customOutlineActive()) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.hitResult == null) return false;
        if (mc.hitResult.getType() != HitResult.Type.BLOCK) return false;

        BlockHitResult blockHitResult = (BlockHitResult) mc.hitResult;
        BlockPos blockPos = blockHitResult.getBlockPos();
        VoxelShape shape = mc.level.getBlockState(blockPos).getShape(mc.level, blockPos);
        if (shape.isEmpty()) return false;

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();
        OutlineRenderCore.submit(poseStack, buffer, blockPos, shape, cameraPos);
        return true;
    }
}
