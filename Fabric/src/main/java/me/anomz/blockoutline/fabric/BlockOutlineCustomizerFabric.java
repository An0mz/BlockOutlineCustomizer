package me.anomz.blockoutline.fabric;

import me.anomz.blockoutline.fabric.client.ClientEvents;
import me.anomz.blockoutline.fabric.client.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockOutlineCustomizerFabric implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("blockoutlinecustomizer");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Block Outline Customizer (Fabric) initialized!");

        KeyBindings.register();

        ClientEvents.register();
    }
}