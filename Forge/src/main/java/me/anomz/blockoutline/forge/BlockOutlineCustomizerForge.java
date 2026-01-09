package me.anomz.blockoutline.forge;

import me.anomz.blockoutline.Constants;
import me.anomz.blockoutline.forge.client.ClientEvents;
import me.anomz.blockoutline.forge.client.KeyBindings;
import me.anomz.blockoutline.forge.client.OutlineRenderer;
import me.anomz.blockoutline.forge.config.OutlineConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class BlockOutlineCustomizerForge {

    public BlockOutlineCustomizerForge() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, OutlineConfig.SPEC);

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(KeyBindings::register);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(OutlineRenderer.class);
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
    }
}
