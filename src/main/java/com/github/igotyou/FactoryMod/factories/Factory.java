package com.github.igotyou.FactoryMod.factories;

import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

public abstract class Factory implements Runnable {

    protected IInteractionManager im;
    protected MultiBlockStructure mbs;
    protected boolean active;
    protected int updateTime;
    protected String name;
    protected int threadId = -1;

    public Factory(IInteractionManager im, MultiBlockStructure mbs, int updateTime, String name) {
        this.im = im;
        this.mbs = mbs;
        this.updateTime = updateTime;
        this.name = name;
    }

    // Legacy constructor for stubs
    public Factory(String name, MultiBlockStructure mbs, int updateTime) {
        this.name = name;
        this.mbs = mbs;
        this.updateTime = updateTime;
    }

    public String getName() { return name; }
    public MultiBlockStructure getMultiBlockStructure() { return mbs; }
    public boolean isActive() { return active; }
    public int getUpdateTime() { return updateTime; }
    public void deactivate() { active = false; }

    public abstract void attemptToActivate(ServerPlayer p, boolean onStartUp);

    public void scheduleUpdate() {
        threadId = com.github.igotyou.FactoryMod.utility.TaskScheduler.scheduleSyncDelayedTask(this, getUpdateTime());
    }

    public void turnFurnaceOn(ServerLevel world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof FurnaceBlockEntity furnace) {
            furnace.setChanged();
        }
    }

    public void turnFurnaceOff(ServerLevel world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof FurnaceBlockEntity furnace) {
            furnace.setChanged();
        }
    }

    public String getLogData() {
        return name + " at " + (mbs != null ? mbs.getCenter() : "unknown");
    }

    public IInteractionManager getInteractionManager() { return im; }
    public void setInteractionManager(IInteractionManager im) { this.im = im; }
    
    // Repair System
    protected int health = 10000;
    protected int maxHealth = 10000;
    
    public int getHealth() { return health; }
    public void setHealth(int h) { this.health = Math.min(h, maxHealth); }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    
    public void repair(int amount) {
        this.health = Math.min(this.health + amount, maxHealth);
    }
    
    public Object getRepairManager() { return this; } // Self as repair manager for now

    @Override
    public void run() {}
}

