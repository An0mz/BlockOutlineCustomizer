package me.anomz.blockoutline.client;

import me.anomz.blockoutline.Constants;
import me.anomz.blockoutline.config.BOCConfig;
import me.anomz.blockoutline.config.BlockOverride;
import me.anomz.blockoutline.config.StyleSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Resolves which {@link StyleSettings} applies to the targeted block,
 * honoring per-block/per-tag overrides from the config.
 */
public final class OverrideResolver {
    private OverrideResolver() {
    }

    private record Matcher(Predicate<BlockState> test, StyleSettings style) {
    }

    private static List<Matcher> matchers = List.of();
    private static int cachedGeneration = -1;
    private static BOCConfig cachedConfig;

    public static StyleSettings resolve(BOCConfig config, BlockPos pos) {
        if (config.blockOverrides.isEmpty()) {
            return config.style;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return config.style;
        }
        rebuildIfStale(config);
        if (matchers.isEmpty()) {
            return config.style;
        }
        BlockState state = level.getBlockState(pos);
        for (Matcher matcher : matchers) {
            if (matcher.test.test(state)) {
                return matcher.style;
            }
        }
        return config.style;
    }

    private static void rebuildIfStale(BOCConfig config) {
        if (config == cachedConfig && config.generation == cachedGeneration) {
            return;
        }
        List<Matcher> built = new ArrayList<>();
        for (BlockOverride override : config.blockOverrides) {
            if (!override.enabled || override.blocks.isEmpty()) {
                continue;
            }
            Predicate<BlockState> test = null;
            for (String entry : override.blocks) {
                Predicate<BlockState> parsed = parseEntry(entry);
                if (parsed != null) {
                    test = test == null ? parsed : test.or(parsed);
                }
            }
            if (test != null) {
                built.add(new Matcher(test, override.style));
            }
        }
        matchers = built;
        cachedConfig = config;
        cachedGeneration = config.generation;
    }

    private static Predicate<BlockState> parseEntry(String entry) {
        String trimmed = entry.trim();
        if (trimmed.startsWith("#")) {
            ResourceLocation id = ResourceLocation.tryParse(trimmed.substring(1));
            if (id == null) {
                Constants.LOGGER.warn("Invalid block tag in config override: {}", entry);
                return null;
            }
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, id);
            return state -> state.is(tag);
        }
        ResourceLocation id = ResourceLocation.tryParse(trimmed);
        if (id == null) {
            Constants.LOGGER.warn("Invalid block id in config override: {}", entry);
            return null;
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
        if (block == Blocks.AIR && !id.equals(BuiltInRegistries.BLOCK.getDefaultKey())) {
            Constants.LOGGER.warn("Unknown block in config override: {}", entry);
            return null;
        }
        return state -> state.getBlock() == block;
    }
}
