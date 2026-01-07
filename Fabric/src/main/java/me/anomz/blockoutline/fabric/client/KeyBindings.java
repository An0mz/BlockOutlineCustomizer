package me.anomz.blockoutline.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.anomz.blockoutline.Constants;
import me.anomz.blockoutline.fabric.client.gui.ConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static KeyMapping openConfigKey;

    public static void register() {
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + Constants.MOD_ID + ".openconfig",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyMapping.CATEGORY_MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ConfigScreen(null));  // Use new screen
                }
            }
        });
    }
}