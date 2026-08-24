# FactoryMod v3.1.1 (Fabric Port)

> This is a vibe coded project built with passion, and a love for MINECRAFT.

FactoryMod is a Minecraft Fabric mod (ported from the classic Bukkit/Civcraft plugin) that introduces specialized, multi-block crafting machines that consume and produce items based on highly customizable configurations.

## Requirements & Dependencies

- **Minecraft Version:** 26.2
- **Fabric Loader:** >= 0.15.11
- **Fabric API**
- **Java:** 21 or newer

## Features & Highlights

- **Full Fabric 1.21.11 Port:** Completely independent of Spigot/Bukkit.
- **Interactive Paginated GUI (`/fm`):**
  - Type `/fm` or hit a Crafting Table with a Stick to browse configured factories.
  - Displays full Setup Costs (materials required to construct the factory).
  - Click on any factory to view all of its recipes with exact Inputs, Outputs, and Production Times.
- **Pre-configured EdenMC Tech Tree:** Includes dozens of production recipes and machines ready out-of-the-box.
- **Custom Economy Balancing:** Recipes updated with Netherite Ingot and Gold Block requirements.
- **Autonomous Ticking:** Dedicated task scheduler and fuel consumption (Charcoal) system.

## Installation

1. Place `FactoryMod-3.1.0.jar` into your server's `mods/` directory.
2. Start the server once to generate `config/factorymod.yml`.
3. *(Optional)* Customize recipes, setup costs, and production times in `config/factorymod.yml`.

## In-Game Quick Start

1. Place a Furnace, Crafting Table, and Chest directly adjacent to each other.
2. Place the required setup cost materials inside the Chest.
3. Left-click the Crafting Table with a Stick to create the factory.
4. Left-click the Crafting Table with a Stick again to browse and select a recipe.
5. Place Charcoal into the Furnace and recipe materials into the Chest.
6. Left-click the Furnace with a Stick to activate the factory!

---
*Based on the original FactoryMod plugin by the Civcraft/Civclassics community.*
