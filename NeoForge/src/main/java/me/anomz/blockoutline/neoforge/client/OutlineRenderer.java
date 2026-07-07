package me.anomz.blockoutline.neoforge.client;

import me.anomz.blockoutline.client.render.OutlineRenderCore;
import me.anomz.blockoutline.config.BOCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

public class OutlineRenderer {

    /**
     * Force mode: registered at HIGHEST priority so we run before mods that
     * draw their own highlight (AE2 cables etc.) and cancel first.
     */
    public static void onHighlightFirst(RenderHighlightEvent.Block event) {
        if (BOCConfig.get().forceOutline) {
            render(event);
        }
    }

    /**
     * Normal mode: registered at LOWEST priority without receiving canceled
     * events, so mods with their own highlight renderers keep theirs.
     */
    public static void onHighlightLast(RenderHighlightEvent.Block event) {
        if (!BOCConfig.get().forceOutline) {
            render(event);
        }
    }

    private static void render(RenderHighlightEvent.Block event) {
        if (!OutlineRenderCore.customOutlineActive()) {
            return; // Let vanilla render
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        BlockPos blockPos = event.getTarget().getBlockPos();
        VoxelShape shape = mc.level.getBlockState(blockPos).getShape(mc.level, blockPos);

        event.setCanceled(true);
        if (shape.isEmpty()) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = event.getMultiBufferSource() instanceof MultiBufferSource.BufferSource bs
                ? bs
                : mc.renderBuffers().bufferSource();
        Vec3 cameraPos = event.getCamera().getPosition();
        OutlineRenderCore.submit(event.getPoseStack(), bufferSource, blockPos, shape, cameraPos);
    }
}
