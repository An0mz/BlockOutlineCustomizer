package me.anomz.blockoutline.forge.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class OutlineDisabler {

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        event.setCanceled(true);
    }
}
