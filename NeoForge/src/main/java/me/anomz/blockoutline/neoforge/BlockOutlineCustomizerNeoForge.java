package me.anomz.blockoutline.neoforge;

import me.anomz.blockoutline.Constants;
import me.anomz.blockoutline.neoforge.client.KeyBindings;
import me.anomz.blockoutline.neoforge.config.OutlineConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class BlockOutlineCustomizerNeoForge {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockOutlineCustomizerNeoForge.class);

    public BlockOutlineCustomizerNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} initializing...", Constants.MOD_NAME);

        modContainer.registerConfig(ModConfig.Type.CLIENT, OutlineConfig.SPEC);
        modEventBus.addListener(this::registerKeyMappings);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_CONFIG_KEY);
    }
}