package me.anomz.blockoutline;

import me.anomz.blockoutline.client.KeyBindings;
import me.anomz.blockoutline.config.OutlineConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("blockoutlinecustomizer")
public class BlockOutlineCustomizer {
    public static final String MOD_ID = "blockoutlinecustomizer";
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockOutlineCustomizer.class);

    public BlockOutlineCustomizer(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Block Outline Customizer initializing...");

        modContainer.registerConfig(ModConfig.Type.CLIENT, OutlineConfig.SPEC);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::clientSetup);

        LOGGER.info("Block Outline Customizer initialized!");
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_CONFIG_KEY);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    me.anomz.blockoutline.client.ClientEvents::onClientTick
            );
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    me.anomz.blockoutline.client.OutlineRenderer::onRenderLevelStage
            );
        });
    }
}