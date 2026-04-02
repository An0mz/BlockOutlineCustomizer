package me.anomz.blockoutline.neoforge.client;

import me.anomz.blockoutline.neoforge.client.KeyBindings;
import me.anomz.blockoutline.neoforge.client.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

@EventBusSubscriber(modid = "blockoutlinecustomizer", value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (KeyBindings.OPEN_CONFIG_KEY.consumeClick()) {
            minecraft.setScreen(new ConfigScreen(minecraft.screen));
        }
    }

    @SubscribeEvent
    public static void cancelVanillaOutline(ExtractBlockOutlineRenderStateEvent event) {
        event.setCanceled(true);
    }
}