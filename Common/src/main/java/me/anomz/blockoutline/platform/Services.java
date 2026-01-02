package me.anomz.blockoutline.platform;

import java.util.ServiceLoader;

/**
 * Service loader to get platform-specific implementations
 */
public class Services {
    private static ConfigHelper configHelper;

    public static ConfigHelper getConfigHelper() {
        if (configHelper == null) {
            configHelper = load(ConfigHelper.class);
        }
        return configHelper;
    }

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}