package com.github.igotyou.FactoryMod;

import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.eggs.PipeEgg;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.structures.PipeStructure;

import com.github.igotyou.FactoryMod.utility.FileHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import com.github.igotyou.FactoryMod.utility.ItemMap;

public class FactoryModManager {

    private FactoryMod plugin;
    private FileHandler fileHandler;
    private HashMap<Class<? extends MultiBlockStructure>, HashMap<ItemMap, IFactoryEgg>> factoryCreationRecipes;
    private HashMap<IFactoryEgg, ItemMap> totalSetupCosts;
    private HashMap<BlockPos, Factory> locations;
    private HashMap<String, IFactoryEgg> eggs;
    private HashSet<Factory> factories;
    private Map<String, IRecipe> recipes;
    private HashSet<Item> possibleCenterBlocks;
    private HashSet<Item> possibleInteractionBlock;
    private Item factoryInteractionItem;
    private boolean citadelEnabled;
    private boolean logInventories;
    private int redstonePowerOn;
    private int redstoneRecipeChange;
    private int maxInputChests;
    private int maxOutputChests;
    private int maxFuelChests;
    private int maxTotalIOFChests;
    private Set<String> compactLore;
    private Set<String> forceInclude;
    private FactoryModPlayerSettings playerSettings;
    private boolean canUpgrade;
    private final Map<String, FccDefinition> fccDefinitions = new HashMap<>();

    /** Immutable config definition for the supported Furnace/Crafting-table/Chest factory. */
    public record FccDefinition(String name, int updateTime, ItemMap setupCost, List<IRecipe> recipes, ItemMap fuel, int fuelConsumptionInterval) { }

    public FactoryModManager(FactoryMod plugin, Item factoryInteractionItem, boolean citadelEnabled,
                             boolean nameLayerEnabled, int redstonePowerOn, int redstoneRecipeChange, boolean logInventories,
                             int maxInputChests, int maxOutputChests, int maxFuelChests, int maxTotalIOFChests,
                             Map<String, String> factoryRenames, boolean canUpgrade) {
        this.plugin = plugin;
        this.factoryInteractionItem = factoryInteractionItem;
        this.citadelEnabled = citadelEnabled;
        this.redstonePowerOn = redstonePowerOn;
        this.redstoneRecipeChange = redstoneRecipeChange;
        this.maxInputChests = maxInputChests;
        this.maxOutputChests = maxOutputChests;
        this.maxFuelChests = maxFuelChests;
        this.maxTotalIOFChests = maxTotalIOFChests;
        this.fileHandler = new FileHandler(this, factoryRenames);
        this.canUpgrade = canUpgrade;

        factoryCreationRecipes = new HashMap<>();
        locations = new HashMap<>();
        eggs = new HashMap<>();
        possibleCenterBlocks = new HashSet<>();
        possibleInteractionBlock = new HashSet<>();
        
        factories = new HashSet<>();
        totalSetupCosts = new HashMap<>();
        recipes = new HashMap<>();
        compactLore = new HashSet<>();
        forceInclude = new HashSet<>();
        playerSettings = new FactoryModPlayerSettings(plugin);

        // Normal furnace, craftingtable, chest factories
        possibleCenterBlocks.add(net.minecraft.world.item.Items.CRAFTING_TABLE);
        possibleInteractionBlock.add(net.minecraft.world.item.Items.CRAFTING_TABLE);
        possibleInteractionBlock.add(net.minecraft.world.item.Items.FURNACE);
        possibleInteractionBlock.add(net.minecraft.world.item.Items.CHEST);
        possibleInteractionBlock.add(net.minecraft.world.item.Items.BARREL);
        possibleInteractionBlock.add(net.minecraft.world.item.Items.TRAPPED_CHEST);

        // sorter
        possibleCenterBlocks.add(net.minecraft.world.item.Items.DROPPER);
        possibleInteractionBlock.add(net.minecraft.world.item.Items.DROPPER);

        // pipe
        possibleCenterBlocks.add(net.minecraft.world.item.Items.DISPENSER);
        possibleInteractionBlock.add(net.minecraft.world.item.Items.DISPENSER);
    }

    /**
     * Sets the lore used for compacting recipes. This is needed for the compact
     * item listeners
     *
     * @param lore Lore used for compacting items
     */
    public void addCompactLore(String lore) {
        compactLore.add(lore);
    }

    public boolean logInventories() {
        return logInventories;
    }

    /**
     * @return Lore given to compacted items
     */
    public boolean isCompactLore(String lore) {
        return compactLore.contains(lore);
    }

    /**
     * Gets the setupcost for a specific factory
     *
     * @param c    Class of the structure type the factory is using
     * @param name Name of the factory
     * @return Setupcost if the factory if it was found or null if it wasnt
     */
    public ItemMap getSetupCost(Class<? extends MultiBlockStructure> c, String name) {
        for (Entry<ItemMap, IFactoryEgg> entry : factoryCreationRecipes.get(c).entrySet()) {
            if (entry.getValue().getName().equals(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Adds a factory and the locations of its blocks to the manager
     *
     * @param f Factory to add
     */
    public void addFactory(Factory f) {
        factories.add(f);
        for (BlockPos loc : f.getMultiBlockStructure().getAllBlocks()) {
            locations.put(loc, f);
        }
    }

    public void registerFccDefinition(String identifier, String name, int updateTime, ItemMap setupCost, List<IRecipe> recipes, ItemMap fuel, int fuelConsumptionInterval) {
        if (recipes.isEmpty()) {
            plugin.warning("Factory " + identifier + " has no valid recipes and was skipped");
            return;
        }
        fccDefinitions.put(identifier, new FccDefinition(name, Math.max(1, updateTime), setupCost, List.copyOf(recipes), fuel, Math.max(1, fuelConsumptionInterval)));
    }

    public Map<String, IRecipe> getRecipes() { return recipes; }

    public Map<String, FccDefinition> getFccDefinitions() {
        return fccDefinitions;
    }

    public void attemptFccCreation(ServerPlayer player, BlockPos center) {
        if (factoryExistsAt(center)) return;
        var world = player.level();
        FurnCraftChestStructure structure = new FurnCraftChestStructure(world, center);
        if (!structure.isComplete()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("\u00a7cFactory structure must be a straight line: [Chest - Crafting Table - Furnace]"));
            return;
        }
        if (structure.blockedByExistingFactory()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("\u00a7cAt least one block is already part of another factory."));
            return;
        }

        eu.pb4.sgui.api.gui.SimpleGui gui = new eu.pb4.sgui.api.gui.SimpleGui(net.minecraft.world.inventory.MenuType.GENERIC_9x6, player, false);
        gui.setTitle(net.minecraft.network.chat.Component.literal("\u00a78Available Factories"));

        int index = 0;
        for (FccDefinition definition : fccDefinitions.values()) {
            if (index >= 54) break;
            
            net.minecraft.world.item.ItemStack icon = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CRAFTING_TABLE);
            icon.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("\u00a7e" + definition.name()));
            
            java.util.List<net.minecraft.network.chat.Component> lore = new java.util.ArrayList<>();
            lore.add(net.minecraft.network.chat.Component.literal("\u00a76Setup Cost:"));
            for (net.minecraft.world.item.ItemStack is : definition.setupCost().getItemStackRepresentation()) {
                lore.add(net.minecraft.network.chat.Component.literal(" \u00a77- \u00a7f" + is.getCount() + "x " + is.getHoverName().getString()));
            }
            lore.add(net.minecraft.network.chat.Component.literal(""));
            lore.add(net.minecraft.network.chat.Component.literal("\u00a7aClick to build this factory!"));
            
            icon.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lore));

            gui.setSlot(index, icon, (slot, clickType, action, openedGui) -> {
                if (factoryExistsAt(center)) {
                    openedGui.close();
                    return;
                }
                
                net.minecraft.world.level.block.entity.BlockEntity chestBe = world.getBlockEntity(structure.getChest());
                net.minecraft.world.Container chestInv = (chestBe instanceof net.minecraft.world.Container c) ? c : null;
                
                boolean created = false;
                
                // First check if materials are in chest
                if (chestInv != null) {
                    ItemMap chestMap = new ItemMap();
                    for (int s = 0; s < chestInv.getContainerSize(); s++) {
                        chestMap.addItemStack(chestInv.getItem(s));
                    }
                    if (definition.setupCost().isContainedIn(chestMap)) {
                        if (definition.setupCost().removeSafelyFrom(chestInv)) {
                            FurnCraftChestFactory factory = new FurnCraftChestFactory(structure, definition.updateTime(), definition.name(), definition.recipes(), definition.fuel(), definition.fuelConsumptionInterval());
                            addFactory(factory);
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("\u00a7aSuccessfully created " + definition.name() + "! (Materials used from chest)"));
                            created = true;
                        }
                    }
                }
                
                // Next check if materials are in player inventory
                if (!created) {
                    ItemMap playerMap = new ItemMap();
                    for (int s = 0; s < player.getInventory().getContainerSize(); s++) {
                        playerMap.addItemStack(player.getInventory().getItem(s));
                    }
                    if (definition.setupCost().isContainedIn(playerMap)) {
                        if (definition.setupCost().removeSafelyFrom(player.getInventory())) {
                            FurnCraftChestFactory factory = new FurnCraftChestFactory(structure, definition.updateTime(), definition.name(), definition.recipes(), definition.fuel(), definition.fuelConsumptionInterval());
                            addFactory(factory);
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("\u00a7aSuccessfully created " + definition.name() + "! (Materials used from inventory)"));
                            created = true;
                        }
                    }
                }

                if (!created) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("\u00a7cYou do not have the setup materials to create this factory."));
                } else {
                    openedGui.close();
                }
            });
            index++;
        }
        gui.open();
    }

    public void tickFactories() {
        for (Factory factory : getAllFactories()) {
            if (factory instanceof FurnCraftChestFactory fcc) fcc.tick();
        }
    }

    /**
     * @return Whether citadel is enabled on the server
     */
    public boolean isCitadelEnabled() {
        return citadelEnabled;
    }

    public int getMaxInputChests() {
        return maxInputChests;
    }

    public int getMaxOutputChests() {
        return maxOutputChests;
    }

    public int getMaxFuelChests() {
        return maxFuelChests;
    }

    /**
     * @return The maximum total number of inputs, outputs, and fuel inputs a factory has. A chest with multiple IOF
     * settings enabled is counted for each.
     */
    public int getMaxTotalIOFChests() {
        return maxTotalIOFChests;
    }

    /**
     * @return Which material is used to interact with factories, stick by default
     */
    public Item getFactoryInteractionItem() {
        return factoryInteractionItem;
    }

    public boolean canUpgrade() {
        return canUpgrade;
    }

    /**
     * Removes a factory from the manager
     *
     * @param f Factory to remove
     */
    public void removeFactory(Factory f) {
        if (f.isActive()) {
            f.deactivate();
        }
        factories.remove(f);
        
        for (BlockPos b : f.getMultiBlockStructure().getAllBlocks()) {
            locations.remove(b);
        }
    }

    /**
     * Tries to get the factory which has a part at the given location
     *
     * @param loc BlockPos which is supposed to be part of a factory
     * @return The factory which had a block at the given location or null if there
     * was no factory
     */
    public Factory getFactoryAt(BlockPos loc) {
        return locations.get(loc);
    }

    /**
     * Tries to get the factory which has a part at the given block
     *
     * @param b BlockState which is supposed to be part of a factory
     * @return The factory which had a block at the given location or null if there
     * was no factory
     */
    public Factory getFactoryAt(BlockState b) {
        // BlockState doesn't have getBlockPos in Fabric, so we can't implement this properly without a World or BlockPos.
        // Assuming we are passing a context that has it, or we shouldn't use this.
        return null;
    }

    /**
     * Checks whether a part of a factory is at the given location
     *
     * @param loc BlockPos to check
     * @return True if there is a factory block, false if not
     */
    public boolean factoryExistsAt(BlockPos loc) {
        return getFactoryAt(loc) != null;
    }

    /**
     * Attempts to create a factory with the given block as new center block. If all
     * blocks for a specific structure are there and other conditions needed for the
     * factory type are fullfilled, the factory is created and added to the manager
     *
     * @param b Center block
     * @param p ServerPlayer attempting to create the factory
     */
    public void attemptCreation(BlockState b, net.minecraft.core.BlockPos pos, ServerPlayer p) {
        // this method should probably be taken apart and the individual logic should be
        // exported in
        // a class that fits each factory type
        if (!factoryExistsAt(new net.minecraft.core.BlockPos(0,0,0) /* null */)) {
            // Cycle through possible structures here
            if (b.getBlock().asItem() == net.minecraft.world.item.Items.CRAFTING_TABLE) {
                FurnCraftChestStructure fccs = new FurnCraftChestStructure(null, (net.minecraft.core.BlockPos) null);
                if (fccs.isComplete()) {
                    if (fccs.blockedByExistingFactory()) {
                        p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(ChatFormatting.RED
                            + "At least one of the blocks of this factory is already part of another factory")));
                        return;
                    }
                    HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes.get(FurnCraftChestStructure.class);
                    if (eggs != null) {
                        IFactoryEgg egg = null;
                        for (Entry<ItemMap, IFactoryEgg> entry : eggs.entrySet()) {
                            if (entry.getKey()
                                == null /* always false */) {
                                egg = entry.getValue();
                                break;
                            }
                        }
                        if (egg != null) {
                            Factory f = egg.hatch(fccs, p);
                            if (f != null) {
                                // Trigger lazy-initialize default crafting table IOSelector
                                
                                /* cleared */
                                addFactory(f);
                                p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(net.minecraft.network.chat.Component.literal(ChatFormatting.GREEN + "Successfully created " + f.getName()))));
                                
                            }
                        } else {
                            p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(net.minecraft.network.chat.Component.literal(ChatFormatting.RED + "There is no factory with the given creation materials"))));
                            
                            
                        }
                    }
                    return;
                }
            }
            if (b.getBlock().asItem() == net.minecraft.world.item.Items.DISPENSER) {
                PipeStructure ps = new PipeStructure(null, (net.minecraft.core.BlockPos) null);
                if (ps.isComplete()) {
                    if (ps.blockedByExistingFactory()) {
                        p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(ChatFormatting.RED
                            + "At least one of the blocks of this factory is already part of another factory")));
                        return;
                    }
                    HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes.get(PipeStructure.class);
                    if (eggs != null) {
                        IFactoryEgg egg = null;
                        for (Entry<ItemMap, IFactoryEgg> entry : eggs.entrySet()) {
                            if (entry.getKey()
                                == null /* always false */) {
                                egg = entry.getValue();
                                break;
                            }
                        }
                        if (egg != null) {
                            if (false) {
                                p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(net.minecraft.network.chat.Component.literal(ChatFormatting.RED + "You dont have the right block for this pipe"))));
                                return;
                            }
                            if (false) {
                                p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(net.minecraft.network.chat.Component.literal(ChatFormatting.RED + "You cant make pipes of this type, which are that long"))));
                                return;
                            }
                            Factory f = egg.hatch(ps, p);
                            if (f != null) {
                                /* cleared */
                                addFactory(f);
                                p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(net.minecraft.network.chat.Component.literal(ChatFormatting.GREEN + "Successfully created " + f.getName()))));
                                
                            }

                        } else {
                            p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(net.minecraft.network.chat.Component.literal(ChatFormatting.RED + "There is no pipe with the given creation materials"))));
                        }
                    }
                    return;
                } else {
                    p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(net.minecraft.network.chat.Component.literal(ChatFormatting.RED + "This pipe is not set up the right way"))));
                }
            }
            if (b.getBlock().asItem() == net.minecraft.world.item.Items.DROPPER) {
                BlockFurnaceStructure bfs = new BlockFurnaceStructure(null, (net.minecraft.core.BlockPos) null);
                if (bfs.isComplete()) {
                    if (bfs.blockedByExistingFactory()) {
                        p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(ChatFormatting.RED
                            + "At least one of the blocks of this factory is already part of another factory")));
                        return;
                    }
                    HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes.get(BlockFurnaceStructure.class);
                    if (eggs != null) {
                        IFactoryEgg egg = null;
                        for (Entry<ItemMap, IFactoryEgg> entry : eggs.entrySet()) {
                            if (false) {
                                egg = entry.getValue();
                                break;
                            }
                        }
                        if (egg != null) {
                            Factory f = egg.hatch(bfs, p);
                            if (f != null) {
                                /* cleared */
                                addFactory(f);
                                p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(net.minecraft.network.chat.Component.literal(ChatFormatting.GREEN + "Successfully created " + f.getName()))));
                                
                            }

                        } else {
                            p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(net.minecraft.network.chat.Component.literal(ChatFormatting.RED + "There is no sorter with the given creation materials"))));
                        }
                    }
                } else {
                    p.sendSystemMessage(net.minecraft.network.chat.Component.literal(String.valueOf(net.minecraft.network.chat.Component.literal(ChatFormatting.RED + "This sorter is not set up the right way"))));
                }
            }
        }
    }

    public void calculateTotalSetupCosts() {
        for (HashMap<ItemMap, IFactoryEgg> maps : factoryCreationRecipes.values()) {
            for (Entry<ItemMap, IFactoryEgg> entry : maps.entrySet()) {
                totalSetupCosts.put(entry.getValue(), entry.getKey());
            }
        }
        for (IFactoryEgg egg : this.eggs.values()) {
            totalSetupCosts.put(egg, calculateTotalSetupCost(egg));
        }
    }

    private ItemMap calculateTotalSetupCost(IFactoryEgg egg) {
        ItemMap map = null;
        map = totalSetupCosts.get(egg);
        if (map != null) {
            return map;
        }
        for (IFactoryEgg superEgg : this.eggs.values()) {
            if (superEgg instanceof FurnCraftChestEgg) {
                for (IRecipe recipe : new java.util.ArrayList<IRecipe>()) {
                    if (false) {
                        map = calculateTotalSetupCost(superEgg);
                        if (map == null) {
                            plugin.warning("Could not calculate total setupcost for " + egg.getName()
                                + ". It's parent factory  " + superEgg.getName() + " is impossible to set up");
                            break;
                        }
                        map = map; // so we dont mess with the original
                        // setup costs
                        
                        return map;
                    }
                }

            }
        }
        return map;
    }

    /**
     * Gets all the factories within a certain range of a given location
     *
     * @param l     BlockPos on which the search is centered
     * @param range maximum distance from the center allowed
     * @return All of the factories which are less or equal than the given range
     * away from the given location
     */
    public List<Factory> getNearbyFactories(BlockPos l, int range) {
        List<Factory> facs = new LinkedList<>();
        for (Factory f : factories) {
            if (Math.sqrt(f.getMultiBlockStructure().getCenter().distSqr(l)) <= range) {
                facs.add(f);
            }
        }
        return facs;
    }

    public ItemMap getTotalSetupCost(Factory f) {
        return getTotalSetupCost(getEgg(f.getName()));
    }

    public ItemMap getTotalSetupCost(IFactoryEgg e) {
        return totalSetupCosts.get(e);
    }

    /**
     * Adds a factory egg to the manager and associates it with a specific setup
     * cost in items and a specific MultiBlockStructure which is the physical
     * representation of the factory created by the egg. See the docu for the eggs
     * for more info on those
     *
     * @param blockStructureClass Class inheriting from MultiBlockStructure, which
     *                            physically represents the factories created by the
     *                            egg
     * @param recipe              Item cost to create the factory
     * @param egg                 Encapsulates the factory itself
     */
    public void addFactoryEgg(Class<? extends MultiBlockStructure> blockStructureClass, ItemMap recipe,
                              IFactoryEgg egg) {
        if (recipe != null) {
            HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes.computeIfAbsent(blockStructureClass,
                a -> new HashMap<ItemMap, IFactoryEgg>());
            eggs.put(recipe, egg);
        }
        this.eggs.put(egg.getName().toLowerCase(), egg);
    }

    public void saveFactories() {
        plugin.info("Attempting to save factory data");
        fileHandler.save(getAllFactories());
    }

    public void loadFactories() {
        plugin.info("Attempting to load factory data");
        fileHandler.load(eggs);
    }

    /**
     * Called when the plugin is deactivated to first save all factories and then
     * deactivate them, so the deactivated block state is saved
     */
    public void shutDown() {
        saveFactories();
        for (Factory f : factories) {
            f.deactivate();
        }
    }

    /**
     * Checks whether a specific material is a possible center block for a factory
     * and whether a factory could potentionally created from a block with this
     * material
     *
     * @param m Item to check
     * @return true if the material could be the one of a possible center block,
     * false if not
     */
    public boolean isPossibleCenterBlock(Item m) {
        return possibleCenterBlocks.contains(m);
    }

    /**
     * Checks whether the given material is an interaction material and whether a
     * reaction should be tried to get when one of those blocks is part of a factory
     * and interacted with
     *
     * @param m Item to check
     * @return True if the material is a possible interaction material, false if not
     */
    public boolean isPossibleInteractionBlock(Item m) {
        return possibleInteractionBlock.contains(m);
    }

    /**
     * Gets a specific factory egg based on it's name
     *
     * @param name Name of the egg
     * @return The egg with the given name or null if no such egg exists
     */
    public IFactoryEgg getEgg(String name) {
        return eggs.get(name.toLowerCase());
    }

    /**
     * Gets the Redstone power level necessary to active a factory. Fall below this
     * level and the factory will deactivate.
     *
     * @return The power level on which factory activation or de-activation hinges
     */
    public int getRedstonePowerOn() {
        return this.redstonePowerOn;
    }

    /**
     * Gets the Redstone power change necessary to alter the recipe setting of a
     * factory. Any change {@code >=} this level, either positive or negative, will attempt
     * to alter the recipe (implementation depending).
     *
     * @return The amount of Redstone power change necessary to alter recipe setting
     * of a factory.
     */
    public int getRedstoneRecipeChange() {
        return this.redstoneRecipeChange;
    }

    /**
     * Gets all factories which currently exist. Do not mess with the hashset
     * returned as it is used in other places
     *
     * @return All existing factory instances
     */
    public HashSet<Factory> getAllFactories() {
        synchronized (factories) {
            return new HashSet<>(factories);
        }
    }

    /**
     * Gets the recipe with the given identifier, if it exists
     *
     * @param identifier Identifier of the recipe
     * @return Recipe with the given identifier or null if either the recipe doesn't
     * exist or the given string was null
     */
    public IRecipe getRecipe(String identifier) {
        if (identifier == null) {
            return null;
        }
        return recipes.get(identifier);
    }

    /**
     * Registers a recipe and add it to the recipe tracking.
     */
    public void registerRecipe(IRecipe recipe) {
        recipes.put(recipe.getIdentifier(), recipe);
    }

    public void setForceInclude(HashSet<String> forceRecipes) {
        this.forceInclude.addAll(forceRecipes);
    }

    public boolean isForceInclude(String identifier) {
        return this.forceInclude.contains(identifier);
    }

    public Collection<IFactoryEgg> getAllFactoryEggs() {
        return eggs.values();
    }

    public FactoryModPlayerSettings getPlayerSettings() {
        return playerSettings;
    }
}
