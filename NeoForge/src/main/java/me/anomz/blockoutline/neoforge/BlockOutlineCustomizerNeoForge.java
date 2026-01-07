package me.anomz.blockoutline.neoforge;

import me.anomz.blockoutline.Constants;
import me.anomz.blockoutline.neoforge.client.OutlineRenderer;
import me.anomz.blockoutline.neoforge.client.ClientEvents;
import me.anomz.blockoutline.neoforge.client.KeyBindings;
import me.anomz.blockoutline.neoforge.config.OutlineConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Constants.MOD_ID)
public class BlockOutlineCustomizerNeoForge {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockOutlineCustomizerNeoForge.class);

    public BlockOutlineCustomizerNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} initializing on NeoForge...", Constants.MOD_NAME);

        modContainer.registerConfig(ModConfig.Type.CLIENT, OutlineConfig.SPEC);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::clientSetup);

        LOGGER.info("{} initialized on NeoForge!", Constants.MOD_NAME);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_CONFIG_KEY);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ClientEvents::onClientTick);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(OutlineRenderer::onRenderBlockHighlight);
        });
    }
}