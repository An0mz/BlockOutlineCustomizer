package me.anomz.blockoutline.fabric.client;

import me.anomz.blockoutline.Constants;
import me.anomz.blockoutline.fabric.client.gui.ConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static KeyMapping openConfigKey;

    public static void register() {
        openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key." + Constants.MOD_ID + ".openconfig",
                GLFW.GLFW_KEY_U,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openConfigKey.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new ConfigScreen(null));
                }
            }
        });
    }
}