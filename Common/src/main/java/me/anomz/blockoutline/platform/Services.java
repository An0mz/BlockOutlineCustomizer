package me.anomz.blockoutline.platform;

import java.util.ServiceLoader;

/**
 * Service loader to get platform-specific implementations
 */
public class Services {
    public static final PlatformHelper PLATFORM = load(PlatformHelper.class);

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to load service for " + clazz.getName()));
    }
}
