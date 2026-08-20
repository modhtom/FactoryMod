# FactoryMod (Fabric Port)

FactoryMod is a Minecraft Fabric mod (originally a Bukkit plugin) that introduces specialized, multi-block crafting machines which can consume and produce goods based on custom configurations.

This repository contains the **Fabric 1.21.11** port of the classic FactoryMod plugin.

## Features

- **Custom Factories**: Create configurable factories using a Furnace, Crafting Table, and Chest.
- **Tech Trees**: Create deep, complex tech trees that require significant investment and encourage cooperation.
- **Improved Economy**: Offer better yields for vanilla items, or create items that are otherwise unobtainable in survival.
- **Paginated GUI Menu**: Type `/fm` or hit a Crafting Table with a Stick to browse the available factories and their recipes in a user-friendly GUI.
- **Configurable**: Easily configure factory setup costs, recipes, fuel consumption, production times, and more in `config/factorymod.yml`.

## Installation

1. Drop the `.jar` file from the [Releases](#) tab into your server's `mods/` directory.
2. Start the server.
3. Open `config/factorymod.yml` to customize your factories and recipes. A default configuration (based on EdenMC) will be automatically generated.

## Usage

1. **Place the Blocks**: Place a Furnace, a Crafting Table, and a Chest next to each other.
2. **Setup Cost**: Place the required setup cost materials into the Chest.
3. **Create the Factory**: Hit the Crafting Table with a Stick.
4. **Choose a Recipe**: Hit the Crafting Table with a Stick again to browse and select a recipe.
5. **Run the Factory**: Place the required ingredients and fuel (e.g. Charcoal) into the chest/furnace, and hit the Furnace with a Stick to start production!

## Compiling

To build the project from source, clone this repository and run:

```bash
./gradlew build
```

The compiled JAR will be located in `build/libs/`.

---
*Based on the original FactoryMod plugin by the Civcraft/Civclassics community.*
