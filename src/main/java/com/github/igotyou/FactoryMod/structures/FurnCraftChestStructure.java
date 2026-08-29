package com.github.igotyou.FactoryMod.structures;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import java.util.List;
import java.util.ArrayList;

public class FurnCraftChestStructure extends MultiBlockStructure { 
    private BlockPos craftingTable;
    private BlockPos furnace;
    private BlockPos chest;
    private boolean complete;

    public FurnCraftChestStructure(ServerLevel world, BlockPos center) { 
        super(world);
        if (world.getBlockState(center).is(Blocks.CRAFTING_TABLE)) {
            this.craftingTable = center;
            List<BlockPos> chestBlocks = new ArrayList<>();
            for (Direction d : Direction.values()) {
                BlockState s = world.getBlockState(center.relative(d));
                if (s.is(Blocks.CHEST) || s.is(Blocks.TRAPPED_CHEST) || s.is(Blocks.BARREL)) {
                    chestBlocks.add(center.relative(d));
                }
            }
            
            for (BlockPos b : chestBlocks) {
                Direction chestFace = null;
                for (Direction d : Direction.values()) {
                    if (center.relative(d).equals(b)) {
                        chestFace = d; break;
                    }
                }
                if (chestFace == null) continue;
                Direction furnaceFace = chestFace.getOpposite();
                BlockPos furnaceBlock = center.relative(furnaceFace);
                if (world.getBlockState(furnaceBlock).is(Blocks.FURNACE)) {
                    this.chest = b;
                    this.furnace = furnaceBlock;
                    break;
                }
            }
        }
        this.complete = this.chest != null && this.furnace != null;
    } 

    public FurnCraftChestStructure(ServerLevel world, List<BlockPos> blocks) {
        super(world);
        this.craftingTable = blocks.get(0);
        this.furnace = blocks.get(1);
        this.chest = blocks.get(2);
        recheckComplete();
    }

    public void recheckComplete() { 
        this.complete = craftingTable != null && world.getBlockState(craftingTable).is(Blocks.CRAFTING_TABLE)
                     && furnace != null && world.getBlockState(furnace).is(Blocks.FURNACE)
                     && chest != null && (world.getBlockState(chest).is(Blocks.CHEST) || world.getBlockState(chest).is(Blocks.TRAPPED_CHEST) || world.getBlockState(chest).is(Blocks.BARREL));
    } 
    
    public boolean isComplete() { return complete; } 
    public BlockPos getCenter() { return craftingTable; } 
    
    public List<BlockPos> getAllBlocks() { 
        List<BlockPos> list = new ArrayList<>();
        list.add(craftingTable); list.add(furnace); list.add(chest);
        return list;
    } 
    
    public List<BlockPos> getRelevantBlocks() { 
        return getAllBlocks();
    }
    
    public BlockPos getCraftingTable() { return craftingTable; }
    public BlockPos getFurnace() { return furnace; }
    public BlockPos getChest() { return chest; }
}
