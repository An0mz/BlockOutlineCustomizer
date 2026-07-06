# Block Outline Customizer

A client-side Minecraft mod that lets you fully customize the block selection outline.
Available for **Fabric** and **NeoForge**.

## Features

- **Custom colors** — RGB sliders for the outline and an optional translucent fill
- **Outline styles** — Full, Dashed, or Corner brackets
- **Width & opacity** — outline width from 1 to 10, fully adjustable opacity
- **Rainbow mode** — animated RGB cycling with adjustable speed, optionally synced between outline and fill
- **Pulse** — breathing opacity animation
- **Presets** — built-in looks (Classic, Neon, Subtle, Rainbow) plus a savable Custom slot
- **Per-block overrides** — give specific blocks or block tags their own style (e.g. highlight `#c:ores` in gold), editable in the config file
- **In-game config screen** — press **U** (rebindable), or open it from ModMenu (Fabric) / the mod list Config button (NeoForge)

## Configuration

Everything can be changed from the in-game screen. The config file is shared by both
loaders and lives at `config/blockoutlinecustomizer.json`.

### Per-block overrides

Overrides are edited in the config file. Each entry matches block ids or `#`-prefixed
block tags and applies its own complete style:

```json
"blockOverrides": [
  {
    "name": "Ores",
    "enabled": true,
    "blocks": ["#c:ores", "minecraft:ancient_debris"],
    "style": {
      "outlineRed": 255,
      "outlineGreen": 170,
      "outlineBlue": 0,
      "outlineWidth": 3.0
    }
  }
]
```

Unspecified style fields use their defaults. The first matching override wins.

## Building

```bash
./gradlew build
```

Jars are produced in `Fabric/build/libs` and `NeoForge/build/libs`. Requires Java 25
(Gradle downloads a toolchain automatically).

## License

[MIT](LICENSE)
