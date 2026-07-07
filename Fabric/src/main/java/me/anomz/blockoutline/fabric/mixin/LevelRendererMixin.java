package me.anomz.blockoutline.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.anomz.blockoutline.client.render.OutlineRenderCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void onRenderBlockOutline(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState, CallbackInfo ci) {
        if (!OutlineRenderCore.customOutlineActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.hitResult == null) return;
        if (mc.hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHitResult = (BlockHitResult) mc.hitResult;
        BlockPos blockPos = blockHitResult.getBlockPos();
        VoxelShape shape = mc.level.getBlockState(blockPos).getShape(mc.level, blockPos);
        if (shape.isEmpty()) return;

        ci.cancel();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();
        OutlineRenderCore.submit(poseStack, bufferSource, blockPos, shape, cameraPos);
    }
}
