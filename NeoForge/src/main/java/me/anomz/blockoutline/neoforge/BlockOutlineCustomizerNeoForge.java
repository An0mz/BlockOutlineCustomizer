package me.anomz.blockoutline.neoforge;

import me.anomz.blockoutline.neoforge.client.ClientEvents;
import me.anomz.blockoutline.neoforge.client.KeyBindings;
import me.anomz.blockoutline.neoforge.client.OutlineRenderer;
import me.anomz.blockoutline.neoforge.config.OutlineConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("blockoutlinecustomizer")
public class BlockOutlineCustomizerNeoForge {
    public static final String MOD_ID = "blockoutlinecustomizer";
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockOutlineCustomizerNeoForge.class);

    public BlockOutlineCustomizerNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Block Outline Customizer initializing...");

        modContainer.registerConfig(ModConfig.Type.CLIENT, OutlineConfig.SPEC);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::clientSetup);
        NeoForge.EVENT_BUS.addListener(OutlineRenderer::onRenderLevelStage);
        LOGGER.info("Block Outline Customizer initialized!");
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_CONFIG_KEY);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    ClientEvents::onClientTick
            );
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    OutlineRenderer::onRenderLevelStage
            );
        });
    }
}