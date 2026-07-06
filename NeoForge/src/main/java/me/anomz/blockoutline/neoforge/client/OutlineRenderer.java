package me.anomz.blockoutline.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.anomz.blockoutline.client.render.OutlineRenderCore;
import me.anomz.blockoutline.config.BOCConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;

public class OutlineRenderer implements CustomBlockOutlineRenderer {

    @Override
    public boolean render(BlockOutlineRenderState renderState, SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState) {
        if (!BOCConfig.get().customOutlineEnabled) return false;

        VoxelShape shape = renderState.shape();
        if (shape.isEmpty()) return false;

        OutlineRenderCore.submit(poseStack, submitNodeCollector, renderState.pos(), shape, levelRenderState.cameraRenderState.pos);
        return true;
    }
}
