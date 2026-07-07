package me.anomz.blockoutline.neoforge;

import me.anomz.blockoutline.Constants;
import me.anomz.blockoutline.client.gui.ConfigScreen;
import me.anomz.blockoutline.neoforge.client.KeyBindings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class BlockOutlineCustomizerNeoForge {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockOutlineCustomizerNeoForge.class);

    public BlockOutlineCustomizerNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} initializing...", Constants.MOD_NAME);

        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new ConfigScreen(parent));
        modEventBus.addListener(this::registerKeyMappings);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_CONFIG_KEY);
    }
}
