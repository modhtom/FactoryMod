package com.github.igotyou.FactoryMod.structures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import java.util.List;
import java.util.ArrayList;

public class PipeStructure extends MultiBlockStructure { 
    private BlockPos start;
    private BlockPos furnace;
    private BlockPos end;
    private boolean complete;

    public PipeStructure(ServerLevel world, BlockPos center) { 
        super(world);
        this.furnace = center;
        // Stub logic for finding the pipe ends
        this.complete = false;
    } 

    public PipeStructure(ServerLevel world, List<BlockPos> blocks) {
        super(world);
        if (blocks.size() >= 3) {
            this.start = blocks.get(0);
            this.furnace = blocks.get(1);
            this.end = blocks.get(2);
        }
    }

    public void recheckComplete() {
        this.complete = false;
    } 
    
    public boolean isComplete() { return complete; } 
    public BlockPos getCenter() { return furnace; } 
    public BlockPos getFurnace() { return furnace; }
    
    public List<BlockPos> getAllBlocks() { 
        List<BlockPos> list = new ArrayList<>();
        if (start != null) list.add(start);
        if (furnace != null) list.add(furnace);
        if (end != null) list.add(end);
        return list;
    } 
    
    public List<BlockPos> getRelevantBlocks() { return getAllBlocks(); } 
}
