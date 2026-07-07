package me.anomz.blockoutline.forge.client;

import me.anomz.blockoutline.client.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        while (KeyBindings.OPEN_CONFIG_KEY.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new ConfigScreen(null));
            }
        }
    }
}
