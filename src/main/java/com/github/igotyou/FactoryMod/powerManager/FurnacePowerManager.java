package com.github.igotyou.FactoryMod.powerManager;

import com.github.igotyou.FactoryMod.utility.IIOFInventoryProvider;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Power manager for a FurnCraftChest factory, which uses a specific item in the
 * furnace of the factory as fuel
 */
public class FurnacePowerManager implements IPowerManager {

    private ItemStack fuel;
    private int powerCounter;
    private int fuelConsumptionIntervall;
    private Block furnace;
    private IIOFInventoryProvider iofProvider;

    public FurnacePowerManager(Block furnace, ItemStack fuel,
                               int fuelConsumptionIntervall) {
        this.fuel = fuel;
        this.fuelConsumptionIntervall = fuelConsumptionIntervall;
        this.furnace = furnace;
    }

    public FurnacePowerManager(ItemStack fuel, int fuelConsumptionIntervall) {
        this.fuel = fuel;
        this.fuelConsumptionIntervall = fuelConsumptionIntervall;
    }

    public void setIofProvider(IIOFInventoryProvider iofProvider) {
        this.iofProvider = iofProvider;
    }

    public IIOFInventoryProvider getIofProvider() {
        return iofProvider;
    }

    public int getPowerCounter() {
        return powerCounter;
    }

    public boolean powerAvailable(int fuelCount) {
        if (fuelCount == 0) {
            return true;
        }
        if (iofProvider != null) {
            Inventory fuelInv = iofProvider.getFuelInventory();
            if (fuelInv != null) {
                return countFuel(fuelInv) >= fuelCount;
            }
        }

        if (furnace.getType() != Material.FURNACE) {
            return false;
        }

        FurnaceInventory fi = ((Furnace) furnace.getState()).getInventory();
        return countFuel(fi) >= fuelCount;
    }

    public int getPowerConsumptionIntervall() {
        return fuelConsumptionIntervall;
    }

    public void increasePowerCounter(int amount) {
        powerCounter += amount;
    }

    public void setPowerCounter(int amount) {
        powerCounter = amount;
    }

    public void consumePower(int fuelCount) {
        if (iofProvider != null) {
            Inventory fuelInv = iofProvider.getFuelInventory();
            if (fuelInv != null) {
                removeFuel(fuelInv, fuelCount);
                return;
            }
        }

        FurnaceInventory fi = ((Furnace) furnace.getState()).getInventory();
        removeFuel(fi, fuelCount);
    }

    public int getFuelAmountAvailable() {
        if (iofProvider != null) {
            Inventory fuelInv = iofProvider.getFuelInventory();
            if (fuelInv != null) {
                return countFuel(fuelInv);
            }
        }
        return countFuel(((Furnace) furnace.getState()).getInventory());
    }

    public ItemStack getFuel() {
        return fuel;
    }

    private int countFuel(Inventory inventory) {
        int amount = 0;
        for (ItemStack item : inventory.getContents()) {
            if (isFuel(item)) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    private void removeFuel(Inventory inventory, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inventory.getSize() && remaining > 0; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (!isFuel(item)) {
                continue;
            }
            int removed = Math.min(item.getAmount(), remaining);
            item.setAmount(item.getAmount() - removed);
            remaining -= removed;
            if (item.getAmount() <= 0) {
                inventory.setItem(slot, null);
            } else {
                inventory.setItem(slot, item);
            }
        }
    }

    private boolean isFuel(ItemStack item) {
        if (item == null || fuel == null) {
            return false;
        }
        Material itemType = item.getType();
        Material fuelType = fuel.getType();
        if (fuelType == Material.COAL || fuelType == Material.CHARCOAL) {
            return itemType == Material.COAL || itemType == Material.CHARCOAL;
        }
        return itemType == fuelType;
    }

}
