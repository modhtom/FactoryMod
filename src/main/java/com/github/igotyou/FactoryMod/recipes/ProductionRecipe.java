package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** A deterministic input-to-output recipe defined in config.yml. */
public final class ProductionRecipe extends InputRecipe {
    private final ItemMap output;

    public ProductionRecipe(String identifier, String name, int productionTime, ItemMap input, ItemMap output) {
        super(identifier, name, productionTime, input);
        this.output = output;
    }

    @Override
    public boolean applyEffect(Container inputInventory, Container outputInventory, Factory factory) {
        if (!enoughMaterialAvailable(inputInventory) || !canFit(outputInventory)) {
            return false;
        }
        if (!input.removeSafelyFrom(inputInventory)) {
            return false;
        }
        for (ItemStack stack : output.getItemStackRepresentation()) {
            insert(outputInventory, stack.copy());
        }
        outputInventory.setChanged();
        return true;
    }

    private boolean canFit(Container inventory) {
        List<ItemStack> simulation = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            simulation.add(inventory.getItem(slot).copy());
        }
        for (ItemStack result : output.getItemStackRepresentation()) {
            ItemStack remaining = result.copy();
            for (ItemStack slot : simulation) {
                if (ItemStack.isSameItemSameComponents(slot, remaining)) {
                    int moved = Math.min(remaining.getCount(), slot.getMaxStackSize() - slot.getCount());
                    slot.grow(moved);
                    remaining.shrink(moved);
                }
            }
            for (int slot = 0; slot < simulation.size() && !remaining.isEmpty(); slot++) {
                if (simulation.get(slot).isEmpty()) {
                    simulation.set(slot, remaining.copy());
                    remaining.setCount(0);
                }
            }
            if (!remaining.isEmpty()) return false;
        }
        return true;
    }

    private static void insert(Container inventory, ItemStack stack) {
        for (int slot = 0; slot < inventory.getContainerSize() && !stack.isEmpty(); slot++) {
            ItemStack current = inventory.getItem(slot);
            if (ItemStack.isSameItemSameComponents(current, stack)) {
                int moved = Math.min(stack.getCount(), current.getMaxStackSize() - current.getCount());
                current.grow(moved);
                stack.shrink(moved);
            }
        }
        for (int slot = 0; slot < inventory.getContainerSize() && !stack.isEmpty(); slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                inventory.setItem(slot, stack.copy());
                stack.setCount(0);
            }
        }
    }

    @Override public String getTypeIdentifier() { return "PRODUCTION"; }
    @Override public List<ItemStack> getOutputRepresentation(Container inventory, Factory factory) {
        return new ArrayList<>(output.getItemStackRepresentation());
    }
    @Override public List<String> getTextualOutputRepresentation(Container inventory, Factory factory) {
        return List.of("Configured production output");
    }
    @Override public ItemStack getRecipeRepresentation() {
        return output.getItemStackRepresentation().stream().findFirst().orElse(ItemStack.EMPTY);
    }
}
