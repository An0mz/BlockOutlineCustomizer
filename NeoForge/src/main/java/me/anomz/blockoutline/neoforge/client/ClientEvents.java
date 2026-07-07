package me.anomz.blockoutline.neoforge.client;

import me.anomz.blockoutline.client.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class ClientEvents {

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (KeyBindings.OPEN_CONFIG_KEY.consumeClick()) {
            minecraft.setScreen(new ConfigScreen(minecraft.screen));
        }
    }
}