package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import com.github.igotyou.FactoryMod.utility.ItemUtils;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Upgraderecipe extends InputRecipe {

    // private FurnCraftChestEgg egg; // using Object to bypass if not migrated
    private Object egg; // FIXME when FurnCraftChestEgg is ported

    public Upgraderecipe(String identifier, String name, int productionTime, ItemMap input,
                         Object egg) {
        super(identifier, name, productionTime, input);
        this.egg = egg;
    }

    @Override
    public boolean applyEffect(Container inputInv, Container outputInv, Factory fccf) {
        ItemMap inMap = new ItemMap();
        for(int i=0; i<inputInv.getContainerSize(); i++) {
             inMap.addItemStack(inputInv.getItem(i));
        }
        if (input.isContainedIn(inMap)) {
            if (input.removeSafelyFrom(inputInv)) {
                // upgrade logic
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean enoughMaterialAvailable(Container inputInv) {
        ItemMap inMap = new ItemMap();
        for(int i=0; i<inputInv.getContainerSize(); i++) {
             inMap.addItemStack(inputInv.getItem(i));
        }
        return input.isContainedIn(inMap);
    }

    @Override
    public List<ItemStack> getOutputRepresentation(Container i, Factory fccf) {
        List<ItemStack> res = new LinkedList<>();
        ItemStack cr = new ItemStack(Items.CRAFTING_TABLE);
        ItemUtils.setLore(cr, "Upgrade to get new and better recipes");
        res.add(cr);
        return res;
    }

    public Object getEgg() {
        return egg;
    }

    @Override
    public String getTypeIdentifier() {
        return "UPGRADE";
    }

    @Override
    public List<String> getTextualOutputRepresentation(Container i, Factory fccf) {
        return Arrays.asList("Upgrades the factory");
    }

    public EffectFeasibility evaluateEffectFeasibility(Container inputInv, Container outputInv, Factory fccf) {
        return new EffectFeasibility(enoughMaterialAvailable(inputInv), "");
    }
    
    public ItemStack getRecipeRepresentation() {
        return new ItemStack(Items.CRAFTING_TABLE);
    }
    
    public List<ItemStack> getInputRepresentation(Container i, Factory r) {
        List<ItemStack> res = new ArrayList<>();
        res.addAll(input.getItemStackRepresentation());
        return res;
    }
}
