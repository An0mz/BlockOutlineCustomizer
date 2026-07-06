package me.anomz.blockoutline.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.anomz.blockoutline.client.render.OutlineRenderCore;
import me.anomz.blockoutline.config.BOCConfig;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "submitBlockOutline", at = @At("HEAD"), cancellable = true)
    private void onSubmitBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, CallbackInfo ci) {
        if (!BOCConfig.get().customOutlineEnabled) return;

        BlockOutlineRenderState outlineState = levelRenderState.blockOutlineRenderState;
        if (outlineState == null) return;

        VoxelShape shape = outlineState.shape();
        if (shape.isEmpty()) return;

        ci.cancel();
        OutlineRenderCore.submit(poseStack, submitNodeCollector, outlineState.pos(), shape, levelRenderState.cameraRenderState.pos);
    }
}
