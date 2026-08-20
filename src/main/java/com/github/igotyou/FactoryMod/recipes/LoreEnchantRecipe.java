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

public class LoreEnchantRecipe extends InputRecipe {

    private List<String> appliedLore;
    private List<String> overwritenLore;
    private ItemMap tool;
    private ItemStack exampleInput;
    private ItemStack exampleOutput;

    public LoreEnchantRecipe(String identifier, String name, int productionTime, ItemMap input, ItemMap tool, List<String> appliedLore,
                             List<String> overwritenLore) {
        super(identifier, name, productionTime, input);
        this.overwritenLore = overwritenLore;
        this.appliedLore = appliedLore;
        this.tool = tool;
        if (!tool.getItemStackRepresentation().isEmpty()) {
            exampleInput = tool.getItemStackRepresentation().iterator().next().copy();
            for (String s : overwritenLore) {
                ItemUtils.addLore(exampleInput, s);
            }
            exampleOutput = tool.getItemStackRepresentation().iterator().next().copy();
            for (String s : appliedLore) {
                ItemUtils.addLore(exampleOutput, s);
            }
        } else {
            exampleInput = new ItemStack(Items.STONE);
            exampleOutput = new ItemStack(Items.STONE);
        }
    }

    @Override
    public boolean enoughMaterialAvailable(Container inputInv) {
        ItemMap map = new ItemMap();
        for(int i=0; i<inputInv.getContainerSize(); i++) {
            map.addItemStack(inputInv.getItem(i));
        }
        if (input.isContainedIn(map)) {
            ItemStack toolio = exampleInput;
            for (int i = 0; i < inputInv.getContainerSize(); i++) {
                ItemStack is = inputInv.getItem(i);
                if (is != null && !is.isEmpty() && toolio.getItem() == is.getItem() && hasStackRequiredLore(is)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<ItemStack> getOutputRepresentation(Container i, Factory fccf) {
        ItemStack is = exampleOutput.copy();
        List<ItemStack> stacks = new LinkedList<>();
        stacks.add(is);
        return stacks;
    }

    @Override
    public boolean applyEffect(Container inputInv, Container outputInv, Factory fccf) {
        if (input.removeSafelyFrom(inputInv)) {
            ItemStack toolio = exampleInput;
            for (int i = 0; i < inputInv.getContainerSize(); i++) {
                ItemStack is = inputInv.getItem(i);
                if (is != null && !is.isEmpty() && toolio.getItem() == is.getItem() && hasStackRequiredLore(is)) {
                    // For Fabric, just completely replace the lore or add new ones
                    // In a simpler way, we just add the applied lore
                    for(String s : appliedLore) {
                         ItemUtils.addLore(is, s);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasStackRequiredLore(ItemStack is) {
        if (is == null || is.isEmpty()) {
            return false;
        }
        net.minecraft.world.item.component.ItemLore loreComp = is.get(net.minecraft.core.component.DataComponents.LORE);
        if (loreComp == null && !overwritenLore.isEmpty()) {
            return false;
        }
        if (overwritenLore.isEmpty()) {
            return true;
        }
        List<String> loreStr = new ArrayList<>();
        if(loreComp != null) {
            for(net.minecraft.network.chat.Component t : loreComp.lines()) {
                 loreStr.add(t.getString());
            }
        }
        for(String ov : overwritenLore) {
            boolean found = false;
            for(String s : loreStr) {
                if(s.contains(ov)) { found = true; break; }
            }
            if(!found) return false;
        }
        return true;
    }

    @Override
    public String getTypeIdentifier() {
        return "LOREENCHANT";
    }

    public List<String> getAppliedLore() {
        return appliedLore;
    }

    public List<String> getOverwrittenLore() {
        return overwritenLore;
    }

    public ItemMap getTool() {
        return tool;
    }

    @Override
    public List<String> getTextualOutputRepresentation(Container i, Factory fccf) {
        return Arrays.asList("1 " + ItemUtils.getItemName(exampleOutput));
    }
    
    public EffectFeasibility evaluateEffectFeasibility(Container inputInv, Container outputInv, Factory fccf) {
        return new EffectFeasibility(enoughMaterialAvailable(inputInv), "");
    }
    
    public ItemStack getRecipeRepresentation() {
        return exampleOutput.copy();
    }
    
    public List<ItemStack> getInputRepresentation(Container i, Factory r) {
        List<ItemStack> res = new ArrayList<>();
        res.addAll(input.getItemStackRepresentation());
        return res;
    }
}
