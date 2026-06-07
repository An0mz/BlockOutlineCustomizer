package me.anomz.blockoutline.neoforge.mixin;

import me.anomz.blockoutline.platform.Services;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderHitOutline", at = @At("HEAD"), cancellable = true)
    private void cancelVanillaOutline(CallbackInfo ci) {
        if (Services.getConfigHelper().isCustomOutlineEnabled()) {
            ci.cancel();
        }
    }
}