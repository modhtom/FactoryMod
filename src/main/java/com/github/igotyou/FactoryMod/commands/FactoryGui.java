package com.github.igotyou.FactoryMod.commands;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;

public class FactoryGui {

    public static void openFactoryList(ServerPlayer player) {
        FactoryModManager manager = FactoryMod.getInstance().getManager();
        if (manager == null) return;
        List<FactoryModManager.FccDefinition> defs = new ArrayList<>(manager.getFccDefinitions().values());
        openFactoryListPage(player, defs, 0);
    }

    private static void openFactoryListPage(ServerPlayer player, List<FactoryModManager.FccDefinition> defs, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Factories (Page " + (page + 1) + ")"));

        int perPage = 45;
        int startIndex = page * perPage;
        int endIndex = Math.min(startIndex + perPage, defs.size());

        for (int i = startIndex; i < endIndex; i++) {
            FactoryModManager.FccDefinition def = defs.get(i);
            ItemStack icon = new ItemStack(Items.FURNACE);
            if (!def.setupCost().getItemStackRepresentation().isEmpty()) {
                icon = def.setupCost().getItemStackRepresentation().iterator().next().copy();
            }

            GuiElementBuilder builder = new GuiElementBuilder(icon.getItem())
                .setName(Component.literal("\u00a7a" + def.name()))
                .addLoreLine(Component.literal("\u00a77Recipes: " + def.recipes().size()))
                .addLoreLine(Component.literal(""));
            builder.addLoreLine(Component.literal("\u00a76Setup Cost:"));
            for (ItemStack is : def.setupCost().getItemStackRepresentation()) {
                builder.addLoreLine(Component.literal("\u00a77 - " + is.getCount() + "x " + is.getHoverName().getString()));
            }

            builder.setCallback((index, type, action, gui1) -> {
                openRecipeListPage(player, def, def.recipes(), 0, defs, page);
            });

            gui.setSlot(i - startIndex, builder);
        }

        // Navigation
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW).setName(Component.literal("\u00a7ePrevious Page"))
                .setCallback((i, t, a, g) -> openFactoryListPage(player, defs, page - 1)));
        }
        if (endIndex < defs.size()) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW).setName(Component.literal("\u00a7eNext Page"))
                .setCallback((i, t, a, g) -> openFactoryListPage(player, defs, page + 1)));
        }

        gui.open();
    }

    private static void openRecipeListPage(ServerPlayer player, FactoryModManager.FccDefinition def, List<IRecipe> recipes, int page, List<FactoryModManager.FccDefinition> defs, int parentPage) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(def.name() + " Recipes (" + (page + 1) + ")"));

        int perPage = 45;
        int startIndex = page * perPage;
        int endIndex = Math.min(startIndex + perPage, recipes.size());

        for (int i = startIndex; i < endIndex; i++) {
            IRecipe recipe = recipes.get(i);
            ItemStack icon = new ItemStack(Items.PAPER);
            if (recipe instanceof InputRecipe ir && !ir.getOutputRepresentation(null, null).isEmpty()) {
                icon = ir.getOutputRepresentation(null, null).get(0).copy();
            }

            GuiElementBuilder builder = new GuiElementBuilder(icon.getItem())
                .setName(Component.literal("\u00a7a" + recipe.getName()))
                .addLoreLine(Component.literal("\u00a77Time: " + recipe.getProductionTime() / 20 + "s"))
                .addLoreLine(Component.literal(""));
                
            if (recipe instanceof InputRecipe ir) {
                builder.addLoreLine(Component.literal("\u00a76Input:"));
                for (ItemStack is : ir.getInput().getItemStackRepresentation()) {
                    builder.addLoreLine(Component.literal("\u00a77 - " + is.getCount() + "x " + is.getHoverName().getString()));
                }
                
                List<ItemStack> outputs = ir.getOutputRepresentation(null, null);
                if (outputs != null && !outputs.isEmpty()) {
                    builder.addLoreLine(Component.literal(""));
                    builder.addLoreLine(Component.literal("\u00a76Output:"));
                    for (ItemStack is : outputs) {
                        builder.addLoreLine(Component.literal("\u00a77 - " + is.getCount() + "x " + is.getHoverName().getString()));
                    }
                }
            }

            gui.setSlot(i - startIndex, builder);
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER).setName(Component.literal("\u00a7cBack to Factories"))
            .setCallback((i, t, a, g) -> openFactoryListPage(player, defs, parentPage)));

        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW).setName(Component.literal("\u00a7ePrevious Page"))
                .setCallback((i, t, a, g) -> openRecipeListPage(player, def, recipes, page - 1, defs, parentPage)));
        }
        if (endIndex < recipes.size()) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW).setName(Component.literal("\u00a7eNext Page"))
                .setCallback((i, t, a, g) -> openRecipeListPage(player, def, recipes, page + 1, defs, parentPage)));
        }

        gui.open();
    }
}
