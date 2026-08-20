package com.github.igotyou.FactoryMod.gui;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

public class Clickable implements IClickable {
    protected ItemStack item;
    
    public Clickable(ItemStack item) {
        this.item = item;
    }
    
    @Override
    public ItemStack getItemStack() {
        return item;
    }
    
    @Override
    public void clicked(ServerPlayer p) {
        // Mock default implementation
    }
    
    protected void onRightClick(ServerPlayer p) {}
    protected void onShiftRightClick(ServerPlayer p) {}
    protected void onDoubleClick(ServerPlayer p) {}
    
    @Override
    public void addedToInventory(ClickableInventory inv, int slot) {}
}
