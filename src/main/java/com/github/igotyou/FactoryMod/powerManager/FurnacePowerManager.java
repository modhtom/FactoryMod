package com.github.igotyou.FactoryMod.powerManager;

import com.github.igotyou.FactoryMod.utility.IIOFInventoryProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Power manager for a FurnCraftChest factory, which uses a specific item in the
 * furnace of the factory as fuel
 */
public class FurnacePowerManager implements IPowerManager {

    private ItemStack fuel;
    private int powerCounter;
    private int fuelConsumptionIntervall;
    private BlockState furnace;
    private IIOFInventoryProvider iofProvider;

    public FurnacePowerManager(BlockState furnace, ItemStack fuel,
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
            Container fuelInv = iofProvider.getFuelInventory();
            if (fuelInv != null) {
                return countFuel(fuelInv) >= fuelCount;
            }
        }

        return false;
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
            Container fuelInv = iofProvider.getFuelInventory();
            if (fuelInv != null) {
                removeFuel(fuelInv, fuelCount);
                return;
            }
        }
    }

    public int getFuelAmountAvailable() {
        if (iofProvider != null) {
            Container fuelInv = iofProvider.getFuelInventory();
            if (fuelInv != null) {
                return countFuel(fuelInv);
            }
        }
        return 0;
    }

    public ItemStack getFuel() {
        return fuel;
    }

    private int countFuel(Container inventory) {
        int amount = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (isFuel(item)) {
                amount += item.getCount();
            }
        }
        return amount;
    }

    private void removeFuel(Container inventory, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (!isFuel(item)) {
                continue;
            }
            int removed = Math.min(item.getCount(), remaining);
            item.setCount(item.getCount() - removed);
            remaining -= removed;
            if (item.getCount() <= 0) {
                inventory.setItem(slot, ItemStack.EMPTY);
            } else {
                inventory.setItem(slot, item);
            }
        }
    }

    private boolean isFuel(ItemStack item) {
        if (item == null || item.isEmpty() || fuel == null) {
            return false;
        }
        Item itemType = item.getItem();
        Item fuelType = fuel.getItem();
        // Fabric Items don't need COAL/CHARCOAL check if they are the same
        return itemType == fuelType;
    }

}
