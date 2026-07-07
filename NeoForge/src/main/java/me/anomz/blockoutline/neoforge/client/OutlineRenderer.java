package me.anomz.blockoutline.neoforge.client;

import me.anomz.blockoutline.client.render.OutlineRenderCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

public class OutlineRenderer {

    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
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
