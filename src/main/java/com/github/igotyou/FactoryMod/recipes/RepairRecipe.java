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

public class RepairRecipe extends InputRecipe {

    private int healthPerRun;

    public RepairRecipe(String identifier, String name, int productionTime, ItemMap input,
                        int healthPerRun) {
        super(identifier, name, productionTime, input);
        this.healthPerRun = healthPerRun;
    }

    @Override
    public List<ItemStack> getOutputRepresentation(Container i, Factory fccf) {
        List<ItemStack> result = new LinkedList<>();
        ItemStack furn = new ItemStack(Items.FURNACE);
        ItemUtils.setLore(furn, "+" + healthPerRun + " health");
        result.add(furn);
        return result;
    }

    @Override
    public boolean applyEffect(Container inputInv, Container outputInv, Factory fccf) {
        if (enoughMaterialAvailable(inputInv)) {
            if (input.removeSafelyFrom(inputInv)) {
                fccf.repair(healthPerRun);
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
    public String getTypeIdentifier() {
        return "REPAIR";
    }

    public int getHealth() {
        return healthPerRun;
    }

    @Override
    public List<String> getTextualOutputRepresentation(Container i, Factory fccf) {
        return Arrays.asList("Repairs the factory by " + healthPerRun + " health");
    }

    public EffectFeasibility evaluateEffectFeasibility(Container inputInv, Container outputInv, Factory fccf) {
        if (fccf.getHealth() >= fccf.getMaxHealth()) {
            return new EffectFeasibility(false, "Factory is already fully repaired");
        }
        if (!enoughMaterialAvailable(inputInv)) {
            return new EffectFeasibility(false, "insufficient input materials");
        }
        return new EffectFeasibility(true, null);
    }
    
    public ItemStack getRecipeRepresentation() {
        return new ItemStack(Items.FURNACE);
    }
    
    public List<ItemStack> getInputRepresentation(Container i, Factory r) {
        List<ItemStack> res = new ArrayList<>();
        res.addAll(input.getItemStackRepresentation());
        return res;
    }
}

