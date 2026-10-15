package com.github.igotyou.FactoryMod.utility;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Handles saving and loading of factory data to/from disk.
 *
 * Save file format (JSON):
 * [
 *   {
 *     "type": "FurnCraftChestFactory",
 *     "name": "Iron Tool Smith",
 *     "craftingTable": {"x": 0, "y": 64, "z": 0},
 *     "furnace":       {"x": 1, "y": 64, "z": 0},
 *     "chest":         {"x":-1, "y": 64, "z": 0},
 *     "currentRecipeId": "make_iron_pick",
 *     "health": 9800,
 *     "maxHealth": 10000
 *   },
 *   ...
 * ]
 */
public class FileHandler {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String SAVE_FILE_NAME = "factorymod_factories.json";

    private final FactoryModManager manager;
    private final File saveFile;

    public FileHandler(FactoryModManager manager, Map<?, ?> ignoredRenames) {
        this.manager = manager;
        // Store in the world's config directory, or fallback to working directory
        this.saveFile = new File("config", SAVE_FILE_NAME);
    }

    /** Called on server stop — serialises every factory to JSON. */
    public void saveAll() {
        JsonArray root = new JsonArray();

        for (Factory factory : manager.getAllFactories()) {
            if (!(factory instanceof FurnCraftChestFactory fcc)) continue;
            if (!(fcc.getMultiBlockStructure() instanceof FurnCraftChestStructure fccs)) continue;

            JsonObject obj = new JsonObject();
            obj.addProperty("type", "FurnCraftChestFactory");
            obj.addProperty("name", fcc.getName());

            obj.add("craftingTable", blockPosToJson(fccs.getCraftingTable()));
            obj.add("furnace",       blockPosToJson(fccs.getFurnace()));
            obj.add("chest",         blockPosToJson(fccs.getChest()));

            IRecipe current = fcc.getCurrentRecipe();
            if (current != null) {
                obj.addProperty("currentRecipeId", current.getIdentifier());
            }

            obj.addProperty("health",    fcc.getHealth());
            obj.addProperty("maxHealth", fcc.getMaxHealth());

            root.add(obj);
        }

        // Ensure the config directory exists
        saveFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(saveFile)) {
            GSON.toJson(root, writer);
            FactoryMod.getLogger().info("[FactoryMod] Saved {} factories to {}", root.size(), saveFile.getAbsolutePath());
        } catch (IOException e) {
            FactoryMod.getLogger().error("[FactoryMod] Failed to save factories: {}", e.getMessage());
        }
    }

    /** Called after config is parsed — reads the JSON file and recreates factories. */
    public void loadAll(MinecraftServer server) {
        if (!saveFile.exists()) {
            FactoryMod.getLogger().info("[FactoryMod] No factory save file found, starting fresh.");
            return;
        }

        int loaded = 0;
        int failed = 0;

        try (FileReader reader = new FileReader(saveFile)) {
            JsonArray root = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement element : root) {
                JsonObject obj = element.getAsJsonObject();
                String type = obj.has("type") ? obj.get("type").getAsString() : "";

                if (!"FurnCraftChestFactory".equals(type)) {
                    FactoryMod.getLogger().warn("[FactoryMod] Unknown factory type '{}', skipping.", type);
                    failed++;
                    continue;
                }

                try {
                    BlockPos craftingTable = jsonToBlockPos(obj.getAsJsonObject("craftingTable"));
                    BlockPos furnace       = jsonToBlockPos(obj.getAsJsonObject("furnace"));
                    BlockPos chest         = jsonToBlockPos(obj.getAsJsonObject("chest"));

                    // Use the overworld by default — you can extend this to save the dimension key
                    net.minecraft.server.level.ServerLevel world = server.overworld();

                    // Force-load the chunk so block queries work even in non-spawn chunks
                    world.getChunk(craftingTable);

                    FurnCraftChestStructure structure = new FurnCraftChestStructure(world,
                            List.of(craftingTable, furnace, chest));

                    if (!structure.isComplete()) {
                        FactoryMod.getLogger().warn(
                                "[FactoryMod] Factory '{}' at {} is no longer valid (blocks missing/changed), skipping.",
                                obj.has("name") ? obj.get("name").getAsString() : "?", craftingTable);
                        failed++;
                        continue;
                    }

                    // Find the matching definition by name so we get the right recipes & timings
                    String savedName = obj.has("name") ? obj.get("name").getAsString() : "";
                    FactoryModManager.FccDefinition def = findDefinitionByName(savedName);
                    if (def == null) {
                        FactoryMod.getLogger().warn(
                                "[FactoryMod] No definition found for factory '{}', skipping.", savedName);
                        failed++;
                        continue;
                    }

                    FurnCraftChestFactory factory = new FurnCraftChestFactory(
                            structure,
                            def.updateTime(),
                            def.name(),
                            def.recipes(),
                            def.fuel(),
                            def.fuelConsumptionInterval());

                    // Restore health
                    int maxHealth = obj.has("maxHealth") ? obj.get("maxHealth").getAsInt() : factory.getMaxHealth();
                    int health    = obj.has("health")    ? obj.get("health").getAsInt()    : maxHealth;
                    factory.setMaxHealth(maxHealth);
                    factory.setHealth(health);

                    // Restore selected recipe
                    if (obj.has("currentRecipeId")) {
                        String recipeId = obj.get("currentRecipeId").getAsString();
                        IRecipe recipe = manager.getRecipe(recipeId);
                        if (recipe != null) {
                            factory.setRecipe(recipe);
                        }
                    }

                    manager.addFactory(factory);
                    loaded++;

                } catch (Exception e) {
                    FactoryMod.getLogger().error("[FactoryMod] Error loading a factory entry: {}", e.getMessage());
                    failed++;
                }
            }

        } catch (IOException e) {
            FactoryMod.getLogger().error("[FactoryMod] Failed to read factory save file: {}", e.getMessage());
            return;
        }

        FactoryMod.getLogger().info("[FactoryMod] Loaded {} factories ({} failed) from {}",
                loaded, failed, saveFile.getAbsolutePath());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static JsonObject blockPosToJson(BlockPos pos) {
        JsonObject o = new JsonObject();
        o.addProperty("x", pos.getX());
        o.addProperty("y", pos.getY());
        o.addProperty("z", pos.getZ());
        return o;
    }

    private static BlockPos jsonToBlockPos(JsonObject o) {
        return new BlockPos(o.get("x").getAsInt(), o.get("y").getAsInt(), o.get("z").getAsInt());
    }

    private FactoryModManager.FccDefinition findDefinitionByName(String name) {
        String cleanName = name.replaceAll("(?i)[§&][0-9A-FK-ORX]", "").trim();
        for (FactoryModManager.FccDefinition def : manager.getFccDefinitions().values()) {
            String cleanDef = def.name().replaceAll("(?i)[§&][0-9A-FK-ORX]", "").trim();
            if (cleanDef.equalsIgnoreCase(cleanName)) return def;
        }
        return null;
    }

    // ─── Legacy stubs (kept so nothing else breaks) ───────────────────────────
    public void save(Object o) {}
    public void load(Object o) {}
}
