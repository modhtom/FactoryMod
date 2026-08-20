package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import com.github.igotyou.FactoryMod.utility.ItemUtils;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CompactingRecipe extends InputRecipe {

    private List<Item> excludedMaterials;
    private String compactedLore;

    public CompactingRecipe(String identifier, ItemMap input, List<Item> excludedMaterial,
                            String name, int productionTime, String compactedLore) {
        super(identifier, name, productionTime, input);
        this.excludedMaterials = excludedMaterial;
        this.compactedLore = compactedLore;
    }

    private ItemMap getMap(Container inv) {
        ItemMap map = new ItemMap();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && !stack.isEmpty()) {
                map.addItemStack(stack);
            }
        }
        return map;
    }

    @Override
    public boolean enoughMaterialAvailable(Container inputInv) {
        if (!input.isContainedIn(getMap(inputInv))) {
            return false;
        }
        ItemMap im = getMap(inputInv);
        for (int i = 0; i < inputInv.getContainerSize(); i++) {
            ItemStack is = inputInv.getItem(i);
            if (is != null && !is.isEmpty()) {
                if (compactable(is, im)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean applyEffect(Container inputInv, Container outputInv, Factory fccf) {
        if (input.isContainedIn(getMap(inputInv))) {
            ItemMap im = getMap(inputInv);
            for (int i = 0; i < inputInv.getContainerSize(); i++) {
                ItemStack is = inputInv.getItem(i);
                if (is != null && !is.isEmpty()) {
                    if (compactable(is, im)) {
                        ItemStack compacted = is.copy();
                        compactStack(compacted);
                        // We should test if it fits in outputInv
                        // But simplified:
                        if (input.removeSafelyFrom(inputInv)) {
                            compact(is, inputInv, outputInv);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public List<ItemStack> getOutputRepresentation(Container i, Factory fccf) {
        List<ItemStack> result = new LinkedList<>();
        if (i == null) {
            ItemStack is = new ItemStack(Items.STONE, 64);
            compactStack(is);
            result.add(is);
            return result;
        }
        ItemMap im = getMap(i);
        for (int j = 0; j < i.getContainerSize(); j++) {
            ItemStack is = i.getItem(j);
            if (is != null && !is.isEmpty()) {
                if (compactable(is, im)) {
                    ItemStack decompactedStack = is.copy();
                    compactStack(decompactedStack);
                    result.add(decompactedStack);
                    break;
                }
            }
        }
        return result;
    }

    public Item getRecipeRepresentationMaterial() {
        return Items.CHEST;
    }

    private void compact(ItemStack is, Container inputInv, Container outputInv) {
        ItemStack copy = is.copy();
        copy.setCount(getCompactStackSize(copy));
        ItemMap toRemove = new ItemMap(copy);
        if (toRemove.removeSafelyFrom(inputInv)) {
            compactStack(copy);
            // find empty slot in output or merge
            for(int j=0; j<outputInv.getContainerSize(); j++) {
                ItemStack outSlot = outputInv.getItem(j);
                if(outSlot.isEmpty()) {
                    outputInv.setItem(j, copy);
                    break;
                } else if(ItemStack.isSameItemSameComponents(outSlot, copy)) {
                    int canAdd = Math.min(copy.getCount(), outSlot.getMaxStackSize() - outSlot.getCount());
                    if(canAdd > 0) {
                        outSlot.grow(canAdd);
                        copy.shrink(canAdd);
                        if(copy.isEmpty()) break;
                    }
                }
            }
        }
    }

    private void compactStack(ItemStack is) {
        ItemUtils.addLore(is, compactedLore);
        is.setCount(1);
    }

    public static int getCompactStackSize(ItemStack m) {
        int max = m.getMaxStackSize();
        if (max == 64) return 64;
        if (max == 16 || max == 2) return 16;
        if (max == 1) return 8;
        return 999_999; 
    }

    private boolean compactable(ItemStack is, ItemMap im) {
        if (is == null || is.isEmpty() || excludedMaterials.contains(is.getItem()) || (input.getAmount(is.getItem()) != 0)) {
            return false;
        }
        net.minecraft.world.item.component.ItemLore lore = is.get(net.minecraft.core.component.DataComponents.LORE);
        if (lore != null) {
            for (net.minecraft.network.chat.Component line : lore.lines()) {
                if (line.getString().contains(compactedLore)) {
                    return false;
                }
            }
        }
        // Simplified Shulker/bundle check, just assume max stack size check
        return im.getAmount(is.getItem()) >= getCompactStackSize(is);
    }

    @Override
    public String getTypeIdentifier() {
        return "COMPACT";
    }

    public String getCompactedLore() {
        return compactedLore;
    }

    public List<Item> getExcludedMaterials() {
        return excludedMaterials;
    }
    
    @Override
    public List<String> getTextualOutputRepresentation(Container i, Factory fccf) {
        return Arrays.asList("Input stack compacted into a single item");
    }

    public EffectFeasibility evaluateEffectFeasibility(Container inputInv, Container outputInv, Factory fccf) {
        return new EffectFeasibility(enoughMaterialAvailable(inputInv), "");
    }
    
    public ItemStack getRecipeRepresentation() {
        return new ItemStack(Items.CHEST);
    }
    
    public List<ItemStack> getInputRepresentation(Container i, Factory r) {
        List<ItemStack> res = new ArrayList<>();
        res.addAll(input.getItemStackRepresentation());
        return res;
    }
}
