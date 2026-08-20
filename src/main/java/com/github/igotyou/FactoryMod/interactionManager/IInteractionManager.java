package com.github.igotyou.FactoryMod.interactionManager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface IInteractionManager {
    void leftClick(ServerPlayer p, BlockPos b, BlockState state, Direction face);
    void rightClick(ServerPlayer p, BlockPos b, BlockState state, Direction face);
    void blockBreak(ServerPlayer p, BlockPos b);
    void redStoneEvent(Object event, BlockPos b);
}
