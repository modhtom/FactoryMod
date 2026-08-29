package com.github.igotyou.FactoryMod.recipes;

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

public class DecompactingRecipe extends InputRecipe {

    private final String compactedLore;

    public DecompactingRecipe(String identifier, ItemMap input, String name, int productionTime,
                              String compactedLore) {
        super(identifier, name, productionTime, input);
        this.compactedLore = compactedLore;
    }

    @Override
    public boolean enoughMaterialAvailable(Container inputInv) {
        ItemMap inputMap = new ItemMap();
        for(int i=0; i<inputInv.getContainerSize(); i++) {
            inputMap.addItemStack(inputInv.getItem(i));
        }
        if (!input.isContainedIn(inputMap)) {
            return false;
        }
        for (int i = 0; i < inputInv.getContainerSize(); i++) {
            ItemStack is = inputInv.getItem(i);
            if (is != null && !is.isEmpty()) {
                if (isDecompactable(is)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public EffectFeasibility evaluateEffectFeasibility(Container inputInv, Container outputInv, Factory fccf) {
        if (!enoughMaterialAvailable(inputInv)) {
            return new EffectFeasibility(false, "insufficient input materials or no decompactable items");
        }
        return new EffectFeasibility(true, null);
    }

    @Override
    public boolean applyEffect(Container inputInv, Container outputInv, Factory fccf) {
        ItemMap inputMap = new ItemMap();
        for(int i=0; i<inputInv.getContainerSize(); i++) {
            inputMap.addItemStack(inputInv.getItem(i));
        }
        if (input.isContainedIn(inputMap)) {
            for (int i=0; i < inputInv.getContainerSize(); i++) {
                ItemStack is = inputInv.getItem(i);
                if (is != null && !is.isEmpty()) {
                    if (isDecompactable(is)) {
                        ItemStack removeClone = is.copy();
                        removeClone.setCount(1);
                        ItemMap toRemove = new ItemMap(removeClone);
                        ItemMap toAdd = new ItemMap();
                        removeCompactLore(removeClone);
                        toAdd.addItemAmount(removeClone.getItem(), CompactingRecipe.getCompactStackSize(removeClone));
                        if (input.removeSafelyFrom(inputInv)) {
                            if (toRemove.removeSafelyFrom(inputInv)) {
                                for (ItemStack add : toAdd.getItemStackRepresentation()) {
                                    for(int j=0; j<outputInv.getContainerSize(); j++) {
                                        ItemStack outSlot = outputInv.getItem(j);
                                        if(outSlot.isEmpty()) {
                                            outputInv.setItem(j, add.copy());
                                            break;
                                        } else if(ItemStack.isSameItemSameComponents(outSlot, add)) {
                                            int canAdd = Math.min(add.getCount(), outSlot.getMaxStackSize() - outSlot.getCount());
                                            if(canAdd > 0) {
                                                outSlot.grow(canAdd);
                                                add.shrink(canAdd);
                                                if(add.isEmpty()) break;
                                            }
                                        }
                                    }
                                }
                            }
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
            result.add(new ItemStack(Items.STONE, 64));
            return result;
        }
        for (int j=0; j<i.getContainerSize(); j++) {
            ItemStack is = i.getItem(j);
            if (is != null && !is.isEmpty()) {
                if (isDecompactable(is)) {
                    ItemStack copy = is.copy();
                    removeCompactLore(copy);
                    ItemMap output = new ItemMap();
                    output.addItemAmount(copy.getItem(), CompactingRecipe.getCompactStackSize(copy));
                    result.addAll(output.getItemStackRepresentation());
                }
            }
        }
        return result;
    }

    private boolean isDecompactable(ItemStack is) {
        net.minecraft.world.item.component.ItemLore lore = is.get(net.minecraft.core.component.DataComponents.LORE);
        if (lore != null) {
            for (net.minecraft.network.chat.Component line : lore.lines()) {
                if (line.getString().contains(compactedLore)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeCompactLore(ItemStack is) {
        // Simplified lore removal: just reset lore for decompaction since it is just compacted material
        is.remove(net.minecraft.core.component.DataComponents.LORE);
    }

    @Override
    public String getTypeIdentifier() {
        return "DECOMPACT";
    }

    public String getCompactedLore() {
        return compactedLore;
    }

    @Override
    public List<String> getTextualOutputRepresentation(Container i, Factory fccf) {
        return Arrays.asList("An entire stack of the decompacted item if it's stackable", "---OR---", "Eight of the decompacted item if it's not stackable");
    }
    
    public ItemStack getRecipeRepresentation() {
        return new ItemStack(Items.TRAPPED_CHEST);
    }
    
    public List<ItemStack> getInputRepresentation(Container i, Factory r) {
        List<ItemStack> res = new ArrayList<>();
        res.addAll(input.getItemStackRepresentation());
        return res;
    }
}
