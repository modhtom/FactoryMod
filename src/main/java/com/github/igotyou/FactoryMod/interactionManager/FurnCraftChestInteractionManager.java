package com.github.igotyou.FactoryMod.interactionManager;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Handles left/right click interactions and opens the recipe selector GUI. */
public final class FurnCraftChestInteractionManager implements IInteractionManager {
    private final FurnCraftChestFactory factory;

    public FurnCraftChestInteractionManager(FurnCraftChestFactory factory) {
        this.factory = factory;
    }

    @Override
    public void leftClick(ServerPlayer player, BlockPos block, BlockState state, Direction face) {
        if (factory.isActive()) {
            factory.deactivate();
            player.sendSystemMessage(Component.literal("\u00a7e" + factory.getName() + " deactivated."));
        } else {
            factory.attemptToActivate(player, false);
        }
    }

    @Override
    public void rightClick(ServerPlayer player, BlockPos block, BlockState state, Direction face) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("\u00a78" + factory.getName() + " Recipes"));

        List<IRecipe> recipes = factory.getRecipes();
        for (int index = 0; index < recipes.size() && index < 27; index++) {
            IRecipe recipe = recipes.get(index);
            ItemStack icon = (recipe instanceof InputRecipe input)
                ? input.getRecipeRepresentation() : ItemStack.EMPTY;
            if (icon == null || icon.isEmpty()) {
                icon = new ItemStack(Items.CRAFTING_TABLE);
            } else {
                icon = icon.copy();
            }

            boolean isCurrent = (recipe == factory.getCurrentRecipe());
            icon.set(DataComponents.CUSTOM_NAME, Component.literal((isCurrent ? "\u00a7a\u25b6 \u00a7l" : "\u00a7e") + recipe.getName()));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal("\u00a77Production time: \u00a7f" + (recipe.getProductionTime() / 20) + "s"));
            
            if (recipe instanceof InputRecipe inputRecipe) {
                lore.add(Component.literal("\u00a76Inputs:"));
                for (ItemStack inStack : inputRecipe.getInput().getItemStackRepresentation()) {
                    lore.add(Component.literal(" \u00a77- \u00a7f" + inStack.getCount() + "x " + inStack.getHoverName().getString()));
                }
                if (recipe instanceof ProductionRecipe prodRecipe) {
                    lore.add(Component.literal("\u00a7aOutputs:"));
                    for (ItemStack outStack : prodRecipe.getOutputRepresentation(null, factory)) {
                        lore.add(Component.literal(" \u00a77- \u00a7f" + outStack.getCount() + "x " + outStack.getHoverName().getString()));
                    }
                }
            }

            lore.add(Component.literal(""));
            if (isCurrent) {
                lore.add(Component.literal("\u00a7a\u2714 Currently Active Recipe"));
            } else {
                lore.add(Component.literal("\u00a7bClick to select this recipe"));
            }
            icon.set(DataComponents.LORE, new ItemLore(lore));

            gui.setSlot(index, icon, (slot, clickType, action, openedGui) -> {
                factory.setRecipe(recipe);
                player.sendSystemMessage(Component.literal("\u00a7aSelected recipe: \u00a7e" + recipe.getName()));
                openedGui.close();
            });
        }
        gui.open();
    }

    @Override
    public void blockBreak(ServerPlayer player, BlockPos block) {
        factory.deactivate();
    }

    @Override
    public void redStoneEvent(Object event, BlockPos block) { }
}
