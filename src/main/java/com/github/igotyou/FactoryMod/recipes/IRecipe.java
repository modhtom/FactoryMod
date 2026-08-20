package com.github.igotyou.FactoryMod.recipes;
import net.minecraft.world.Container;
import com.github.igotyou.FactoryMod.factories.Factory;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public interface IRecipe {
    String getName();
    String getIdentifier();
    int getProductionTime();
    default int getProductionTime(Factory fccf) { return getProductionTime(); }
    boolean enoughMaterialAvailable(Container inputInv);
    default EffectFeasibility evaluateEffectFeasibility(Container inputInv, Container outputInv, Factory fccf) { return new EffectFeasibility(true, null); }
    boolean applyEffect(Container inputInv, Container outputInv, Factory fccf);
    String getTypeIdentifier();
    default boolean canApplySpeed() { return true; }
    default boolean mustBeLoaded() { return false; }
}
