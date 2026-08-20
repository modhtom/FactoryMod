# FactoryMod (Fabric Port)

FactoryMod is a Minecraft mod featuring specialized crafting machines that can consume and produce goods efficiently. Originally an iconic Bukkit/Spigot plugin for servers like Civcraft, CivClassics, and EdenMC, this is a **Fabric 1.21.11** port of the mod!

FactoryMod's flexible configurability means admins can drastically improve upon a vanilla Minecraft economy by creating:
* Tech trees with precise control over depth and difficulty
* Incentives for cooperation through permanence of capital
* A way for players to obtain survival-unobtainable items

## Features
* **Fabric 1.21.11 Compatibility:** Fully ported to Fabric!
* **Multi-block Factories:** Create static factory structures by placing a furnace, crafting bench, and chest together.
* **EdenMC Recipes:** Comes pre-configured with a modified version of the EdenMC economy config.
* **SGUI Menus:** Use `/fm` or hit a Crafting Table with a stick to open a beautiful, paginated chest GUI. You can browse all available factories, check their setup costs, and explore inputs, outputs, and times for every recipe without looking up a wiki!
* **Fuel & Upkeep:** Factories require fuel (Charcoal by default) and have repair costs, working as an upkeep mechanism.

## Commands
* `/fm`, `/factory`, `/factories` - Opens the interactive GUI to view all available factories and recipes.

## How to use
1. **View Factories:** Run `/fm` to open the GUI and check the *Setup Cost* for the factory you want to build.
2. **Build the Structure:** Place a Furnace, a Crafting Table, and a Chest next to each other.
3. **Setup:** Place the required Setup Cost items into the chest.
4. **Initialize:** Hit the Crafting Table with a Stick.
5. **Craft:** Place fuel (e.g. Charcoal) in the Furnace, put the recipe inputs in the Chest, and hit the Crafting Table with a stick to select a recipe. Hit the Furnace with a stick to run the factory!

## Configuration
The `config/factorymod.yml` file is generated on first startup. You can customize the factory trees, setup costs, inputs, outputs, and update ticks here.

---
*Adapted from the original [igotyou/FactoryMod](https://github.com/igotyou/FactoryMod) plugin.*
