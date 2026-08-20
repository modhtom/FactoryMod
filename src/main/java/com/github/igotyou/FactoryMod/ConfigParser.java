package com.github.igotyou.FactoryMod;

import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.recipes.CompactingRecipe;
import com.github.igotyou.FactoryMod.recipes.DecompactingRecipe;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.*;

import com.github.igotyou.FactoryMod.utility.ConfigurationSection;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Loads FactoryMod's server configuration from Fabric's config directory. */
public final class ConfigParser {
    private final FactoryMod plugin;
    public ConfigParser(FactoryMod plugin) { this.plugin = plugin; }

    public FactoryModManager parse() {
        ConfigurationSection config = load();
        Item interactionItem = item(config.getString("factory_interaction_material", "STICK"));
        FactoryModManager manager = new FactoryModManager(plugin, interactionItem, false, false,
            config.getInt("redstone_power_on", 7), config.getInt("redstone_recipe_change", 2),
            config.getBoolean("log_inventories", false), config.getInt("max_input_chests", 10),
            config.getInt("max_output_chests", 10), config.getInt("max_fuel_chests", 10),
            config.getInt("max_iof_chests", 15), Map.of(), false);

        Map<String, IRecipe> recipes = parseRecipes(config.getConfigurationSection("recipes"), manager);
        parseFactories(config.getConfigurationSection("factories"), recipes, manager);
        plugin.info("Loaded " + recipes.size() + " recipes and configured factory definitions from config/factorymod.yml");
        return manager;
    }

    private ConfigurationSection load() {
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve("factorymod.yml");
            if (Files.notExists(path)) {
                try (InputStream defaultConfig = FactoryMod.class.getResourceAsStream("/config.yml")) {
                    if (defaultConfig == null) throw new IllegalStateException("Bundled config.yml is missing");
                    Files.copy(defaultConfig, path);
                }
            }
            try (InputStream stream = Files.newInputStream(path)) {
                Object loaded = new Yaml().load(stream);
                if (loaded instanceof Map<?, ?> map) {
                    @SuppressWarnings("unchecked") Map<String, Object> root = (Map<String, Object>) map;
                    return new ConfigurationSection(root, "", "");
                }
            }
        } catch (Exception exception) {
            plugin.severe("Could not load config/factorymod.yml: " + exception.getMessage());
        }
        return new ConfigurationSection(new HashMap<>(), "", "");
    }

    private Map<String, IRecipe> parseRecipes(ConfigurationSection section, FactoryModManager manager) {
        Map<String, IRecipe> recipes = new HashMap<>();
        if (section == null) return recipes;
        
        // UPGRADE recipes need the factory, which we link after parsing factories.
        // For now, we will just parse them without the factory egg.
        
        for (String id : section.getKeys(false)) {
            ConfigurationSection recipe = section.getConfigurationSection(id);
            if (recipe == null) continue;
            String type = recipe.getString("type", "").toUpperCase(Locale.ROOT);
            String name = recipe.getString("name", id);
            int productionTime = ticks(recipe.getString("production_time", "1s"));
            IRecipe parsed = switch (type) {
                case "PRODUCTION" -> new ProductionRecipe(id, name, productionTime,
                    itemMap(recipe.getConfigurationSection("input")), itemMap(recipe.getConfigurationSection("output")));
                case "COMPACT" -> new CompactingRecipe(id, itemMap(recipe.getConfigurationSection("input")),
                    recipe.getStringList("excluded_materials").stream().map(this::item).toList(), name, productionTime,
                    recipe.getString("compact_lore", "Compacted"));
                case "DECOMPACT" -> new DecompactingRecipe(id, itemMap(recipe.getConfigurationSection("input")), name,
                    productionTime, recipe.getString("compact_lore", "Compacted"));
                case "REPAIR" -> new RepairRecipe(id, name, productionTime,
                    itemMap(recipe.getConfigurationSection("input")), recipe.getInt("health_gained", 10000));
                case "UPGRADE" -> new Upgraderecipe(id, name, productionTime,
                    itemMap(recipe.getConfigurationSection("input")), null); // Egg will be resolved later if needed
                default -> null;
            };
            if (parsed == null) {
                plugin.warning("Skipping unsupported recipe type " + type + " at recipes." + id);
                continue;
            }
            recipes.put(id, parsed);
            manager.registerRecipe(parsed);
        }
        return recipes;
    }

    private void parseFactories(ConfigurationSection section, Map<String, IRecipe> recipes, FactoryModManager manager) {
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            ConfigurationSection factory = section.getConfigurationSection(id);
            if (factory == null) continue;
            String type = factory.getString("type");
            if (!"FCC".equalsIgnoreCase(type) && !"FCCUPGRADE".equalsIgnoreCase(type)) {
                plugin.warning("Skipping unsupported factory type at factories." + id);
                continue;
            }
            List<IRecipe> assigned = new ArrayList<>();
            for (String recipeId : factory.getStringList("recipes")) {
                IRecipe recipe = recipes.get(recipeId);
                if (recipe != null) assigned.add(recipe);
                else plugin.warning("Factory " + id + " references unknown recipe " + recipeId);
            }
            
            String factoryName = factory.getString("name", id);
            int updateTime = ticks(factory.getString("updatetime", "1s"));
            int fuelIntervall = ticks(factory.getString("fuel_consumption_intervall", "10s"));
            ItemMap fuel = itemMap(factory.getConfigurationSection("fuel"));
            if (fuel.getItemStackRepresentation().isEmpty()) {
                // Try default fuel
                fuel = itemMap(section.getConfigurationSection("default_fuel"));
            }
            ItemMap setupCost = itemMap(factory.getConfigurationSection("setupcost"));
            if (fuel.getItemStackRepresentation().isEmpty()) {
                fuel.addItemStack(new ItemStack(Items.CHARCOAL, 1));
            }
            FurnCraftChestEgg egg = new FurnCraftChestEgg(factoryName, updateTime, assigned, fuel, fuelIntervall, 1.0);

            manager.addFactoryEgg(com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure.class, setupCost, egg);
            manager.registerFccDefinition(id, factoryName, updateTime, setupCost, assigned, fuel, fuelIntervall);
        }
    }

    private ItemMap itemMap(ConfigurationSection section) {
        ItemMap map = new ItemMap();
        if (section == null) return map;
        for (String id : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(id);
            if (entry == null) continue;
            
            String materialName = entry.getString("type");
            if (materialName == null || materialName.isEmpty()) {
                materialName = entry.getString("material");
            }
            if (materialName == null) continue;
            
            Item parsed = item(materialName);
            if (parsed != Items.AIR) {
                int amount = Math.max(1, entry.getInt("amount", 1));
                ItemStack stack = new ItemStack(parsed, amount);
                
                ConfigurationSection meta = entry.getConfigurationSection("meta");
                if (meta != null) {
                    String displayName = meta.getString("display-name");
                    if (displayName != null) {
                        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal(displayName));
                    }
                }
                map.addItemStack(stack);
            }
        }
        return map;
    }

    private Item item(String id) {
        if (id == null || id.isBlank()) return Items.AIR;
        Identifier key = Identifier.parse(id.contains(":") ? id.toLowerCase(Locale.ROOT) : "minecraft:" + id.toLowerCase(Locale.ROOT));
        return BuiltInRegistries.ITEM.getValue(key);
    }

    private static int ticks(String value) {
        if (value == null || value.isBlank()) return 20;
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        try {
            if (normalized.endsWith("ms")) return Math.max(1, Integer.parseInt(normalized.substring(0, normalized.length() - 2)) / 50);
            if (normalized.endsWith("s")) return Math.max(1, Integer.parseInt(normalized.substring(0, normalized.length() - 1)) * 20);
            if (normalized.endsWith("m")) return Math.max(1, Integer.parseInt(normalized.substring(0, normalized.length() - 1)) * 1200);
            return Math.max(1, Integer.parseInt(normalized));
        } catch (NumberFormatException ignored) { return 20; }
    }
}
