package com.github.igotyou.FactoryMod.structures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import java.util.List;
import java.util.ArrayList;

public class BlockFurnaceStructure extends MultiBlockStructure { 
    private BlockPos furnace;
    private boolean complete;

    public BlockFurnaceStructure(ServerLevel world, BlockPos center) { 
        super(world);
        if (world.getBlockState(center).is(Blocks.FURNACE)) {
            this.furnace = center;
        }
        this.complete = this.furnace != null;
    } 

    public BlockFurnaceStructure(ServerLevel world, List<BlockPos> blocks) {
        super(world);
        this.furnace = blocks.get(0);
        recheckComplete();
    }

    public void recheckComplete() {
        this.complete = furnace != null && world.getBlockState(furnace).is(Blocks.FURNACE);
    } 
    
    public boolean isComplete() { return complete; } 
    public BlockPos getCenter() { return furnace; } 
    public BlockPos getFurnace() { return furnace; }
    
    public List<BlockPos> getAllBlocks() { 
        List<BlockPos> list = new ArrayList<>();
        list.add(furnace);
        return list;
    } 
    
    public List<BlockPos> getRelevantBlocks() { return getAllBlocks(); } 
}
