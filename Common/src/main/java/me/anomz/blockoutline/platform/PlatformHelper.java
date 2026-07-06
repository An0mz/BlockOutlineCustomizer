package me.anomz.blockoutline.platform;

import java.nio.file.Path;

/**
 * Platform-agnostic services, implemented by each loader.
 */
public interface PlatformHelper {
    Path getConfigDir();
}
