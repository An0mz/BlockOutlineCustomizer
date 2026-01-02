package me.anomz.blockoutline.fabric.client;

import me.anomz.blockoutline.fabric.client.rendering.OutlineRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class ClientEvents {
    public static void register() {
        WorldRenderEvents.BLOCK_OUTLINE.register(OutlineRenderer::onRenderBlockOutline);
    }
}