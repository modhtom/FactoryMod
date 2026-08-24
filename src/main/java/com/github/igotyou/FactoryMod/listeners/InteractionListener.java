package com.github.igotyou.FactoryMod.listeners;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Factory;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class InteractionListener {
    public static void register() {
        // Right-click callback: only intercept when holding the factory interaction item (Stick)
        UseBlockCallback.EVENT.register((Player player, Level level, InteractionHand hand, BlockHitResult hitResult) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            
            FactoryModManager manager = FactoryMod.getInstance().getManager();
            if (manager == null) return InteractionResult.PASS;

            ItemStack handItem = serverPlayer.getMainHandItem();
            // If NOT holding a Stick, let vanilla open the chest / furnace / crafting table!
            if (handItem.getItem() != manager.getFactoryInteractionItem()) {
                return InteractionResult.PASS;
            }
            
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            
            Factory factory = manager.getFactoryAt(pos);
            if (factory != null) {
                factory.getInteractionManager().rightClick(serverPlayer, pos, state, hitResult.getDirection());
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        // Left-click (punch) callback: only intercept when holding the factory interaction item (Stick)
        AttackBlockCallback.EVENT.register((Player player, Level level, InteractionHand hand, BlockPos pos, Direction direction) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
            
            FactoryModManager manager = FactoryMod.getInstance().getManager();
            if (manager == null) return InteractionResult.PASS;

            ItemStack handItem = serverPlayer.getMainHandItem();
            // If NOT holding a Stick, let vanilla punch / mine the block!
            if (handItem.getItem() != manager.getFactoryInteractionItem()) {
                return InteractionResult.PASS;
            }
            
            BlockState state = level.getBlockState(pos);
            
            Factory factory = manager.getFactoryAt(pos);
            if (factory != null) {
                factory.getInteractionManager().leftClick(serverPlayer, pos, state, direction);
                return InteractionResult.SUCCESS;
            } else if (manager.isPossibleCenterBlock(state.getBlock().asItem())) {
                manager.attemptFccCreation(serverPlayer, pos);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        // Block break listener: dismantle factory if a part of it is broken
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level.isClientSide()) return;
            FactoryModManager manager = FactoryMod.getInstance().getManager();
            if (manager == null) return;
            Factory factory = manager.getFactoryAt(pos);
            if (factory != null) {
                manager.removeFactory(factory);
                
                // Return half the setup materials
                com.github.igotyou.FactoryMod.utility.ItemMap setupCost = null;
                for (FactoryModManager.FccDefinition def : manager.getFccDefinitions().values()) {
                    if (def.name().equals(factory.getName())) {
                        setupCost = def.setupCost();
                        break;
                    }
                }

                if (setupCost == null) {
                    setupCost = manager.getTotalSetupCost(factory);
                }
                
                if (setupCost != null) {
                    for (ItemStack stack : setupCost.getItemStackRepresentation()) {
                        int amount = stack.getCount() / 2;
                        if (amount > 0) {
                            ItemStack drop = stack.copy();
                            drop.setCount(amount);
                            net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                            level.addFreshEntity(itemEntity);
                        }
                    }
                }

                if (player instanceof ServerPlayer sp) {
                    sp.sendSystemMessage(Component.literal("\u00a7c" + factory.getName() + " was dismantled. You recovered half the materials."));
                }
            }
        });
    }
}
