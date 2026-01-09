package me.anomz.blockoutline.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyMapping OPEN_CONFIG_KEY;

    public static void register(RegisterKeyMappingsEvent event) {
        OPEN_CONFIG_KEY = new KeyMapping(
                "key.blockoutlinecustomizer.config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "key.categories.misc"
        );

        event.register(OPEN_CONFIG_KEY);
    }
}