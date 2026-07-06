package me.anomz.blockoutline.neoforge.client;

import me.anomz.blockoutline.Constants;
import me.anomz.blockoutline.neoforge.client.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    private static final OutlineRenderer OUTLINE_RENDERER = new OutlineRenderer();

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (KeyBindings.OPEN_CONFIG_KEY.consumeClick()) {
            minecraft.gui.setScreen(new ConfigScreen(minecraft.gui.screen()));
        }
    }

    @SubscribeEvent
    public static void onExtractBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
        event.addCustomRenderer(OUTLINE_RENDERER);
    }
}
