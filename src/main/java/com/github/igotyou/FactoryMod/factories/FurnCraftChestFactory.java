package com.github.igotyou.FactoryMod.factories;

import com.github.igotyou.FactoryMod.interactionManager.FurnCraftChestInteractionManager;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.EffectFeasibility;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import java.util.List;

public class FurnCraftChestFactory extends Factory {
    private int currentProductionTimer = 0;
    private int currentEnergyTimer = 0;
    private IRecipe currentRecipe;
    private List<IRecipe> recipes;
    private ItemMap fuel;
    private int fuelConsumptionIntervall;

    public FurnCraftChestFactory(FurnCraftChestStructure mbs, int updateTime, String name, List<IRecipe> recipes, ItemMap fuel, int fuelConsumptionIntervall) {
        super(null, mbs, updateTime, name);
        setInteractionManager(new FurnCraftChestInteractionManager(this));
        
        this.recipes = recipes;
        this.fuel = (fuel != null) ? fuel : new ItemMap();
        this.fuelConsumptionIntervall = Math.max(1, fuelConsumptionIntervall);
        if (!recipes.isEmpty()) {
            this.currentRecipe = recipes.get(0);
        }
    }

    public List<IRecipe> getRecipes() { return recipes; }
    public IRecipe getCurrentRecipe() { return currentRecipe; }
    public void setRecipe(IRecipe recipe) { this.currentRecipe = recipe; }

    public void upgrade(String name, List<IRecipe> recipes, ItemMap fuel, int fuelConsumptionIntervall, int updateTime) {
        this.name = name;
        this.recipes = recipes;
        this.fuel = fuel;
        this.fuelConsumptionIntervall = fuelConsumptionIntervall;
        this.updateTime = updateTime;
        if (!recipes.isEmpty()) {
            this.currentRecipe = recipes.get(0);
        } else {
            this.currentRecipe = null;
        }
    }

    @Override
    public void attemptToActivate(ServerPlayer p, boolean onStartUp) {
        if (active) return;
        if (currentRecipe == null) {
            if (p != null) p.sendSystemMessage(Component.literal("\u00a7cNo recipe selected for " + name + "! Right-click with stick to select a recipe."));
            return;
        }
        
        FurnCraftChestStructure fccs = (FurnCraftChestStructure) mbs;
        if (!fccs.isComplete()) {
            if (p != null) p.sendSystemMessage(Component.literal("\u00a7cFactory structure is broken: [Chest - Crafting Table - Furnace]"));
            return;
        }

        Container chest = null;
        net.minecraft.world.level.block.state.BlockState chestState = mbs.getWorld().getBlockState(fccs.getChest());
        if (chestState.getBlock() instanceof net.minecraft.world.level.block.ChestBlock chestBlock) {
            chest = net.minecraft.world.level.block.ChestBlock.getContainer(chestBlock, chestState, mbs.getWorld(), fccs.getChest(), true);
        } else {
            BlockEntity be = mbs.getWorld().getBlockEntity(fccs.getChest());
            if (be instanceof Container c) {
                chest = c;
            }
        }
        if (chest == null) {
            if (p != null) p.sendSystemMessage(Component.literal("\u00a7cChest not found."));
            return;
        }

        if (currentRecipe instanceof InputRecipe inputRecipe) {
            if (!inputRecipe.enoughMaterialAvailable(chest)) {
                if (p != null) {
                    p.sendSystemMessage(Component.literal("\u00a7cNot enough materials in chest for \u00a7e" + currentRecipe.getName() + "\u00a7c."));
                    StringBuilder sb = new StringBuilder("\u00a77Needs: \u00a7f");
                    for (ItemStack is : inputRecipe.getInput().getItemStackRepresentation()) {
                        sb.append(is.getCount()).append("x ").append(is.getHoverName().getString()).append(", ");
                    }
                    if (sb.length() > 2) sb.setLength(sb.length() - 2);
                    p.sendSystemMessage(Component.literal(sb.toString()));
                }
                return;
            }
        }

        // Check fuel in furnace
        if (!fuel.getItemStackRepresentation().isEmpty()) {
            BlockEntity furnaceEntity = mbs.getWorld().getBlockEntity(fccs.getFurnace());
            if (!(furnaceEntity instanceof FurnaceBlockEntity furnace)) {
                if (p != null) p.sendSystemMessage(Component.literal("\u00a7cFurnace not found."));
                return;
            }
            ItemMap furnaceMap = new ItemMap();
            for (int i = 0; i < furnace.getContainerSize(); i++) {
                furnaceMap.addItemStack(furnace.getItem(i));
            }
            if (!fuel.isContainedIn(furnaceMap)) {
                if (p != null) p.sendSystemMessage(Component.literal("\u00a7cNo fuel in furnace! (Put Coal or Charcoal in the Furnace)"));
                return;
            }
        }

        active = true;
        currentProductionTimer = 0;
        currentEnergyTimer = 0;
        
        // Initial fuel consumption on activation
        if (!fuel.getItemStackRepresentation().isEmpty()) {
            BlockEntity furnaceEntity = mbs.getWorld().getBlockEntity(fccs.getFurnace());
            if (furnaceEntity instanceof FurnaceBlockEntity furnace) {
                if (!fuel.removeSafelyFrom(furnace)) {
                    active = false;
                    if (p != null) p.sendSystemMessage(Component.literal("\u00a7cFailed to consume initial fuel."));
                    return;
                }
            }
        }

        turnFurnaceOn(mbs.getWorld(), fccs.getFurnace());
        scheduleUpdate();
        if (p != null) p.sendSystemMessage(Component.literal("\u00a7a" + name + " activated! Running recipe: \u00a7e" + currentRecipe.getName()));
    }

    public void tick() {
        run();
    }

    @Override
    public void run() {
        if (!active) return;
        
        FurnCraftChestStructure fccs = (FurnCraftChestStructure) mbs;
        if (!fccs.isComplete()) {
            deactivate("Structure broken");
            return;
        }

        Container chest = null;
        net.minecraft.world.level.block.state.BlockState chestState = mbs.getWorld().getBlockState(fccs.getChest());
        if (chestState.getBlock() instanceof net.minecraft.world.level.block.ChestBlock chestBlock) {
            chest = net.minecraft.world.level.block.ChestBlock.getContainer(chestBlock, chestState, mbs.getWorld(), fccs.getChest(), true);
        } else {
            BlockEntity be = mbs.getWorld().getBlockEntity(fccs.getChest());
            if (be instanceof Container c) {
                chest = c;
            }
        }
        
        if (chest == null) {
            deactivate("Chest missing");
            return;
        }

        BlockEntity furnaceEntity = mbs.getWorld().getBlockEntity(fccs.getFurnace());
        if (!(furnaceEntity instanceof FurnaceBlockEntity furnace)) {
            deactivate("Furnace missing");
            return;
        }

        if (currentRecipe instanceof InputRecipe inputRecipe) {
            if (!inputRecipe.enoughMaterialAvailable(chest)) {
                deactivate("Insufficient materials");
                return;
            }
        }
        
        currentProductionTimer += updateTime;
        currentEnergyTimer += updateTime;

        // Check and consume fuel
        if (fuel != null && !fuel.getItemStackRepresentation().isEmpty() && currentEnergyTimer >= fuelConsumptionIntervall) {
            if (!fuel.removeSafelyFrom(furnace)) {
                deactivate("Out of fuel");
                return;
            }
            currentEnergyTimer = 0;
        }

        if (currentRecipe instanceof InputRecipe inputRecipe) {
            if (currentProductionTimer >= inputRecipe.getProductionTime()) {
                EffectFeasibility feas = inputRecipe.evaluateEffectFeasibility(chest, chest, this);
                if (!feas.isFeasible()) {
                    deactivate(feas.reasonSnippet() != null ? feas.reasonSnippet() : "failed recipe conditions");
                    return;
                }
                boolean success = inputRecipe.applyEffect(chest, chest, this);
                if (!success) {
                    deactivate("Output inventory is full");
                    return;
                }
                
                if (!(inputRecipe instanceof com.github.igotyou.FactoryMod.recipes.RepairRecipe)) {
                    setHealth(getHealth() - 10);
                    if (getHealth() <= 0) {
                        deactivate("Factory broke down due to disrepair");
                        return; // FactoryModManager will clean it up next tick
                    }
                }
                
                notifyPlayers("\u00a7aCompleted task: " + currentRecipe.getName() + " \u00a77(Health: " + getHealth() + "/" + getMaxHealth() + ")");
                currentProductionTimer = 0;
            }
        }
        
        scheduleUpdate();
    }

    public void deactivate(String reason) {
        if (active) {
            active = false;
            FurnCraftChestStructure fccs = (FurnCraftChestStructure) mbs;
            turnFurnaceOff(mbs.getWorld(), fccs.getFurnace());
            notifyPlayers("\u00a7cStopped: " + reason);
        }
    }

    @Override
    public void deactivate() {
        deactivate("Turned off manually");
    }

    private void notifyPlayers(String message) {
        if (mbs != null && mbs.getWorld() != null) {
            net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(mbs.getCenter()).inflate(15.0);
            java.util.List<net.minecraft.server.level.ServerPlayer> players = mbs.getWorld().getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class, box, p -> true);
            for (net.minecraft.server.level.ServerPlayer p : players) {
                p.sendSystemMessage(net.minecraft.network.chat.Component.literal("\u00a78[\u00a76" + name + "\u00a78] " + message));
            }
        }
    }
}
