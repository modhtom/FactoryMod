package com.github.igotyou.FactoryMod.gui;

import net.minecraft.server.level.ServerPlayer;

public class ClickableInventory {
    public ClickableInventory(int size, String name) {}
    
    public void setSlot(IClickable c, int index) {}
    public IClickable getSlot(int index) { return null; }
    
    public void showInventory(ServerPlayer p) {}
}
