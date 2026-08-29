package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class RandomItemRecipe extends InputRecipe {
    private final List<ItemStack> display;
    private final Map<List<ItemStack>, Double> outputs;
    private final Random random = new Random();

    public RandomItemRecipe(String identifier, String name, int productionTime, ItemMap input, List<ItemStack> display, Map<List<ItemStack>, Double> outputs) {
        super(identifier, name, productionTime, input);
        this.display = display;
        this.outputs = outputs;
    }

    private List<ItemStack> getSplitOutputs(List<ItemStack> output) {
        List<ItemStack> split = new ArrayList<>();
        for (ItemStack stack : output) {
            int amount = stack.getCount();
            while (amount > 0) {
                int count = Math.min(amount, stack.getMaxStackSize());
                ItemStack copy = stack.copy();
                copy.setCount(count);
                split.add(copy);
                amount -= count;
            }
        }
        return split;
    }

    @Override
    public EffectFeasibility evaluateEffectFeasibility(Container inputInv, Container outputInv, Factory fccf) {
        if (!enoughMaterialAvailable(inputInv)) {
            return new EffectFeasibility(false, "insufficient input materials");
        }
        return new EffectFeasibility(true, null);
    }

    @Override
    public boolean applyEffect(Container inputInventory, Container outputInventory, Factory factory) {
        if (!evaluateEffectFeasibility(inputInventory, outputInventory, factory).isFeasible()) {
            return false;
        }
        
        double roll = random.nextDouble();
        double current = 0.0;
        List<ItemStack> selected = null;
        for (Map.Entry<List<ItemStack>, Double> entry : outputs.entrySet()) {
            current += entry.getValue();
            if (roll <= current) {
                selected = entry.getKey();
                break;
            }
        }
        if (selected == null && !outputs.isEmpty()) {
            selected = outputs.keySet().iterator().next();
        }
        
        if (selected != null) {
            List<ItemStack> split = getSplitOutputs(selected);
            if (!canFit(outputInventory, split)) return false;
            if (!input.removeSafelyFrom(inputInventory)) return false;
            for (ItemStack stack : split) {
                insert(outputInventory, stack.copy());
            }
            outputInventory.setChanged();
            return true;
        }
        return false;
    }

    private boolean canFit(Container inventory, List<ItemStack> items) {
        List<ItemStack> simulation = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            simulation.add(inventory.getItem(slot).copy());
        }
        for (ItemStack result : items) {
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

    @Override public String getTypeIdentifier() { return "RANDOM"; }
    @Override public List<ItemStack> getOutputRepresentation(Container inventory, Factory factory) {
        return new ArrayList<>(display);
    }
    @Override public List<String> getTextualOutputRepresentation(Container inventory, Factory factory) {
        return List.of("Random output");
    }
    @Override public ItemStack getRecipeRepresentation() {
        return display.stream().findFirst().orElse(ItemStack.EMPTY);
    }
}
