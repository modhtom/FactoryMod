package com.github.igotyou.FactoryMod.utility;

import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.List;

public class FactoryModGUI {
    private final FactoryModManager manager;

    public FactoryModGUI(FactoryModManager manager) {
        this.manager = manager;
    }

    public void showFactoryOverview(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Factory Overview"));
        
        List<Factory> factories = manager.getAllFactories().stream().toList();
        for (int i = 0; i < factories.size(); i++) {
            Factory f = factories.get(i);
            // Just basic listing
            gui.setSlot(i, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CRAFTING_TABLE), (index, type, action, gui1) -> {
                // Do something
            });
        }
        gui.open();
    }

    public void showFactoryMenu(ServerPlayer player, Factory factory) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal(factory.getName()));
        
        // This menu lets the player interact with the factory
        // E.g. view recipes, activate/deactivate
        // Just stub for now.
        gui.open();
    }
}
