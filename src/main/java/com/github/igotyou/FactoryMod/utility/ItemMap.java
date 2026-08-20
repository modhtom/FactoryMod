package com.github.igotyou.FactoryMod.utility;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class ItemMap {
    private final Map<Item, Integer> items = new HashMap<>();

    public ItemMap() {}

    public ItemMap(ItemStack stack) {
        addItemStack(stack);
    }

    public ItemMap(Collection<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            addItemStack(stack);
        }
    }

    public void addItemStack(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            addItemAmount(stack.getItem(), stack.getCount());
        }
    }

    public void addItemAmount(Item item, int amount) {
        items.put(item, items.getOrDefault(item, 0) + amount);
    }

    public void merge(ItemMap other) {
        for (Map.Entry<Item, Integer> entry : other.items.entrySet()) {
            addItemAmount(entry.getKey(), entry.getValue());
        }
    }

    public int getAmount(Item item) {
        return items.getOrDefault(item, 0);
    }

    public boolean remove(ItemMap other) {
        if (!isContainedIn(other)) return false;
        for (Map.Entry<Item, Integer> entry : other.items.entrySet()) {
            int current = items.getOrDefault(entry.getKey(), 0);
            items.put(entry.getKey(), current - entry.getValue());
        }
        return true;
    }

    public boolean isContainedIn(ItemMap other) {
        for (Map.Entry<Item, Integer> entry : this.items.entrySet()) {
            if (other.getAmount(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public Collection<ItemStack> getItemStackRepresentation() {
        Collection<ItemStack> stacks = new java.util.ArrayList<>();
        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            int amount = entry.getValue();
            while (amount > 0) {
                int count = Math.min(amount, entry.getKey().getDefaultMaxStackSize());
                stacks.add(new ItemStack(entry.getKey(), count));
                amount -= count;
            }
        }
        return stacks;
    }

    public int getTotalItemAmount() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void removeItemStackCompletely(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            items.remove(stack.getItem());
        }
    }
    
    public ItemMap clone() {
        ItemMap map = new ItemMap();
        map.items.putAll(this.items);
        return map;
    }
    
    public Collection<ItemStack> getLoredItemCountRepresentation() {
        Collection<ItemStack> stacks = new java.util.ArrayList<>();
        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            ItemStack stack = new ItemStack(entry.getKey(), 1);
            ItemUtils.addLore(stack, "Amount: " + entry.getValue());
            stacks.add(stack);
        }
        return stacks;
    }
    
    public boolean removeSafelyFrom(Container inv) {
        ItemMap have = new ItemMap();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            have.addItemStack(inv.getItem(i));
        }
        if (!this.isContainedIn(have)) return false;

        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            Item item = entry.getKey();
            int amountNeeded = entry.getValue();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(item)) {
                    int toRemove = Math.min(amountNeeded, stack.getCount());
                    stack.shrink(toRemove);
                    amountNeeded -= toRemove;
                    if (amountNeeded <= 0) break;
                }
            }
        }
        inv.setChanged();
        return true;
    }
    
    public Map<Item, Integer> getAllItems() {
        return items;
    }
}
