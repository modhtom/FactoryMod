package com.github.igotyou.FactoryMod.events;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
public class RecipeExecuteEvent {
    public RecipeExecuteEvent(Factory f, IRecipe r) {}
    public boolean isCancelled() { return false; }
    public void setCancelled(boolean c) {}
}
