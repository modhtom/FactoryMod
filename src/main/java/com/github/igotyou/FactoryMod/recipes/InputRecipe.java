package com.github.igotyou.FactoryMod.recipes;

import net.minecraft.world.Container;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.ArrayList;

public abstract class InputRecipe implements IRecipe {
    protected String name;
    protected String identifier;
    protected int productionTime;
    protected ItemMap input;

    public InputRecipe(String identifier, String name, int productionTime, ItemMap input) {
        this.identifier = identifier;
        this.name = name;
        this.productionTime = productionTime;
        this.input = input;
    }

    public String getIdentifier() { return identifier; }
    public String getName() { return name; }
    public int getProductionTime() { return productionTime; }
    public ItemMap getInput() { return input; }

    public boolean enoughMaterialAvailable(Container inv) {
        ItemMap have = new ItemMap();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            have.addItemStack(inv.getItem(i));
        }
        return input.isContainedIn(have);
    }

    public abstract List<ItemStack> getOutputRepresentation(Container i, Factory fccf);
    public abstract List<String> getTextualOutputRepresentation(Container i, Factory fccf);

    public List<ItemStack> getInputRepresentation(Container i, IRecipe r) { 
        return new ArrayList<>(input.getItemStackRepresentation());
    }
    public ItemStack getRecipeRepresentation() { return null; }
}
