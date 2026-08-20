package com.github.igotyou.FactoryMod.interactionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
public class PipeInteractionManager implements IInteractionManager {
    public void leftClick(ServerPlayer p, BlockPos b, BlockState state, Direction face) {}
    public void rightClick(ServerPlayer p, BlockPos b, BlockState state, Direction face) {}
    public void blockBreak(ServerPlayer p, BlockPos b) {}
    public void redStoneEvent(Object event, BlockPos b) {}
}
