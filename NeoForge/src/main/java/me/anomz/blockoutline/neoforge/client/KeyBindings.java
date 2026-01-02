package me.anomz.blockoutline.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.anomz.blockoutline.Constants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final String KEY_CATEGORY = "key.categories." + Constants.MOD_ID;

    public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            "key." + Constants.MOD_ID + ".openconfig",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            KeyMapping.Category.register(ResourceLocation.parse(KEY_CATEGORY))
    );
}