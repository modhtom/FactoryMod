package com.github.igotyou.FactoryMod.structures;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import java.util.List;
import java.util.LinkedList;

public abstract class MultiBlockStructure {
    protected ServerLevel world;
    
    public MultiBlockStructure(ServerLevel world) {
        this.world = world;
    }

    public ServerLevel getWorld() { return world; }

    public abstract List<BlockPos> getRelevantBlocks();
    public abstract List<BlockPos> getAllBlocks();
    public abstract BlockPos getCenter();
    public abstract boolean isComplete();
    public abstract void recheckComplete();

    public static boolean isBlockDirectlyConnectedTo(ServerLevel world, BlockPos b, Item m) { 
        for (Direction d : Direction.values()) {
            if (world.getBlockState(b.relative(d)).is(net.minecraft.world.level.block.Block.byItem(m))) return true;
        }
        return false; 
    }
    
    public static boolean isBlockDirectlyConnectedTo(ServerLevel world, BlockPos b, Item m, Direction blockedFace) { 
        for (Direction d : Direction.values()) {
            if (d == blockedFace) continue;
            if (world.getBlockState(b.relative(d)).is(net.minecraft.world.level.block.Block.byItem(m))) return true;
        }
        return false; 
    }
    
    public static List<BlockPos> getAdjacentBlocks(BlockPos b) { 
        List<BlockPos> result = new LinkedList<>();
        for (Direction d : Direction.values()) {
            result.add(b.relative(d));
        }
        return result; 
    }
    
    public static boolean insecureRedstone(ServerLevel world, BlockPos here) { 
        return false; 
    }
    
    public boolean blockedByExistingFactory() { 
        for (BlockPos block : getAllBlocks()) {
            if (com.github.igotyou.FactoryMod.FactoryMod.getInstance().getManager().factoryExistsAt(block)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isCompletelyDestroyed() { 
        return !isComplete(); 
    }
}
