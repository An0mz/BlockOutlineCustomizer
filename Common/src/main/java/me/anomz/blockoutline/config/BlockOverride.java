package me.anomz.blockoutline.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies a different style when the targeted block matches any entry in
 * {@link #blocks}. Entries are block ids ("minecraft:chest") or block tags
 * prefixed with '#' ("#c:ores"). Edited via the config JSON.
 */
public class BlockOverride {
    public String name = "";
    public boolean enabled = true;
    public List<String> blocks = new ArrayList<>();
    public StyleSettings style = new StyleSettings();

    public void sanitize() {
        if (name == null) name = "";
        if (blocks == null) blocks = new ArrayList<>();
        blocks.removeIf(e -> e == null || e.isBlank());
        if (style == null) style = new StyleSettings();
        style.sanitize();
    }
}
