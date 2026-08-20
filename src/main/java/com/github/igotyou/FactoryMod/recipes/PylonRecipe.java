package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import com.github.igotyou.FactoryMod.utility.ItemUtils;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class PylonRecipe extends InputRecipe {

    private ItemMap output;
    private static int currentGlobalWeight;
    private static int globalLimit;
    private int weight;

    public PylonRecipe(String identifier, String name, int productionTime, ItemMap input,
                       ItemMap output, int weight) {
        super(identifier, name, productionTime, input);
        this.output = output;
        this.weight = weight;
    }

    @Override
    public boolean applyEffect(Container inputInv, Container outputInv, Factory fccf) {
        ItemMap inMap = new ItemMap();
        for(int i=0; i<inputInv.getContainerSize(); i++) {
             inMap.addItemStack(inputInv.getItem(i));
        }
        if (!input.isContainedIn(inMap)) {
            return false;
        }
        ItemMap actualOutput = getCurrentOutput();
        if (input.removeSafelyFrom(inputInv)) {
            for (ItemStack is : actualOutput.getItemStackRepresentation()) {
                for(int j=0; j<outputInv.getContainerSize(); j++) {
                    ItemStack outSlot = outputInv.getItem(j);
                    if(outSlot.isEmpty()) {
                        outputInv.setItem(j, is.copy());
                        break;
                    } else if(ItemStack.isSameItemSameComponents(outSlot, is)) {
                        int canAdd = Math.min(is.getCount(), outSlot.getMaxStackSize() - outSlot.getCount());
                        if(canAdd > 0) {
                            outSlot.grow(canAdd);
                            is.shrink(canAdd);
                            if(is.isEmpty()) break;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static void setGlobalLimit(int limit) {
        globalLimit = limit;
    }

    public static int getGlobalLimit() {
        return globalLimit;
    }

    @Override
    public List<ItemStack> getOutputRepresentation(Container i, Factory fccf) {
        ItemMap currOut = getCurrentOutput();
        List<ItemStack> res = new LinkedList<>();
        for (ItemStack is : currOut.getItemStackRepresentation()) {
            ItemUtils.setLore(is, "Pylon factory item", "Current global weight: " + currentGlobalWeight);
            res.add(is);
        }
        return res;
    }

    @Override
    public boolean enoughMaterialAvailable(Container inputInv) {
        ItemMap inMap = new ItemMap();
        for(int i=0; i<inputInv.getContainerSize(); i++) {
             inMap.addItemStack(inputInv.getItem(i));
        }
        return input.isContainedIn(inMap);
    }

    public int getWeight() {
        return weight;
    }

    private ItemMap getCurrentOutput() {
        double overload = Math.max(1.0, (float) currentGlobalWeight / (float) (globalLimit > 0 ? globalLimit : 1));
        double multiplier = 1.0 / overload;
        ItemMap actualOutput = new ItemMap();
        for (Entry<Item, Integer> entry : output.getAllItems().entrySet()) {
            actualOutput.addItemAmount(entry.getKey(), (int) (entry.getValue() * multiplier));
        }
        return actualOutput;
    }

    public static void addWeight(int weight) {
        currentGlobalWeight += weight;
    }

    public static void removeWeight(int weight) {
        currentGlobalWeight -= weight;
    }

    @Override
    public String getTypeIdentifier() {
        return "PYLON";
    }

    public ItemMap getOutput() {
        return output;
    }

    @Override
    public List<String> getTextualOutputRepresentation(Container i, Factory fccf) {
        List<String> result = new ArrayList<>();
        for (Entry<Item, Integer> entry : output.getAllItems().entrySet()) {
            if (entry.getValue() > 0) {
                result.add(entry.getValue() + " " + entry.getKey().getName(new ItemStack(entry.getKey())).getString());
            }
        }
        return result;
    }

    public EffectFeasibility evaluateEffectFeasibility(Container inputInv, Container outputInv, Factory fccf) {
        return new EffectFeasibility(enoughMaterialAvailable(inputInv), "");
    }
    
    public ItemStack getRecipeRepresentation() {
        if(!output.getItemStackRepresentation().isEmpty())
            return output.getItemStackRepresentation().iterator().next().copy();
        return new ItemStack(net.minecraft.world.item.Items.BEACON);
    }
    
    public List<ItemStack> getInputRepresentation(Container i, Factory r) {
        List<ItemStack> res = new ArrayList<>();
        res.addAll(input.getItemStackRepresentation());
        return res;
    }
}
