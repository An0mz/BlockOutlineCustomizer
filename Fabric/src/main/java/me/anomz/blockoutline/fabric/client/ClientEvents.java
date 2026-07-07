package me.anomz.blockoutline.fabric.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class ClientEvents {
    public static void register() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(OutlineRenderer::beforeBlockOutline);
        WorldRenderEvents.BLOCK_OUTLINE.register(OutlineRenderer::onRenderBlockOutline);
    }
}