package com.github.igotyou.FactoryMod.eggs;

import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import java.util.List;

public class FurnCraftChestEgg implements IFactoryEgg {
    private String name;
    private int updateTime;
    private List<IRecipe> recipes;
    private ItemMap fuel;
    private int fuelConsumptionIntervall;
    private double returnRate;

    public FurnCraftChestEgg(String name, int updateTime, List<IRecipe> recipes, ItemMap fuel, int fuelConsumptionIntervall, double returnRate) {
        this.name = name;
        this.updateTime = updateTime;
        this.recipes = recipes;
        this.fuel = fuel;
        this.fuelConsumptionIntervall = fuelConsumptionIntervall;
        this.returnRate = returnRate;
    }

    @Override
    public String getName() { return name; }
    
    public List<IRecipe> getRecipes() { return recipes; }
    public ItemMap getFuel() { return fuel; }
    public int getFuelConsumptionIntervall() { return fuelConsumptionIntervall; }
    public int getUpdateTime() { return updateTime; }

    @Override
    public Factory hatch(MultiBlockStructure m, ServerPlayer p) {
        if (m instanceof FurnCraftChestStructure fccs) {
            return new FurnCraftChestFactory(fccs, updateTime, name, recipes, fuel, fuelConsumptionIntervall);
        }
        return null;
    }

    public Factory revive(List<BlockPos> b, int h, String r, int t) {
        return null;
    }

    public Class<? extends MultiBlockStructure> getMultiBlockStructure() {
        return FurnCraftChestStructure.class;
    }

    @Override
    public double getReturnRate() { return returnRate; }
}
