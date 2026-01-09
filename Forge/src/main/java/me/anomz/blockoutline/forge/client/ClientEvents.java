package me.anomz.blockoutline.forge.client;

import me.anomz.blockoutline.forge.client.gui.ConfigScreen;
import me.anomz.blockoutline.forge.platform.ForgeConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        while (KeyBindings.OPEN_CONFIG_KEY.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new ConfigScreen(null, new ForgeConfigHelper()));
            }
        }
    }
}