package me.anomz.blockoutline.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final String KEY_CATEGORY = "key.categories.blockoutlinecustomizer";
    
    public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            "key.blockoutlinecustomizer.openconfig",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            KeyMapping.Category.register(Identifier.parse(KEY_CATEGORY))
    );
}
