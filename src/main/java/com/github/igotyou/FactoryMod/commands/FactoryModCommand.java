package com.github.igotyou.FactoryMod.commands;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FactoryModCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("fm")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    if (source.getEntity() instanceof ServerPlayer player) {
                        FactoryGui.openFactoryList(player);
                    } else {
                        source.sendSystemMessage(Component.literal("FactoryMod is active."));
                    }
                    return 1;
                })
                .then(Commands.literal("help")
                    .executes(context -> {
                        if (context.getSource().getEntity() instanceof ServerPlayer player) {
                            showHelp(player);
                        }
                        return 1;
                    })
                )
                .then(Commands.literal("list")
                    .executes(context -> {
                        if (context.getSource().getEntity() instanceof ServerPlayer player) {
                            FactoryGui.openFactoryList(player);
                        }
                        return 1;
                    })
                )
                .then(Commands.literal("recipes")
                    .executes(context -> {
                        if (context.getSource().getEntity() instanceof ServerPlayer player) {
                            showRecipes(player);
                        }
                        return 1;
                    })
                )
            );
            
            // Also alias /factory and /factories
            dispatcher.register(Commands.literal("factory")
                .executes(context -> {
                    if (context.getSource().getEntity() instanceof ServerPlayer player) {
                        FactoryGui.openFactoryList(player);
                    }
                    return 1;
                })
            );
            dispatcher.register(Commands.literal("factories")
                .executes(context -> {
                    if (context.getSource().getEntity() instanceof ServerPlayer player) {
                        FactoryGui.openFactoryList(player);
                    }
                    return 1;
                })
            );
        });
    }

    private static void showHelp(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("\u00a76=== FactoryMod Guide ==="));
        player.sendSystemMessage(Component.literal("\u00a7e1. Build Structure:\u00a7f Place a \u00a7bCrafting Table\u00a7f in the center."));
        player.sendSystemMessage(Component.literal("   Place a \u00a7bChest\u00a7f (or Barrel) on one side, and a \u00a7bFurnace\u00a7f on the exact opposite side in a straight line: \u00a77[Chest - Crafting Table - Furnace]"));
        player.sendSystemMessage(Component.literal("\u00a7e2. Setup Cost:\u00a7f Put required creation items in the Chest or inventory. (Run \u00a7a/fm list\u00a7f to see required items)"));
        player.sendSystemMessage(Component.literal("\u00a7e3. Create:\u00a7f Left-click (hit) the \u00a7bCrafting Table\u00a7f with a \u00a7bStick\u00a7f."));
        player.sendSystemMessage(Component.literal("\u00a7e4. Select Recipe:\u00a7f Right-click the Crafting Table or Furnace with a \u00a7bStick\u00a7f to open the recipe menu."));
        player.sendSystemMessage(Component.literal("\u00a7e5. Run:\u00a7f Put fuel (Coal) in the Furnace and input items in the Chest. Left-click the factory with a \u00a7bStick\u00a7f to toggle on/off."));
    }

    private static void showList(ServerPlayer player) {
        FactoryModManager manager = FactoryMod.getInstance().getManager();
        if (manager == null) {
            player.sendSystemMessage(Component.literal("\u00a7cFactoryMod is still initializing..."));
            return;
        }
        player.sendSystemMessage(Component.literal("\u00a76=== Configured Factory Types ==="));
        for (FactoryModManager.FccDefinition def : manager.getFccDefinitions().values()) {
            player.sendSystemMessage(Component.literal("\u00a7e\u2022 \u00a7a" + def.name() + " \u00a77(" + def.recipes().size() + " recipes)"));
            StringBuilder sb = new StringBuilder("  \u00a77Setup cost: \u00a7f");
            for (ItemStack is : def.setupCost().getItemStackRepresentation()) {
                sb.append(is.getCount()).append("x ").append(is.getHoverName().getString()).append(", ");
            }
            if (sb.length() > 2) sb.setLength(sb.length() - 2);
            player.sendSystemMessage(Component.literal(sb.toString()));
        }
    }

    private static void showRecipes(ServerPlayer player) {
        FactoryModManager manager = FactoryMod.getInstance().getManager();
        if (manager == null) return;
        player.sendSystemMessage(Component.literal("\u00a76=== Available Recipes ==="));
        for (IRecipe recipe : manager.getRecipes().values()) {
            player.sendSystemMessage(Component.literal("\u00a7e\u2022 \u00a7a" + recipe.getName() + " \u00a77(" + (recipe.getProductionTime() / 20) + "s)"));
        }
    }
}
